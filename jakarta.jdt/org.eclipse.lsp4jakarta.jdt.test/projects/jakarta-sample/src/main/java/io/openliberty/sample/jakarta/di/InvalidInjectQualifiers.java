package io.openliberty.sample.jakarta.di;


import io.openliberty.sample.jakarta.di.CustomQualifiers.*;
import jakarta.enterprise.inject.Any;
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
		
		@Inject
		@Any
		@Default
		private Service service2;
		
		@Inject
		public Processor(@Default @Any Service service) {
			super();
			this.service = service;
		}
	}
	
	@Inject
	@Any
	@Default
	private InnerBean bean2;
	
	
	public InnerBean getBean2() {
		return bean2;
	}

	@Inject
	public void setBean2(@Any @Default InnerBean bean2) {
		this.bean2 = bean2;
	}
	
	@Inject
	@Gone
	@Unused
	@Invalid
	private InnerBean bean3;
	
}

class TempClass{
	int id;
}