package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;

@Interceptor
@Priority(-100)
public class InvalidNegativePriorityInterceptor {
    
    public InvalidNegativePriorityInterceptor() {
    }
}