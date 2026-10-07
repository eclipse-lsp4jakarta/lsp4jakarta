package io.openliberty.sample.jakarta.cdi;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.transaction.UserTransaction;

// Valid: UserTransaction injected via @Resource in a servlet (not a CDI managed bean)
@WebServlet("/transaction")
public class ValidUserTransactionUsage extends HttpServlet {

    @Resource
    private UserTransaction userTransaction;

    public void doWork() {
        // business logic using userTransaction
    }
}

// Valid: CDI bean injects UserTransaction with @Default qualifier explicitly — same rule applies,
// but kept here to confirm @Default explicit still flags as invalid (covered in invalid test).

// Valid: no CDI scope annotation — not a managed bean, no diagnostic
class UnscopedBean {

    @Inject
    private UserTransaction userTransaction;
}

// Valid: CDI bean injects UserTransaction with a custom non-default qualifier —
// this targets a different bean, not the built-in UserTransaction; spec §18.8 does not apply.
@ApplicationScoped
class BeanWithCustomQualifierInjection {

    @Inject
    @CustomTransactionQualifier
    private UserTransaction specialTransaction;
}
