package io.openliberty.sample.jakarta.di;

import jakarta.inject.Scope;

@Scope
public @interface InvalidScopeAttributesOnlyFields {

	public static final int STATUS = 0;
	
	int TOKEN = 1;
	
	String DEFAULT_VALUE = "default";
}
