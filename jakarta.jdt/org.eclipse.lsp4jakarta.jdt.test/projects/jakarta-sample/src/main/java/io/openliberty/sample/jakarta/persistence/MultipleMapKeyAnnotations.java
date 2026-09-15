package io.openliberty.sample.jakarta.persistence;

import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyJoinColumn;

@Entity
public class MultipleMapKeyAnnotations {
	
	@Id
	private int id;
	
    @MapKeyJoinColumn()
    @MapKeyJoinColumn()
    Map<Integer, String> test1;
    
    @MapKeyJoinColumn(name = "n1")
    @MapKeyJoinColumn(referencedColumnName = "rcn2")
    Map<Integer, String> test2;
    
    @MapKeyJoinColumn(name = "n1", referencedColumnName = "rcn1")
    @MapKeyJoinColumn()
    Map<Integer, String> test3;
}