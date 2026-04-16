package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseMappedSuperclassWithIdOnGetter {
    
    private Long id;
    private String commonField;
    
    @Id
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCommonField() {
        return commonField;
    }
    
    public void setCommonField(String commonField) {
        this.commonField = commonField;
    }
}
