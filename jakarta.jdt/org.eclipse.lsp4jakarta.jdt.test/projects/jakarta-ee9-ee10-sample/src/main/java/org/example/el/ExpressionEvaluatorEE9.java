package org.example.el;

import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;

/**
 * Sample using Jakarta Expression Language 4.0 (EE 9).
 * EE 9=4.0.0, EE 10=5.0.1, EE 11=6.0.0 — version-distinct per tier.
 */
public class ExpressionEvaluatorEE9 {

    public Object evaluate(String expression, Class<?> expectedType) {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("greeting", "Hello from EL 4.0 (EE 9)");
        return processor.eval(expression);
    }

    public ValueExpression createValueExpression(ELContext context,
                                                  String expression,
                                                  Class<?> expectedType) {
        ExpressionFactory factory = ExpressionFactory.newInstance();
        return factory.createValueExpression(context, expression, expectedType);
    }
}
