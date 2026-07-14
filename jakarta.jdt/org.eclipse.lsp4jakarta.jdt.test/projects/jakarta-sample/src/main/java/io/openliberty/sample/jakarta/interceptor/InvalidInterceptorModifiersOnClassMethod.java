package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptors;

@Interceptors(Monitored.class)
public final class InvalidInterceptorModifiersOnClassMethod {
	// Diagnostic: non-static, non-private final method
	public final void processPayment() {
		
    }
	// No diagnostic: private final method is permitted
	private final void internalProcess() {
	}
	// No diagnostic: static final method is permitted
	public static final void staticHelper() {
	}
}
