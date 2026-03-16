package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptor;

@Interceptor
public abstract class InvalidInterceptor {
	
	String config;
	
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
		
		public String getInnerConfig() {
			return innerConfig;
		}
		
		public InnerInvalidInterceptor(String config) {
				
		}

		public void setInnerConfig(String innerConfig) {
			this.innerConfig = innerConfig;
		}

	}

}

