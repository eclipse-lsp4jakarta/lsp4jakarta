package io.openliberty.sample.jakarta.di;

import jakarta.inject.Scope;

@Scope
public @interface InvalidScopeAttributes {

	public static final int status = 0; 
	
	String value();
	
	int token = 0;
}

