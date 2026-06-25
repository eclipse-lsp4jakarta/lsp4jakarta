package io.openliberty.sample.jakarta.cdi;

import jakarta.interceptor.Interceptor;
import jakarta.decorator.Decorator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;

// ========== Valid Interceptors ==========

// Valid interceptor with explicit @Dependent scope
@Interceptor
@Dependent
class ValidInterceptorWithDependent {
}

// Valid interceptor with no scope (defaults to @Dependent)
@Interceptor
class ValidInterceptorWithNoScope {
}

// ========== Valid Decorators ==========

// Valid decorator with explicit @Dependent scope
@Decorator
@Dependent
class ValidDecoratorWithDependent {
}

// Valid decorator with no scope (defaults to @Dependent)
@Decorator
class ValidDecoratorWithNoScope {
}

// ========== Invalid Interceptors with Built-in Normal Scopes ==========

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

// Invalid interceptor with multiple scopes including illegal ones
@Interceptor
@ApplicationScoped
@SessionScoped
class InterceptorWithMultipleIllegalScopes {
}

// ========== Invalid Decorators with Built-in Normal Scopes ==========

// Invalid decorator with @ApplicationScoped
@Decorator
@ApplicationScoped
class DecoratorWithApplicationScoped {
}

// Invalid decorator with @SessionScoped
@Decorator
@SessionScoped
class DecoratorWithSessionScoped {
}

// Invalid decorator with multiple scopes including illegal ones
@Decorator
@RequestScoped
@ConversationScoped
class DecoratorWithMultipleIllegalScopes {
}

// ========== Invalid Interceptors/Decorators with Custom Normal Scopes ==========

// Invalid interceptor with custom normal scope
@Interceptor
@CustomNormalScope
class InterceptorWithCustomNormalScope {
}

// Invalid decorator with custom normal scope
@Decorator
@CustomNormalScope
class DecoratorWithCustomNormalScope {
}

// Invalid interceptor with both built-in and custom normal scopes
@Interceptor
@ApplicationScoped
@CustomNormalScope
class InterceptorWithMixedScopes {
}

// Invalid decorator with both built-in and custom normal scopes
@Decorator
@ApplicationScoped
@CustomNormalScope
class DecoratorWithMixedScopes {
}
