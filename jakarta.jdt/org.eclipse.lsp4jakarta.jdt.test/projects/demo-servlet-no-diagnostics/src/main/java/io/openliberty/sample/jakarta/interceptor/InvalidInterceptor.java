package io.openliberty.sample.jakarta.interceptor;

import jakarta.fake.Interceptor;

@Interceptor
public class InvalidInterceptor {

	String config;

	String config1;

	private InvalidInterceptor() {

	}

	private InvalidInterceptor(String config, String config1) {

	}

	public InvalidInterceptor(String config) {

	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	@Interceptor
	public class InnerInvalidInterceptor{

		String innerConfig;

		String innerConfig1;

		public String getInnerConfig() {
			return innerConfig;
		}

		protected InnerInvalidInterceptor() {

		}

		public InnerInvalidInterceptor(String innerConfig) {

		}

		protected InnerInvalidInterceptor(String innerConfig, String innerConfig2) {

		}

		public void setInnerConfig(String innerConfig) {
			this.innerConfig = innerConfig;
		}

	}

}
