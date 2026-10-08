package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptors;

@Interceptors(Monitored.class)
public final class InvalidInterceptorModifiersOnClassMethod {
	// ERROR: public final method (non-static, non-private)
	public final void processPayment() {

    }
	// ERROR: protected final method (non-static, non-private)
	protected final void processRefund() {
	}
	// ERROR: package-private (default) final method (non-static, non-private)
	final void processVoid() {
	}
	// Valid: private final method is permitted
	private final void internalProcess() {
	}
	// Valid: public static final method is permitted
	public static final void publicStaticHelper() {
	}
	// Valid: protected static final method is permitted
	protected static final void protectedStaticHelper() {
	}
	// Valid: package-private static final method is permitted
	static final void packageStaticHelper() {
	}
	// Valid: private static final method is permitted
	private static final void privateStaticHelper() {
	}
}
