package io.openliberty.sample.jakarta.di;

import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CustomQualifiers {

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Fast {
	}

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Secure {
	}

}