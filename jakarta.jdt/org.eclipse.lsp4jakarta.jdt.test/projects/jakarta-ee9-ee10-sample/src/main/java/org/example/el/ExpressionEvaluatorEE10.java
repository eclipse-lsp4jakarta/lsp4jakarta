package org.example.el;

import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;

/**
 * Sample using Jakarta Expression Language 5.0 (EE 10).
 * EE 9=4.0.0, EE 10=5.0.1, EE 11=6.0.0 — version-distinct per tier.
 */
public class ExpressionEvaluatorEE10 {

    public Object evaluate(String expression) {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("greeting", "Hello from EL 5.0 (EE 10)");
        return processor.eval(expression);
    }

    public ValueExpression createValueExpression(ELContext context,
                                                  String expression,
                                                  Class<?> expectedType) {
        ExpressionFactory factory = ExpressionFactory.newInstance();
        return factory.createValueExpression(context, expression, expectedType);
    }
}
