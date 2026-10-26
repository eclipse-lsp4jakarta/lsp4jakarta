package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;

@Monitored
@Interceptor
@Priority(-100)
public class InvalidNegativePriorityInterceptor {
    
    public InvalidNegativePriorityInterceptor() {
    }
}