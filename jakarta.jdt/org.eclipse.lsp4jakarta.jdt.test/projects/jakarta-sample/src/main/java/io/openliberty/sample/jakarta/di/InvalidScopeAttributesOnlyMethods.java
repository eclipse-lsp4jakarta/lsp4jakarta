package io.openliberty.sample.jakarta.di;

import jakarta.inject.Scope;

@Scope
public @interface InvalidScopeAttributesOnlyMethods {

	String value();
	
	int token();
	
	boolean enabled();
}
