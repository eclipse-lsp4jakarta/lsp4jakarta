package io.openliberty.sample.jakarta.security.identitystore;

import jakarta.decorator.Decorator;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:comp/DefaultDataSource",
    callerQuery = "select password from caller where name = ?",
    groupsQuery = "select group_name from caller_groups where caller_name = ?"
)
@Decorator
public class DatabaseIdentityStoreWithDecoratorScope {
    // Invalid: Has @Decorator instead of @ApplicationScoped
    
    private String dataSourceName;
    private int maxPoolSize;
    
    public String getDataSourceName() {
        return dataSourceName;
    }
    
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
    
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}