/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.cnr.contab.security.auth;

import org.wildfly.security.auth.SupportLevel;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.MapAttributes;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.interfaces.ClearPassword;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Security Realm per Elytron che replica la logica del vecchio DatabaseServerLoginModule
 */
public class DatabaseSecurityRealm implements SecurityRealm {

    private String dsJndiName = "java:/jdbc/CIR";
    private String principalsQuery = "SELECT PASSWORD FROM UTENTE WHERE UPPER(cd_utente)=UPPER(?) AND FL_AUTENTICAZIONE_LDAP='N'";
    private String rolesQuery = "SELECT DISTINCT CD_RUOLO, 'Roles' FROM UTENTE_UNITA_RUOLO WHERE UPPER(cd_utente)=UPPER(?)";
    private boolean ignorePasswordCase = true;

    public DatabaseSecurityRealm() {
    }

    public DatabaseSecurityRealm(Map<String, String> options) {
        if (options != null) {
            this.dsJndiName = options.getOrDefault("dsJndiName", this.dsJndiName);
            this.principalsQuery = options.getOrDefault("principalsQuery", this.principalsQuery);
            this.rolesQuery = options.getOrDefault("rolesQuery", this.rolesQuery);
            this.ignorePasswordCase = Boolean.parseBoolean(
                    options.getOrDefault("ignorePasswordCase", "true")
            );
        }
    }

    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        return new DatabaseRealmIdentity(principal);
    }

    @Override
    public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType,
                                                    String algorithmName,
                                                    AlgorithmParameterSpec parameterSpec)
            throws RealmUnavailableException {
        return PasswordCredential.class.isAssignableFrom(credentialType)
                ? SupportLevel.POSSIBLY_SUPPORTED
                : SupportLevel.UNSUPPORTED;
    }

    @Override
    public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
                                                 String algorithmName)
            throws RealmUnavailableException {
        return PasswordGuessEvidence.class.isAssignableFrom(evidenceType)
                ? SupportLevel.SUPPORTED
                : SupportLevel.UNSUPPORTED;
    }

    /**
     * Replica la logica di hashing password del vecchio DatabaseServerLoginModule
     */
    private String createPasswordHash(String username, String password) {
        byte[] buser = username.toUpperCase().getBytes(StandardCharsets.UTF_8);

        // Gestione speciale per utenti PDGP
        if (username.startsWith("PDGP")) {
            return password;
        }

        byte[] bpassword = password.toUpperCase().getBytes(StandardCharsets.UTF_8);
        byte h = 0;

        for (int i = 0; i < bpassword.length; i++) {
            h = (byte) (bpassword[i] ^ h);
            for (int j = 0; j < buser.length; j++) {
                bpassword[i] ^= buser[j] ^ h;
            }
        }

        return Base64.getEncoder().encodeToString(bpassword);
    }

    /**
     * Implementazione RealmIdentity per database
     */
    private class DatabaseRealmIdentity implements RealmIdentity {
        private final Principal principal;
        private String storedPassword;
        private Set<String> roles;

        public DatabaseRealmIdentity(Principal principal) {
            this.principal = principal;
        }

        @Override
        public Principal getRealmIdentityPrincipal() {
            return principal;
        }

        @Override
        public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType,
                                                        String algorithmName,
                                                        AlgorithmParameterSpec parameterSpec)
                throws RealmUnavailableException {
            return DatabaseSecurityRealm.this.getCredentialAcquireSupport(
                    credentialType, algorithmName, parameterSpec
            );
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType)
                throws RealmUnavailableException {
            return getCredential(credentialType, null);
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType,
                                                      String algorithmName)
                throws RealmUnavailableException {
            if (PasswordCredential.class.isAssignableFrom(credentialType)) {
                loadPasswordAndRoles();
                if (storedPassword != null) {
                    Password password = ClearPassword.createRaw(
                            ClearPassword.ALGORITHM_CLEAR,
                            storedPassword.toCharArray()
                    );
                    return credentialType.cast(new PasswordCredential(password));
                }
            }
            return null;
        }

        @Override
        public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
                                                     String algorithmName)
                throws RealmUnavailableException {
            return DatabaseSecurityRealm.this.getEvidenceVerifySupport(evidenceType, algorithmName);
        }

        @Override
        public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
            if (evidence instanceof PasswordGuessEvidence) {
                PasswordGuessEvidence passwordEvidence = (PasswordGuessEvidence) evidence;
                char[] guess = passwordEvidence.getGuess();

                loadPasswordAndRoles();

                // Se non c'è password memorizzata, l'autenticazione fallisce
                if (storedPassword == null) {
                    return false;
                }
                // Se la password è vuota significa che è il primo accesso e va cambiata
                if (storedPassword.isEmpty()) {
                    return true;
                }

                // Calcola l'hash della password fornita
                String inputPassword = new String(guess);
                String hashedInput = createPasswordHash(principal.getName(), inputPassword);

                // Confronta
                if (ignorePasswordCase) {
                    return hashedInput.equalsIgnoreCase(storedPassword);
                } else {
                    return hashedInput.equals(storedPassword);
                }
            }
            return false;
        }

        @Override
        public boolean exists() throws RealmUnavailableException {
            loadPasswordAndRoles();
            return storedPassword != null;
        }

        @Override
        public AuthorizationIdentity getAuthorizationIdentity() throws RealmUnavailableException {
            loadPasswordAndRoles();

            // Aggiungi sempre il ruolo default
            if (roles == null) {
                roles = new HashSet<>();
            }
            roles.add("default-roles-cnr");

            // Crea attributes con i ruoli
            MapAttributes attributes = new MapAttributes();
            attributes.addAll("Roles", roles);

            return AuthorizationIdentity.basicIdentity(attributes);
        }

        /**
         * Carica password e ruoli dal database
         */
        private void loadPasswordAndRoles() throws RealmUnavailableException {
            if (storedPassword != null || roles != null) {
                return; // Già caricato
            }

            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                InitialContext ctx = new InitialContext();
                DataSource ds = (DataSource) ctx.lookup(dsJndiName);
                conn = ds.getConnection();

                // Carica password
                ps = conn.prepareStatement(principalsQuery);
                ps.setString(1, principal.getName());
                rs = ps.executeQuery();

                if (rs.next()) {
                    storedPassword = Optional.ofNullable(rs.getString(1)).orElse("");
                }

                rs.close();
                ps.close();

                // Carica ruoli
                roles = new HashSet<>();
                ps = conn.prepareStatement(rolesQuery);
                ps.setString(1, principal.getName());
                rs = ps.executeQuery();

                while (rs.next()) {
                    String role = rs.getString(1);
                    if (role != null && !role.isEmpty()) {
                        roles.add(role);
                    }
                }

            } catch (Exception e) {
                throw new RealmUnavailableException("Failed to load user data from database", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (ps != null) ps.close();
                    if (conn != null) conn.close();
                } catch (Exception e) {
                    // Ignora errori di chiusura
                }
            }
        }
    }
}