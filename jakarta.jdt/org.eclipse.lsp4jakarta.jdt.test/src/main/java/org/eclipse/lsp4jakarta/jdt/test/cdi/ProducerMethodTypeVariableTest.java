/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.test.cdi;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.test.core.BaseJakartaTest;
import org.junit.Test;

/**
 * Tests for CDI producer method type variable diagnostics.
 *
 * CDI 3.0 spec section 3.2:
 *
 * Rule 1 — bare type variable is always a definition error:
 * A producer method whose return type is a type variable (T) or an array whose
 * component type is a type variable (T[]) is always invalid.
 *
 * Rule 2 — parameterized type with type variable requires @Dependent scope:
 * A producer method returning List<T>, Map<K,V>, etc. must use @Dependent scope.
 * Any other scope is a definition error.
 */
public class ProducerMethodTypeVariableTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void producerMethodTypeVariableDiagnostics() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ProducerMethodTypeVariable.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Rule 2 method: List<T> with @ApplicationScoped (line 50, 0-based 49)
        Diagnostic listWithApplicationScope = d(49, 19, 50,
                                                "A producer method with a parameterized return type that contains a type variable must have scope @Dependent.",
                                                DiagnosticSeverity.Error, "jakarta-cdi",
                                                "InvalidProducerMethodWithTypeVariableAndNonDependentScope");

        // Rule 2 method: Map<String, T> with @RequestScoped (line 57, 0-based 56)
        Diagnostic mapWithRequestScope = d(56, 26, 52,
                                           "A producer method with a parameterized return type that contains a type variable must have scope @Dependent.",
                                           DiagnosticSeverity.Error, "jakarta-cdi",
                                           "InvalidProducerMethodWithTypeVariableAndNonDependentScope");

        // Rule 1 method: bare type variable T (line 84, 0-based 83)
        Diagnostic bareTypeVariable = d(83, 13, 36,
                                        "A producer method return type must not be a type variable or an array whose component type is a type variable.",
                                        DiagnosticSeverity.Error, "jakarta-cdi",
                                        "InvalidProducerMethodWithBareTypeVariableReturnType");

        // Rule 1 method: array of type variable T[] (line 90, 0-based 89)
        Diagnostic bareTypeVariableArray = d(89, 15, 43,
                                             "A producer method return type must not be a type variable or an array whose component type is a type variable.",
                                             DiagnosticSeverity.Error, "jakarta-cdi",
                                             "InvalidProducerMethodWithBareTypeVariableReturnType");

        // Rule 2 field: List<T> with @ApplicationScoped (line 112, 0-based 111)
        Diagnostic fieldListWithApplicationScope = d(111, 12, 49,
                                                     "A producer field with a parameterized type that contains a type variable must have scope @Dependent.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi",
                                                     "InvalidProducerFieldWithTypeVariableAndNonDependentScope");

        // Rule 2 field: Map<String, T> with @RequestScoped (line 117, 0-based 116)
        Diagnostic fieldMapWithRequestScope = d(116, 19, 51,
                                                "A producer field with a parameterized type that contains a type variable must have scope @Dependent.",
                                                DiagnosticSeverity.Error, "jakarta-cdi",
                                                "InvalidProducerFieldWithTypeVariableAndNonDependentScope");

        // Rule 1 field: bare type variable T (line 138, 0-based 137)
        Diagnostic fieldBareTypeVariable = d(137, 6, 35,
                                             "A producer field type must not be a type variable or an array whose component type is a type variable.",
                                             DiagnosticSeverity.Error, "jakarta-cdi",
                                             "InvalidProducerFieldWithBareTypeVariableType");

        // Rule 1 field: array of type variable T[] (line 142, 0-based 141)
        Diagnostic fieldBareTypeVariableArray = d(141, 8, 42,
                                                  "A producer field type must not be a type variable or an array whose component type is a type variable.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi",
                                                  "InvalidProducerFieldWithBareTypeVariableType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              listWithApplicationScope, mapWithRequestScope,
                              bareTypeVariable, bareTypeVariableArray,
                              fieldListWithApplicationScope, fieldMapWithRequestScope,
                              fieldBareTypeVariable, fieldBareTypeVariableArray);
    }
}
