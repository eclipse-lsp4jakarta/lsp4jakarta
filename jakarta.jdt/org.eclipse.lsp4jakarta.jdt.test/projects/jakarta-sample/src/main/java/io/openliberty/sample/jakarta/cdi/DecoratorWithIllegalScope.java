package io.openliberty.sample.jakarta.cdi;

import jakarta.decorator.Decorator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;

// Valid decorator with explicit @Dependent scope
@Decorator
@Dependent
class ValidDecoratorWithDependent {
}

// Valid decorator with no scope (defaults to @Dependent)
@Decorator
class ValidDecoratorWithNoScope {
}

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

// Invalid decorator with @RequestScoped
@Decorator
@RequestScoped
class DecoratorWithRequestScoped {
}

// Invalid decorator with @ConversationScoped
@Decorator
@ConversationScoped
class DecoratorWithConversationScoped {
}

// Invalid decorator with multiple scopes including illegal ones
@Decorator
@RequestScoped
@ConversationScoped
class DecoratorWithMultipleIllegalScopes {
}
