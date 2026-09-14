package io.openliberty.sample.jakarta.security.identitystore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;

@LdapIdentityStoreDefinition(
    url = "ldap://localhost:10389",
    callerBaseDn = "ou=caller,dc=jsr375,dc=net",
    groupSearchBase = "ou=group,dc=jsr375,dc=net"
)
@ApplicationScoped
public class LdapIdentityStoreValid {
    // Valid: Has both @ApplicationScoped and implicit @Default qualifier
    
    private String ldapServerUrl;
    private int connectionTimeout;
    
    public String getLdapServerUrl() {
        return ldapServerUrl;
    }
    
    public void setLdapServerUrl(String ldapServerUrl) {
        this.ldapServerUrl = ldapServerUrl;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
