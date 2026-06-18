package io.openliberty.sample.jakarta.security.identitystore;

import jakarta.enterprise.context.RequestScoped;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;

@LdapIdentityStoreDefinition(
    url = "ldap://localhost:10389",
    callerBaseDn = "ou=caller,dc=jsr375,dc=net",
    groupSearchBase = "ou=group,dc=jsr375,dc=net"
)
@RequestScoped
public class LdapIdentityStoreWithWrongScope {
    // Invalid: Has @RequestScoped instead of @ApplicationScoped
    
    private String searchFilter;
    private boolean useSSL;
    
    public String getSearchFilter() {
        return searchFilter;
    }
    
    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }
    
    public boolean isUseSSL() {
        return useSSL;
    }
    
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }
}
