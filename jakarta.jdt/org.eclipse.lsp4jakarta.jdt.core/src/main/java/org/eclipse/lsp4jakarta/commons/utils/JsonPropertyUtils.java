/*******************************************************************************
* Copyright (c) 2025, 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.jsonb.Constants;

/**
 * Utilities for working with JSON properties and extracting/decoding values from its attribute, annotation etc.
 */
public class JsonPropertyUtils {

    /**
     * @param propertyName
     * @return String
     * @description Method decodes unicode property name value to string value
     */
    public static String decodeUniCodeName(String propertyName) {
        Pattern pattern = Pattern.compile(Constants.JSONB_PROPERTYNAME_UNICODE); // Pattern for detecting unicode sequence
        Matcher matcher = pattern.matcher(propertyName);
        StringBuffer decoded = new StringBuffer();
        while (matcher.find()) {
            String unicode = matcher.group(1);
            char decodedChar = (char) Integer.parseInt(unicode, 16);
            matcher.appendReplacement(decoded, Character.toString(decodedChar));
        }
        matcher.appendTail(decoded);
        return decoded.toString();
    }

    /**
     * @param annotation
     * @return String
     * @throws JavaModelException
     * @description Method extracts property name value from the annotation
     */
    public static String extractPropertyNameFromJsonField(IAnnotation annotation) throws JavaModelException {
        for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
            if (pair.getValue() instanceof String) {
                return (String) pair.getValue();
            }
        }
        return null;
    }

    /**
     * @param type
     * @param annotation
     * @return
     * @throws JavaModelException
     * @description Method returns true if elements matches one of the JSONB annotations are present.
     */
    public static boolean isJsonbType(IType type, IAnnotation annotation) throws JavaModelException {
        String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type, annotation.getElementName(),
                                                                             Constants.JSONB_ANNOTATIONS.toArray(String[]::new));
        return matchedAnnotation != null;
    }
}