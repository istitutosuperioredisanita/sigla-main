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
import org.wildfly.security.authz.Attributes;
import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.MapAttributes;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.Evidence;

import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Collections;
import java.util.Map;

/**
 * Security Realm combinato che replica la logica dei login modules di Thorntail.
 * Prova prima LDAP (sufficient), poi Database (sufficient).
 */
public class CombinedSecurityRealm implements SecurityRealm {

    private LdapSecurityRealm ldapRealm;
    private DatabaseSecurityRealm databaseRealm;
    private boolean resolved = false;
    private String authenticatedBy; // NUOVO

    // Aggiungi questo metodo
    public String getAuthenticatedBy() {
        return authenticatedBy;
    }
    public CombinedSecurityRealm() {
        this.ldapRealm = new LdapSecurityRealm();
        this.databaseRealm = new DatabaseSecurityRealm();
    }

    public CombinedSecurityRealm(Map<String, String> ldapOptions, Map<String, String> databaseOptions) {
        this.ldapRealm = new LdapSecurityRealm(ldapOptions);
        this.databaseRealm = new DatabaseSecurityRealm(databaseOptions);
    }

    public void initialize(Map<String, String> configuration) {
        this.ldapRealm = new LdapSecurityRealm(configuration);
        this.databaseRealm = new DatabaseSecurityRealm(configuration);
    }

    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        return new CombinedRealmIdentity(principal);
    }

    @Override
    public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType,
                                                    String algorithmName,
                                                    AlgorithmParameterSpec parameterSpec)
            throws RealmUnavailableException {
        SupportLevel ldapSupport = ldapRealm.getCredentialAcquireSupport(
                credentialType, algorithmName, parameterSpec
        );
        if (ldapSupport == SupportLevel.SUPPORTED) {
            return ldapSupport;
        }
        return databaseRealm.getCredentialAcquireSupport(
                credentialType, algorithmName, parameterSpec
        );
    }

    @Override
    public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
                                                 String algorithmName)
            throws RealmUnavailableException {
        // Entrambi supportano la verifica password
        return SupportLevel.SUPPORTED;
    }

    /**
     * RealmIdentity combinato che delega a LDAP o Database
     */
    private class CombinedRealmIdentity implements RealmIdentity {
        private final Principal principal;
        private RealmIdentity delegateIdentity;
        private boolean resolved = false;
        private String authenticatedBy; // NUOVO

        // Aggiungi questo metodo
        public String getAuthenticatedBy() {
            return authenticatedBy;
        }

        public CombinedRealmIdentity(Principal principal) {
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
            resolveIdentity();
            return delegateIdentity != null
                    ? delegateIdentity.getCredentialAcquireSupport(credentialType, algorithmName, parameterSpec)
                    : SupportLevel.UNSUPPORTED;
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType)
                throws RealmUnavailableException {
            resolveIdentity();
            return delegateIdentity != null
                    ? delegateIdentity.getCredential(credentialType)
                    : null;
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType,
                                                      String algorithmName)
                throws RealmUnavailableException {
            resolveIdentity();
            return delegateIdentity != null
                    ? delegateIdentity.getCredential(credentialType, algorithmName)
                    : null;
        }

        @Override
        public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
                                                     String algorithmName)
                throws RealmUnavailableException {
            return CombinedSecurityRealm.this.getEvidenceVerifySupport(evidenceType, algorithmName);
        }

        @Override
        public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
            // Prova prima LDAP (sufficient)
            try {
                RealmIdentity ldapIdentity = ldapRealm.getRealmIdentity(principal);
                if (ldapIdentity.exists() && ldapIdentity.verifyEvidence(evidence)) {
                    delegateIdentity = ldapIdentity;
                    authenticatedBy = "LDAP"; // NUOVO
                    resolved = true;
                    return true;
                }
            } catch (Exception e) {
                // LDAP non disponibile o autenticazione fallita, prova database
            }

            // Prova Database (sufficient)
            try {
                RealmIdentity dbIdentity = databaseRealm.getRealmIdentity(principal);
                if (dbIdentity.exists() && dbIdentity.verifyEvidence(evidence)) {
                    delegateIdentity = dbIdentity;
                    authenticatedBy = "DATABASE"; // NUOVO
                    resolved = true;
                    return true;
                }
            } catch (Exception e) {
                // Database non disponibile o autenticazione fallita
            }

            return false;
        }

        @Override
        public boolean exists() throws RealmUnavailableException {
            // L'utente esiste se esiste in LDAP o nel database
            try {
                RealmIdentity ldapIdentity = ldapRealm.getRealmIdentity(principal);
                if (ldapIdentity.exists()) {
                    return true;
                }
            } catch (Exception e) {
                // Ignora errori LDAP
            }

            try {
                RealmIdentity dbIdentity = databaseRealm.getRealmIdentity(principal);
                return dbIdentity.exists();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public AuthorizationIdentity getAuthorizationIdentity() throws RealmUnavailableException {
            resolveIdentity();
            if (delegateIdentity != null) {
                AuthorizationIdentity baseIdentity = delegateIdentity.getAuthorizationIdentity();

                // Determina quale realm è stato usato
                String realmType = (delegateIdentity.equals(ldapRealm.getRealmIdentity(principal))) ? "LDAP" : "DATABASE";

                // Crea un'identità con attributi personalizzati
                return AuthorizationIdentity.basicIdentity(baseIdentity,
                        new MapAttributes(Collections.singletonMap("AUTH_REALM",
                                Collections.singletonList(realmType))));
            }
            return AuthorizationIdentity.EMPTY;
        }

        /**
         * Risolve quale identity usare (LDAP o Database)
         */
        private void resolveIdentity() throws RealmUnavailableException {
            if (resolved) {
                return;
            }

            // Prova prima LDAP
            try {
                RealmIdentity ldapIdentity = ldapRealm.getRealmIdentity(principal);
                if (ldapIdentity.exists()) {
                    delegateIdentity = ldapIdentity;
                    resolved = true;
                    return;
                }
            } catch (Exception e) {
                // LDAP non disponibile
            }

            // Prova Database
            try {
                RealmIdentity dbIdentity = databaseRealm.getRealmIdentity(principal);
                if (dbIdentity.exists()) {
                    delegateIdentity = dbIdentity;
                    resolved = true;
                    return;
                }
            } catch (Exception e) {
                // Database non disponibile
            }

            resolved = true;
        }
    }

}