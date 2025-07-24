/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.commons;

/**
 * Represents the context where the cursor is in a Java file.
 */
public enum JavaCursorContextKind {

    /**
     * The cursor is in a file that does not have a root type declaration.
     *
     * eg.
     * <code><br />
     * |<br />
     * </code>
     */
    IN_EMPTY_FILE(1),

    /**
     * The cursor is before a type declaration body, either at the root of a file or
     * within another class. The cursor is before any annotations on the type.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * |<br />
     * &commat;Inject<br />
     * public class MyClass {<br />
     * <br />
     * }<br />
     * </code>
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * &commat;Inject<br />
     * public class MyClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;private int memberVariable;<br />
     * &emsp;&emsp;&emsp;&emsp;|<br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Inject<br />
     * &emsp;&emsp;&emsp;&emsp;public static class MyChildClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;}<br />
     * }<br />
     * </code>
     */
    BEFORE_CLASS(2),

    /**
     * The cursor is in a type declaration body, and the next declaration in the
     * body is a method declaration. The cursor is before any annotations on the
     * method.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * public class MyClass {<br />
     * &emsp;&emsp;&emsp;&emsp;|<br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Deprecated<br />
     * &emsp;&emsp;&emsp;&emsp;public void myMethod() {<br />
     * &emsp;&emsp;&emsp;&emsp;}<br />
     * }<br />
     * </code>
     */
    BEFORE_METHOD(3),

    /**
     * The cursor is in a type declaration body, and the next declaration in the
     * body is a field declaration. The cursor is before any annotations on the
     * field.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * public class MyClass {<br />
     * &emsp;&emsp;&emsp;&emsp;|<br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Deprecated<br />
     * &emsp;&emsp;&emsp;&emsp;public String myString;<br />
     * }<br />
     * </code>
     */
    BEFORE_FIELD(4),

    /**
     * The cursor is before a type declaration body, either at the root of a file or
     * within another class. The cursor is somewhere within the annotation
     * declarations on the class.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * &commat;Inject|<br />
     * public class MyClass {<br />
     * <br />
     * }<br />
     * </code>
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * &commat;Inject<br />
     * public class MyClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;private int memberVariable;<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Inject|<br />
     * &emsp;&emsp;&emsp;&emsp;public static class MyChildClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;}<br />
     * }<br />
     * </code>
     */
    IN_CLASS_ANNOTATIONS(5),

    /**
     * The cursor is in a type declaration body, and the next declaration in the
     * body is a method declaration. The cursor is somewhere within the annotation
     * declarations on the method.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * public class MyClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Deprecated|<br />
     * &emsp;&emsp;&emsp;&emsp;public void myMethod() {<br />
     * &emsp;&emsp;&emsp;&emsp;}<br />
     * }<br />
     * </code>
     */
    IN_METHOD_ANNOTATIONS(6),

    /**
     * The cursor is in a type declaration body, and the next declaration in the
     * body is a field declaration. The cursor is somewhere within the annotation
     * declarations on the field.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * public class MyClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Deprecated|<br />
     * &emsp;&emsp;&emsp;&emsp;public String myString;<br />
     * }<br />
     * </code>
     */
    IN_FIELD_ANNOTATIONS(7),

    /**
     * The cursor is in a type declaration body, after all the declarations for the
     * type.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * public class MyClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Deprecated<br />
     * &emsp;&emsp;&emsp;&emsp;public String myString;<br />
     * &emsp;&emsp;&emsp;&emsp;|<br />
     * }<br />
     * </code>
     */
    IN_CLASS(8),

    /**
     * None of the above context apply.
     *
     * eg.
     * <code>
     * <br />
     * package org.acme;<br />
     * <br />
     * public class MyClass {<br />
     * <br />
     * &emsp;&emsp;&emsp;&emsp;&commat;Deprecated<br />
     * &emsp;&emsp;&emsp;&emsp;public void myMethod() {<br />
     * &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|<br />
     * &emsp;&emsp;&emsp;&emsp;}<br />
     * }<br />
     * </code>
     */
    NONE(2000);

    private final int value;

    private JavaCursorContextKind(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static JavaCursorContextKind forValue(int value) {
        JavaCursorContextKind[] allValues = JavaCursorContextKind.values();
		
        // Root cause for https://github.com/eclipse-lsp4jakarta/lsp4jakarta/issues/520.
		// See issue for more details.		
		if (value == NONE.getValue()) {
			return NONE;
		} else {
			if (value < 1 || value > allValues.length)
				throw new IllegalArgumentException("Illegal enum value: " + value);
			return allValues[value - 1];
		}
    }
}
