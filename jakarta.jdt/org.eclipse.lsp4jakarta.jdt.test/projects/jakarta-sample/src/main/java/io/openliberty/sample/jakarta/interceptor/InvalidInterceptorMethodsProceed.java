package io.openliberty.sample.jakarta.interceptor;

import java.util.logging.Logger;
import java.util.logging.Level;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


public class InvalidInterceptorMethodsProceed {
	
	private static final Logger LOGGER = Logger.getLogger(InvalidInterceptorMethodsProceed.class.getName());

    @AroundInvoke
    public Object missingProceedInvoke(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "Interceptor method without proceed");
        return null; 
    }

    @AroundConstruct
    public Object missingProceedConstruct(InvocationContext ctx) throws Exception {
    	LOGGER.log(Level.INFO, "Around construct without proceed");
        return new Object(); 
    }

    @AroundTimeout
    public Object missingProceedTimeout(InvocationContext ctx) throws Exception {
    	LOGGER.log(Level.INFO, "Around timeout without proceed");
        return null; 
    }

    @PostConstruct
    public void invalidPostConstruct(InvocationContext ctx) throws Exception {
    	LOGGER.log(Level.INFO, "PostConstruct called");
    }

    @PreDestroy
    public void invalidPreDestroy(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "PreDestroy called");
    }
    
    public void validMethod(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "Valid Method called");
    }
    
	class InnerClassInterceptorMethod {

		@AroundInvoke
	    public Object missingProceedInvokeInner(InvocationContext ctx) throws Exception {
	        LOGGER.log(Level.INFO, "Interceptor method without proceed");
	        return null; 
	    }

	    public Object missingProceedConstructInner(InvocationContext ctx) throws Exception {
	        LOGGER.log(Level.INFO, "Around construct without proceed");
	        return new Object(); 
	    }

	    public Object missingProceedTimeoutInner(InvocationContext ctx) throws Exception {
	        LOGGER.log(Level.INFO, "Around timeout without proceed");
	        return null; 
	    }

	    @PostConstruct
	    public void invalidPostConstructInner(InvocationContext ctx) throws Exception {
	        LOGGER.log(Level.INFO, "PostConstruct called");
	    }
	    
		@PreDestroy
		public void invalidPreDestroyInner(InvocationContext ctx) throws Exception {
			LOGGER.log(Level.INFO, "PreDestroy called");
		}
	}
}