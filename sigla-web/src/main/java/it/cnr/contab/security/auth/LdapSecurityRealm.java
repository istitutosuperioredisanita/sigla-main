/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.Serializable;
import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;

/**
 * Security Realm per autenticazione LDAP con Elytron
 */
public class LdapSecurityRealm implements SecurityRealm {

    private String providerUrl = "ldap://localhost:389";
    private String bindDN;
    private String bindCredential;
    private String baseCtxDN = "o=cnr,c=it";
    private String baseFilter = "uid={0}";
    private String rolesCtxDN = "ou=groups,o=cnr,c=it";
    private String roleFilter = "member={1}";
    private String roleAttributeID = "cn";
    private String defaultRole = "default-roles-cnr";
    private String[] userAttributes = new String[]{
            "mail", "cnrnome", "cnrcognome", "codicefiscale", "emailcertificatoperpuk"
    };

    public LdapSecurityRealm() {
    }

    public LdapSecurityRealm(Map<String, String> options) {
        if (options != null) {
            this.providerUrl = options.getOrDefault("ldap.url", this.providerUrl);
            this.bindDN = options.get("ldap.bindDN");
            this.bindCredential = options.get("ldap.bindCredential");
            this.baseCtxDN = options.getOrDefault("ldap.baseCtxDN", this.baseCtxDN);
            this.baseFilter = options.getOrDefault("ldap.baseFilter", this.baseFilter);
            this.rolesCtxDN = options.getOrDefault("ldap.rolesCtxDN", this.rolesCtxDN);
            this.roleFilter = options.getOrDefault("ldap.roleFilter", this.roleFilter);
            this.roleAttributeID = options.getOrDefault("ldap.roleAttributeID", this.roleAttributeID);
            this.defaultRole = options.getOrDefault("ldap.defaultRole", this.defaultRole);

            String userAttrsStr = options.get("ldap.userAttributes");
            if (userAttrsStr != null) {
                this.userAttributes = userAttrsStr.split(";");
            }
        }
    }

    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        return new LdapRealmIdentity(principal);
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
     * Crea contesto LDAP
     */
    private DirContext createContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        if (bindDN != null && bindCredential != null) {
            env.put(Context.SECURITY_PRINCIPAL, bindDN);
            env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        }

