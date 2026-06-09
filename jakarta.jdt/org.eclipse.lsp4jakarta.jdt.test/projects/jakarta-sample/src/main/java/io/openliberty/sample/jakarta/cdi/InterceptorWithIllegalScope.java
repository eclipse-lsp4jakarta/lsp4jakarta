package io.openliberty.sample.jakarta.cdi;

import jakarta.interceptor.Interceptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;

// Valid interceptor with explicit @Dependent scope
@Interceptor
@Dependent
class ValidInterceptorWithDependent {
}

// Valid interceptor with no scope (defaults to @Dependent)
@Interceptor
class ValidInterceptorWithNoScope {
}

// Invalid interceptor with @ApplicationScoped
@Interceptor
@ApplicationScoped
class InterceptorWithApplicationScoped {
}

// Invalid interceptor with @SessionScoped
@Interceptor
@SessionScoped
class InterceptorWithSessionScoped {
}

// Invalid interceptor with @RequestScoped
@Interceptor
@RequestScoped
class InterceptorWithRequestScoped {
}

// Invalid interceptor with @ConversationScoped
@Interceptor
@ConversationScoped
class InterceptorWithConversationScoped {
}

// Invalid interceptor with multiple scopes including illegal ones
@Interceptor
@ApplicationScoped
@SessionScoped
class InterceptorWithMultipleIllegalScopes {
}
