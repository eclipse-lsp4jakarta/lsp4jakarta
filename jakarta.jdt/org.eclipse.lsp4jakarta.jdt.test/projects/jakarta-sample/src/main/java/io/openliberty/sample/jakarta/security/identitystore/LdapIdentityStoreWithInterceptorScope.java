package io.openliberty.sample.jakarta.security.identitystore;

import jakarta.interceptor.Interceptor;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;

@LdapIdentityStoreDefinition(
    url = "ldap://localhost:10389",
    callerBaseDn = "ou=caller,dc=jsr375,dc=net",
    groupSearchBase = "ou=group,dc=jsr375,dc=net"
)
@Interceptor
public class LdapIdentityStoreWithInterceptorScope {
    // Invalid: Has @Interceptor instead of @ApplicationScoped
    
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