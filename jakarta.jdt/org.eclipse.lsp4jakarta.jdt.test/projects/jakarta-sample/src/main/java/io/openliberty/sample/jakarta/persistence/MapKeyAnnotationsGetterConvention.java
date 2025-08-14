package io.openliberty.sample.jakarta.persistence;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.MapKey;
import jakarta.persistence.MapKeyClass;

public class MapKeyAnnotationsGetterConvention {

    Integer age;

    
    String name;

    Map<Integer, String> place;

    Map<Integer, String> gender;

    Map<Integer, String> testMap = new HashMap<>();

    
    @MapKeyClass(Map.class)
    public Map<Integer, String> getTestMap() {
        return this.testMap;
    }

    
    public Integer getAge() {
        return this.age;
    }

    public String getName() {
        return this.name;
    }

    @MapKeyClass(Map.class)
    private Map<Integer, String> getPlace() {
        return this.place;
    }

    @MapKey()
    public  Map<Integer, String> geGender() {
        return null;
    }

    @MapKey()
    public Map<Integer, String> getPerform() {
        return null;
    }

}