        return new InitialDirContext(env);
    }

    /**
     * Implementazione RealmIdentity per LDAP
     */
    private class LdapRealmIdentity implements RealmIdentity {
        private final Principal principal;
        private String userDN;
        private Map<String, Object> userAttributes;
        private Set<String> roles;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            LdapRealmIdentity that = (LdapRealmIdentity) o;
            return Objects.equals(principal, that.principal);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(principal);
        }

        public LdapRealmIdentity(Principal principal) {
            this.principal = principal;
        }

        @Override
        public Principal getRealmIdentityPrincipal() {
            return this.principal;
        }

        @Override
        public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType,
                                                        String algorithmName,
                                                        AlgorithmParameterSpec parameterSpec)
                throws RealmUnavailableException {
            return LdapSecurityRealm.this.getCredentialAcquireSupport(
                    credentialType, algorithmName, parameterSpec
            );
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType)
                throws RealmUnavailableException {
            return null; // LDAP non fornisce le password
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType,
                                                      String algorithmName)
                throws RealmUnavailableException {
            return null;
        }

        @Override
        public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
                                                     String algorithmName)
                throws RealmUnavailableException {
            return LdapSecurityRealm.this.getEvidenceVerifySupport(evidenceType, algorithmName);
        }

        @Override
        public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
            if (evidence instanceof PasswordGuessEvidence passwordEvidence) {
                char[] password = passwordEvidence.getGuess();

                try {
                    // Prima trova il DN dell'utente
                    findUserDN();

                    if (userDN == null) {
                        return false;
                    }

                    // Prova autenticazione LDAP
                    Hashtable<String, String> env = new Hashtable<>();
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    env.put(Context.PROVIDER_URL, providerUrl);
                    env.put(Context.SECURITY_AUTHENTICATION, "simple");
                    env.put(Context.SECURITY_PRINCIPAL, userDN);
                    env.put(Context.SECURITY_CREDENTIALS, new String(password));

                    DirContext userCtx = null;
                    try {
                        userCtx = new InitialDirContext(env);
                        return true; // Autenticazione riuscita
                    } finally {
                        if (userCtx != null) {
                            userCtx.close();
                        }
                    }
                } catch (NamingException e) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean exists() throws RealmUnavailableException {
            try {
                findUserDN();
                return userDN != null;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public AuthorizationIdentity getAuthorizationIdentity() throws RealmUnavailableException {
            findUserDN();
            loadRoles();

            // Aggiungi sempre il ruolo default
            if (roles == null) {
                roles = new HashSet<>();
            }
            roles.add(defaultRole);

            // Crea attributes
            MapAttributes attributes = new MapAttributes();
            attributes.addAll("Roles", roles);

            // Aggiungi attributi utente se disponibili
            if (userAttributes != null) {
                for (Map.Entry<String, Object> entry : userAttributes.entrySet()) {
                    attributes.addFirst(entry.getKey(), entry.getValue().toString());
                }
            }
            return AuthorizationIdentity.basicIdentity(attributes);
        }

        /**
         * Cerca il DN dell'utente in LDAP
         */
        private void findUserDN() throws RealmUnavailableException {
            if (userDN != null) {
                return; // Già trovato
            }

            DirContext ctx = null;
            try {
                ctx = createContext();

                String filter = baseFilter.replace("{0}", principal.getName());
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                controls.setReturningAttributes(LdapSecurityRealm.this.userAttributes);

                NamingEnumeration<SearchResult> results = ctx.search(baseCtxDN, filter, controls);

                if (results.hasMore()) {
                    SearchResult result = results.next();
                    userDN = result.getNameInNamespace();

                    // Carica attributi utente
                    userAttributes = new HashMap<>();
                    javax.naming.directory.Attributes attrs = result.getAttributes();
                    NamingEnumeration<? extends Attribute> allAttrs = attrs.getAll();

                    while (allAttrs.hasMore()) {
                        Attribute attr = allAttrs.next();
                        String attrId = attr.getID();
                        Object attrValue = attr.get();
                        userAttributes.put(attrId, attrValue);
                    }
                }
            } catch (NamingException e) {
                throw new RealmUnavailableException("Failed to search LDAP for user", e);
            } finally {
                if (ctx != null) {
                    try {
                        ctx.close();
                    } catch (NamingException e) {
                        // Ignora
                    }
                }
            }
        }

        /**
         * Carica i ruoli dell'utente da LDAP
         */
        private void loadRoles() throws RealmUnavailableException {
            if (roles != null || userDN == null) {
                return;
            }

            roles = new HashSet<>();
            DirContext ctx = null;

            try {
                ctx = createContext();

                String filter = roleFilter.replace("{1}", userDN);
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                controls.setReturningAttributes(new String[]{roleAttributeID});

                NamingEnumeration<SearchResult> results = ctx.search(rolesCtxDN, filter, controls);

                while (results.hasMore()) {
                    SearchResult result = results.next();
                    Attributes attrs = result.getAttributes();
                    Attribute roleAttr = attrs.get(roleAttributeID);

                    if (roleAttr != null) {
                        String role = (String) roleAttr.get();
                        if (role != null && !role.isEmpty()) {
                            roles.add(role);
                        }
                    }
                }
            } catch (NamingException e) {
                // Nessun accesso ai ruoli
                //throw new RealmUnavailableException("Failed to load roles from LDAP", e);
            } finally {
                if (ctx != null) {
                    try {
                        ctx.close();
                    } catch (NamingException e) {
                        // Ignora
                    }
                }
            }
        }
    }
}