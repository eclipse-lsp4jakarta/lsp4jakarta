package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

// Invalid: injecting UserTransaction via @Inject field in an @ApplicationScoped CDI bean
@ApplicationScoped
public class UserTransactionInjectedInCdiBean {

    @Inject
    private UserTransaction userTransaction;

    public void doWork() {
        // business logic
    }
}

// Invalid: injecting UserTransaction via @Inject field in a @RequestScoped CDI bean
@RequestScoped
class AnotherTransactionalBean {

    @Inject
    private UserTransaction tx;

    public void process() {
        // business logic
    }
}

// Invalid: injecting UserTransaction via @Inject field in a @SessionScoped CDI bean
@SessionScoped
class SessionTransactionalBean implements java.io.Serializable {

    @Inject
    private UserTransaction sessionTx;

    public void execute() {
        // business logic
    }
}

// Invalid: injecting UserTransaction via @Inject initializer method parameter in a CDI bean
@ApplicationScoped
class BeanWithInjectMethod {

    @Inject
    public void init(UserTransaction ut) {
        // initializer
    }
}
