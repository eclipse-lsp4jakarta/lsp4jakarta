package io.openliberty.sample.jakarta.interceptor;

import jakarta.fake.Interceptor;

@Interceptor
public abstract class InvalidAbstractInterceptor {

	String config;

	String config1;

	private InvalidAbstractInterceptor() {

	}

	private InvalidAbstractInterceptor(String config, String config1) {

	}

	public InvalidAbstractInterceptor(String config) {

	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

}
