/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Adit Rada, Yijia Jing - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.test.jsonb;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.test.core.BaseJakartaTest;
import org.junit.Test;

import com.google.gson.Gson;

public class JsonbDiagnosticsCollectorTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void deleteExtraJsonbCreatorAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/jsonb/ExtraJsonbCreatorAnnotations.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(18, 11, 39,
                          "Only one constructor or static factory method can be annotated with @JsonbCreator in a given class.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidNumerOfJsonbCreatorAnnotationsInClass");

        Diagnostic d2 = d(21, 48, 61,
                          "Only one constructor or static factory method can be annotated with @JsonbCreator in a given class.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidNumerOfJsonbCreatorAnnotationsInClass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        // test code actions
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(17, 4, 18, 4, "");
        CodeAction ca1 = ca(uri, "Remove @JsonbCreator", d1, te1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(20, 4, 21, 4, "");
        CodeAction ca2 = ca(uri, "Remove @JsonbCreator", d2, te2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca2);
    }

    @Test
    public void JsonbTransientNotMutuallyExclusive() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/jsonb/JsonbTransientDiagnostic.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Diagnostic for the field "id"
        Diagnostic d1 = d(21, 16, 18,
                          "When a class field is annotated with @JsonbTransient, this field, getter or setter must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnField");
        d1.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbTransient")));

        // Diagnostic for the field "name"
        Diagnostic d2 = d(25, 19, 23,
                          "When a class field is annotated with @JsonbTransient, this field, getter or setter must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnField");
        d2.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty", "jakarta.json.bind.annotation.JsonbTransient")));

        // Diagnostic for the field "favoriteLanguage"
        Diagnostic d3 = d(30, 19, 35,
                          "When a class field is annotated with @JsonbTransient, this field, getter or setter must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnField");
        d3.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty", "jakarta.json.bind.annotation.JsonbAnnotation",
                                                       "jakarta.json.bind.annotation.JsonbTransient")));

        // Diagnostic for the field "favoriteEditor"
        Diagnostic d4 = d(39, 19, 33,
                          "When an accessor is annotated with @JsonbTransient, its field or the accessor must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnAccessor");
        d4.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        // Diagnostic for the getter "getId"
        Diagnostic d5 = d(42, 16, 21,
                          "When a class field is annotated with @JsonbTransient, this field, getter or setter must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnField");
        d5.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        // Diagnostic for the setter "setId"
        Diagnostic d6 = d(49, 17, 22,
                          "When a class field is annotated with @JsonbTransient, this field, getter or setter must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnField");
        d6.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbAnnotation")));

        // Diagnostic for the getter "getFavoriteEditor"
        Diagnostic d7 = d(67, 19, 36,
                          "When an accessor is annotated with @JsonbTransient, its field or the accessor must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnAccessor");
        d7.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbTransient")));

        // Diagnostic for the setter "setFavoriteEditor"
        Diagnostic d8 = d(74, 17, 34,
                          "When an accessor is annotated with @JsonbTransient, its field or the accessor must not be annotated with other JSON Binding annotations.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidJSonBindindAnnotationWithJsonbTransientOnAccessor");
        d8.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbAnnotation", "jakarta.json.bind.annotation.JsonbTransient")));

        Diagnostic d9 = d(79, 19, 25,
                          "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d9.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        Diagnostic d10 = d(82, 19, 25,
                           "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                           DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d10.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6, d7, d8, d9, d10);

        // Test code actions
        // Quick fix for the field "id"
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(20, 4, 21, 4, "");
        CodeAction ca1 = ca(uri, "Remove @JsonbTransient", d1, te1);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1);

        // Quick fix for the field "name"
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te3 = te(24, 4, 25, 4, "");
        TextEdit te4 = te(23, 4, 24, 4, "");
        CodeAction ca3 = ca(uri, "Remove @JsonbTransient", d2, te3);
        CodeAction ca4 = ca(uri, "Remove @JsonbProperty", d2, te4);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca4, ca3);

        // Quick fix for the field "favoriteLanguage"
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);
        TextEdit te5 = te(29, 4, 30, 4, "");
        TextEdit te6 = te(27, 4, 29, 4, "");
        CodeAction ca5 = ca(uri, "Remove @JsonbTransient", d3, te5);
        CodeAction ca6 = ca(uri, "Remove @JsonbProperty, @JsonbAnnotation", d3, te6);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca6, ca5);

        // Quick fix for the accessor "getId"
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, d5);
        TextEdit te7 = te(41, 4, 42, 4, "");
        CodeAction ca7 = ca(uri, "Remove @JsonbProperty", d5, te7);
        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, ca7);

        // Quick fix for the accessor "setId"
        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d6);
        TextEdit te8 = te(48, 4, 49, 4, "");
        CodeAction ca8 = ca(uri, "Remove @JsonbAnnotation", d6, te8);
        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, ca8);
    }

    @Test
    public void JsonbPropertyUniquenessSubClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/jsonb/JsonbTransientDiagnosticSubClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(11, 19, 36,
                          "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d1.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        Diagnostic d2 = d(17, 19, 34,
                          "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d2.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        Diagnostic d3 = d(20, 19, 34,
                          "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d3.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);
    }

    @Test
    public void JsonbPropertyUniquenessSubSubClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/jsonb/JsonbTransientDiagnosticSubSubClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(8, 19, 31,
                          "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d1.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        Diagnostic d2 = d(11, 19, 36,
                          "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d2.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        Diagnostic d3 = d(14, 19, 37,
                          "Multiple fields or properties with @JsonbProperty must not have JSON members with duplicate names, the member names must be unique.",
                          DiagnosticSeverity.Error, "jakarta-jsonb", "InvalidPropertyNamesOnJsonbFields");
        d3.setData(new Gson().toJsonTree(Arrays.asList("jakarta.json.bind.annotation.JsonbProperty")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);
    }
}