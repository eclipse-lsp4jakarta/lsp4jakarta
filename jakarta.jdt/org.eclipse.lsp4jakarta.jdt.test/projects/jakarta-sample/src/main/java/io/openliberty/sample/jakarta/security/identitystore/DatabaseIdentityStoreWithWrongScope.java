package io.openliberty.sample.jakarta.security.identitystore;

import jakarta.enterprise.context.RequestScoped;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:comp/DefaultDataSource",
    callerQuery = "select password from caller where name = ?",
    groupsQuery = "select group_name from caller_groups where caller_name = ?"
)
@RequestScoped
public class DatabaseIdentityStoreWithWrongScope {
    // Invalid: Has @RequestScoped instead of @ApplicationScoped
    
    private String hashAlgorithm;
    private boolean enableCaching;
    
    public String getHashAlgorithm() {
        return hashAlgorithm;
    }
    
    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }
    
    public boolean isEnableCaching() {
        return enableCaching;
    }
    
    public void setEnableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
    }
}
