package io.openliberty.sample.jakarta.persistence;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.MapKey;
import jakarta.persistence.MapKeyClass;

public class MapKeyAnnotationsType {

    Integer age;

    @MapKey()
    String name;

    Map<Integer, String> place;

    Map<Integer, String> gender;

    Map<Integer, String> testMap = new HashMap<>();

   
    public Map<Integer, String> getTestMap() {
        return this.testMap;
    }

    @MapKey()
    public Integer getAge() {
        return this.age;
    }

    public String getName() {
        return this.name;
    }

    private Map<Integer, String> getPlace() {
        return this.place;
    }

}
