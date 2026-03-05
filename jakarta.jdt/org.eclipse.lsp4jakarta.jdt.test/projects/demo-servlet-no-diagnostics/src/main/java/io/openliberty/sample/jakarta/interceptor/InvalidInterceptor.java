package io.openliberty.sample.jakarta.interceptor;

import jakarta.fake.Interceptor;

@Interceptor
public abstract class InvalidInterceptor {
	
	String config;
	
	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public InvalidInterceptor(String config) {
		
	}
	
	@Interceptor
	public class InnerInvalidInterceptor{
		
		String innerConfig;
		
		public String getInnerConfig() {
			return innerConfig;
		}

		public void setInnerConfig(String innerConfig) {
			this.innerConfig = innerConfig;
		}

		public InnerInvalidInterceptor(String innerConfig) {
			
		}
		
	}

}

