package io.openliberty.sample.jakarta.di;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ApplicationScoped
public class CustomQualifiers {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface Fast {
	}

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Secure {
	}
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Gone {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Unused {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Invalid {
	}

}