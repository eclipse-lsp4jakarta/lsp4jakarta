package org.eclipse.lsp4jakarta.jdt.internal.jsonp;

/**
*
* This enum is used to indicate whether a JSON builder should
* construct an object, an array, or handle an unknown type.
* 
*/
public enum JSONBuilderType {
	
	/**
     * Indicates that the JSON builder should create a JSON object.
     */
    OBJECT,
    
    /**
     * Indicates that the JSON builder should create a JSON array.
     */
    ARRAY,
    
    /**
     * Indicates that the JSON type is unknown or cannot be determined.
     * This can be used as a default or fallback value.
     */
    UNKNOWN
}
