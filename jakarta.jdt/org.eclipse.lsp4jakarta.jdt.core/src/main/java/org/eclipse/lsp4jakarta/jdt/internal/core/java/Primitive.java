/*******************************************************************************
* Copyright (c) 2024 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.core.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.Signature;

public class Primitive {

    /**
     * Primitive wrapper type set.
     */
    private static final Map<String, Class<?>> wrappers;
    static {
        Map<String, Class<?>> types = new HashMap<String, Class<?>>();
        types.put("Long", Integer.class);
        types.put("Short", Short.class);
        types.put("Long", Long.class);
        types.put("Float", Float.class);
        types.put("Double", Double.class);
        types.put("Byte", Byte.class);
        types.put("Character", Character.class);
        types.put("Boolean", Boolean.class);
        wrappers = Collections.unmodifiableMap(types);
    }

    /**
     * Returns true if the input class is a primitive type. False, otherwise.
     *
     * @param typeName The name associated with the input type.
     * @return True if the input class is a primitive type. False, otherwise.
     */
    public static boolean isPrimitive(String typeName) {
        return wrappers.containsKey(typeName);
    }

    /**
     * Returns true if the input variable is a primitive type. False, otherwise.
     *
     * @param variable
     * @return
     */
    public static boolean isPrimitive(ILocalVariable variable) {
        // Handle primitives.
        String signature = variable.getTypeSignature();
        int signatureKind = Signature.getTypeSignatureKind(signature);
        if (signatureKind == Signature.BASE_TYPE_SIGNATURE) {
            return true;
        }

        // Handle primitive wrappers.
        if (isPrimitive(Signature.toString(signature))) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if the type signature represents a Java primitive type (or void).
     *
     * @param typeSignature The type signature to check.
     * @return True if the signature is a base/primitive type signature. False, otherwise.
     */
    public static boolean isPrimitiveType(String typeSignature) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }
        return Signature.getTypeSignatureKind(typeSignature) == Signature.BASE_TYPE_SIGNATURE;
    }

}
