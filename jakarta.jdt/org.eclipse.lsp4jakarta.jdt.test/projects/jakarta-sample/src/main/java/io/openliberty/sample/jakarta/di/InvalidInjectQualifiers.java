package io.openliberty.sample.jakarta.di;


import io.openliberty.sample.jakarta.di.CustomQualifiers.Fast;
import io.openliberty.sample.jakarta.di.CustomQualifiers.Secure;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.Service;

public class InvalidInjectQualifiers {

	@Inject 
	@Fast 
	@Secure 
	private Processor processor; 

	@Inject 
	public InvalidInjectQualifiers(@Fast @Secure Processor processor) 
	{ 
		this.processor = processor; 
	}
	
	@Inject
	@Named("Fast")
	@Default
	private InnerBean bean;
	
	
	public InnerBean getBean() {
		return bean;
	}

	@Inject
	public void setBean(io.openliberty.sample.jakarta.di.InvalidInjectQualifiers.InnerBean bean) {
		this.bean = bean;
	}

	public static class InnerBean {

		@Inject
		@Named("Fast")
		@Default
		private Service service;
	}
	
	public static class Processor {
		
		@Inject
		@Fast
		@Default
		private Service service;
	}
	
}

class TempClass{
	int id;
}