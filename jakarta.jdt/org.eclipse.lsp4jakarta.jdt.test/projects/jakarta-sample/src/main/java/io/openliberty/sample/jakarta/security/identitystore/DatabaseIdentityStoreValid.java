package io.openliberty.sample.jakarta.security.identitystore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:comp/DefaultDataSource",
    callerQuery = "select password from caller where name = ?",
    groupsQuery = "select group_name from caller_groups where caller_name = ?"
)
@ApplicationScoped
public class DatabaseIdentityStoreValid {
    // Valid: Has both @ApplicationScoped and implicit @Default qualifier
    
    private String dataSourceName;
    private int queryTimeout;
    
    public String getDataSourceName() {
        return dataSourceName;
    }
    
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
    
    public int getQueryTimeout() {
        return queryTimeout;
    }
    
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}
