/*******************************************************************************
* Copyright (c) 2021, 2026 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.test.persistence;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;

import com.google.gson.JsonArray;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
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

public class JakartaPersistenceTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void deleteMapKeyOrMapKeyClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyAndMapKeyClassTogether.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(21, 32, 42,
                          "@MapKeyClass and @MapKey annotations cannot be used on the same method.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyAnnotationsOnSameMethod");

        Diagnostic d2 = d(16, 25, 32,
                          "@MapKeyClass and @MapKey annotations cannot be used on the same field or property.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyAnnotationsOnSameField");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);

        TextEdit te1 = te(20, 4, 21, 4, "");
        TextEdit te2 = te(19, 4, 20, 4, "");
        CodeAction ca1 = ca(uri, "Remove @MapKeyClass", d1, te1);
        CodeAction ca2 = ca(uri, "Remove @MapKey", d1, te2);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca2, ca1);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);

        TextEdit te3 = te(14, 13, 15, 27, "");
        TextEdit te4 = te(14, 4, 15, 4, "");
        CodeAction ca3 = ca(uri, "Remove @MapKeyClass", d2, te3);
        CodeAction ca4 = ca(uri, "Remove @MapKey", d2, te4);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca4, ca3);
    }

    @Test
    public void completeMapKeyJoinColumnAnnotation() throws Exception {

        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MultipleMapKeyAnnotations.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test diagnostics are present
        Diagnostic d1 = d(16, 25, 30,
                          "A field with multiple @MapKeyJoinColumn annotations must specify both the name and referencedColumnName attributes in the corresponding @MapKeyJoinColumn annotations.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidFieldWithMultipleMPJCAnnotations");
        Diagnostic d2 = d(16, 25, 30,
                          "A field with multiple @MapKeyJoinColumn annotations must specify both the name and referencedColumnName attributes in the corresponding @MapKeyJoinColumn annotations.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidFieldWithMultipleMPJCAnnotations");

        Diagnostic d3 = d(20, 25, 30,
                          "A field with multiple @MapKeyJoinColumn annotations must specify both the name and referencedColumnName attributes in the corresponding @MapKeyJoinColumn annotations.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidFieldWithMultipleMPJCAnnotations");
        Diagnostic d4 = d(20, 25, 30,
                          "A field with multiple @MapKeyJoinColumn annotations must specify both the name and referencedColumnName attributes in the corresponding @MapKeyJoinColumn annotations.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidFieldWithMultipleMPJCAnnotations");

        Diagnostic d5 = d(24, 25, 30,
                          "A field with multiple @MapKeyJoinColumn annotations must specify both the name and referencedColumnName attributes in the corresponding @MapKeyJoinColumn annotations.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidFieldWithMultipleMPJCAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5);

        // test quick fixes
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(14, 4, 15, 23,
                          "@MapKeyJoinColumn(name = \"\", referencedColumnName = \"\")\n\t@MapKeyJoinColumn(name = \"\", referencedColumnName = \"\")");
        CodeAction ca1 = ca(uri, "Insert the missing attributes to the @MapKeyJoinColumn annotation", d1, te1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d3);
        TextEdit te2 = te(18, 4, 19, 52,
                          "@MapKeyJoinColumn(referencedColumnName = \"rcn2\", name = \"\")\n\t@MapKeyJoinColumn(name = \"n1\", referencedColumnName = \"\")");
        CodeAction ca2 = ca(uri, "Insert the missing attributes to the @MapKeyJoinColumn annotation", d3, te2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca2);

        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d5);
        TextEdit te3 = te(22, 4, 23, 23,
                          "@MapKeyJoinColumn(name = \"\", referencedColumnName = \"\")\n\t@MapKeyJoinColumn(name = \"n1\", referencedColumnName = \"rcn1\")");
        CodeAction ca3 = ca(uri, "Insert the missing attributes to the @MapKeyJoinColumn annotation",
                            d5, te3);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca3);
    }

    @Test
    public void addEmptyConstructor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMissingConstructor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test diagnostics are present
        Diagnostic d = d(6, 13, 37,
                         "A class using the @Entity annotation must contain a public or protected constructor with no arguments.",
                         DiagnosticSeverity.Error, "jakarta-persistence", "InvalidConstructorInEntityAnnotatedClass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);

        // test quick fixes
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d);
        TextEdit te1 = te(8, 1, 8, 1, "protected EntityMissingConstructor() {\n\t}\n\n\t");
        CodeAction ca1 = ca(uri, "Add a no-arg protected constructor to this class", d, te1);
        TextEdit te2 = te(8, 1, 8, 1, "public EntityMissingConstructor() {\n\t}\n\n\t");
        CodeAction ca2 = ca(uri, "Add a no-arg public constructor to this class", d, te2);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);
    }

    @Test
    public void removeFinalModifiers() throws Exception {

        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/FinalModifiers.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test diagnostics are present
        Diagnostic d1 = d(14, 21, 28,
                          "A class using the @Entity annotation cannot contain any methods that are declared final.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidFinalMethodInEntityAnnotatedClass");
        d1.setData(IJavaElement.METHOD);

        Diagnostic d2 = d(11, 14, 15,
                          "A class using the @Entity annotation cannot contain any persistent instance variables that are declared final.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidPersistentFieldInEntityAnnotatedClass");
        d2.setData(IJavaElement.FIELD);

        Diagnostic d3 = d(12, 17, 18,
                          "A class using the @Entity annotation cannot contain any persistent instance variables that are declared final.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidPersistentFieldInEntityAnnotatedClass");
        d3.setData(IJavaElement.FIELD);

        Diagnostic d4 = d(12, 30, 31,
                          "A class using the @Entity annotation cannot contain any persistent instance variables that are declared final.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidPersistentFieldInEntityAnnotatedClass");
        d4.setData(IJavaElement.FIELD);

        Diagnostic d5 = d(6, 19, 33,
                          "A class using the @Entity annotation must not be final.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidFinalModifierOnEntityAnnotatedClass");
        d5.setData(IJavaElement.TYPE);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5);

        // test quick fixes
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(14, 10, 14, 16, "");
        CodeAction ca1 = ca(uri, "Remove the 'final' modifier", d1, te1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(11, 4, 11, 10, "");
        CodeAction ca2 = ca(uri, "Remove the 'final' modifier", d2, te2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca2);

        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);
        TextEdit te3 = te(12, 4, 12, 10, "");
        CodeAction ca3 = ca(uri, "Remove the 'final' modifier", d3, te3);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca3);

        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, d4);
        TextEdit te4 = te(12, 4, 12, 10, "");
        CodeAction ca4 = ca(uri, "Remove the 'final' modifier", d4, te4);

        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, ca4);

        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d5);
        TextEdit te5 = te(6, 6, 6, 12, "");
        CodeAction ca5 = ca(uri, "Remove the 'final' modifier", d5, te5);

        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, ca5);
    }

    @Test
    public void testMethodOrFieldType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyAnnotationsType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(27, 19, 25,
                          "`@MapKey` annotation can only be applied to methods with a return type of java.util.Map.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidReturnTypeOfMethod");

        Diagnostic d2 = d(13, 11, 15,
                          "`@MapKey` annotation can only be applied to fields of type java.util.Map.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidTypeOfField");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);
    }

    @Test
    public void testAccessorAndNamingConventions() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyAnnotationsGetterConvention.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(37, 33, 41,
                          "Method is not public and may not be accessible as expected.",
                          DiagnosticSeverity.Warning, "jakarta-persistence", "InvalidMethodAccessSpecifier");

        Diagnostic d2 = d(42, 33, 41,
                          "This method does not conform to persistent property getter naming conventions.",
                          DiagnosticSeverity.Warning, "jakarta-persistence", "InvalidMethodName");

        Diagnostic d3 = d(47, 32, 42,
                          "Method has no matching field name.",
                          DiagnosticSeverity.Warning, "jakarta-persistence", "InvalidMapKeyAnnotationsFieldNotFound");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);
    }

    @Test
    public void testIdDateMissingTemporal() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityIdDateMissingTemporal.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(11, 14, 16,
                          "A field or property marked with @Id and of type java.util.Date must explicitly specify @Temporal(TemporalType.DATE).",
                          DiagnosticSeverity.Error, "jakarta-persistence", "MissingTemporalAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);

        // test quick fix
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit dateMissingTemporalTE = te(5, 30, 10, 1,
                                            "\nimport jakarta.persistence.Temporal;\nimport jakarta.persistence.TemporalType;\n\n@Entity\npublic class EntityIdDateMissingTemporal {\n\n	@Temporal(value = TemporalType.DATE)\n	");
        CodeAction dateMissingTemporalCA = ca(uri, "Insert @Temporal(value = TemporalType.DATE)", d1, dateMissingTemporalTE);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, dateMissingTemporalCA);
    }

    @Test
    public void testPropertyIdDateMissingTemporal() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityPropertyIdDateMissingTemporal.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(13, 13, 18,
                          "A field or property marked with @Id and of type java.util.Date must explicitly specify @Temporal(TemporalType.DATE).",
                          DiagnosticSeverity.Error, "jakarta-persistence", "MissingTemporalAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);

        // test quick fix
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit dateMissingTemporalTE = te(5, 30, 12, 1,
                                            "\nimport jakarta.persistence.Temporal;\nimport jakarta.persistence.TemporalType;\n\n@Entity\npublic class EntityPropertyIdDateMissingTemporal {\n\n	private Date pk;\n\n	@Temporal(value = TemporalType.DATE)\n	");
        CodeAction dateMissingTemporalCA = ca(uri, "Insert @Temporal(value = TemporalType.DATE)", d1, dateMissingTemporalTE);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, dateMissingTemporalCA);
    }

    @Test
    public void testIdDateInvalidTemporalType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityInvalidTemporalType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(13, 1, 29,
                          "The @Temporal annotation on a field or property annotated with @Id and of type java.util.Date must specify TemporalType.DATE.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidValueInTemporalAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);

        // test quick fix
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit invalidTemporalTypeTE = te(13, 11, 13, 28, "TemporalType.DATE");
        CodeAction invalidTemporalTypeCA = ca(uri, "Change @Temporal value to TemporalType.DATE", d1, invalidTemporalTypeTE);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, invalidTemporalTypeCA);
    }

    @Test
    public void testPropertyIdDateInvalidTemporalType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityPropertyInvalidTemporalType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(15, 1, 29,
                          "The @Temporal annotation on a field or property annotated with @Id and of type java.util.Date must specify TemporalType.DATE.",
                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidValueInTemporalAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);

        // test quick fix
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit invalidTemporalTypeTE = te(15, 11, 15, 28, "TemporalType.DATE");
        CodeAction invalidTemporalTypeCA = ca(uri, "Change @Temporal value to TemporalType.DATE", d1, invalidTemporalTypeTE);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, invalidTemporalTypeCA);
    }

    @Test
    public void testMissingPrimaryKey() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMissingPrimaryKey.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(5, 13, 36,
                         "The class EntityMissingPrimaryKey annotated with @Entity must define a primary key using @Id or @EmbeddedId.",
                         DiagnosticSeverity.Error, "jakarta-persistence", "MissingPrimaryKey");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    @Test
    public void testEntityWithEmbeddedId() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityWithEmbeddedId.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for an entity with @EmbeddedId
        // This confirms that @EmbeddedId is correctly recognized as a primary key
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testEntityWithIdOnGetter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityWithIdOnGetter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for an entity with @Id on getter method
        // This confirms that @Id on getter methods is correctly recognized as a primary key
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testEntityWithEmbeddedIdOnGetter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityWithEmbeddedIdOnGetter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for an entity with @EmbeddedId on getter method
        // This confirms that @EmbeddedId on getter methods is correctly recognized as a primary key
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testEntityWithMappedSuperclass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityWithMappedSuperclass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for an entity that inherits @Id from @MappedSuperclass
        // This confirms that primary keys in @MappedSuperclass are correctly recognized
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testEntityWithMappedSuperclassIdOnGetter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityWithMappedSuperclassIdOnGetter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for an entity that inherits @Id on getter from @MappedSuperclass
        // This confirms that primary keys on getter methods in @MappedSuperclass are correctly recognized
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testDuplicateVersionInClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/DuplicateVersionInClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic duplicateVersionD1 = d(10, 16, 24,
                                          "Multiple fields or properties are annotated with @Version. Only one @Version annotation is allowed per entity class.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateVersionAnnotationInClass");

        Diagnostic duplicateVersionD2 = d(13, 16, 24,
                                          "Multiple fields or properties are annotated with @Version. Only one @Version annotation is allowed per entity class.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateVersionAnnotationInClass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, duplicateVersionD1, duplicateVersionD2);
    }

    @Test
    public void testVersionInHierarchy() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/VersionInHierarchyChild.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic versionInHierarchyD1 = d(10, 16, 28,
                                            "The @Version annotation is already present in the parent entity class. Only one @Version annotation is allowed in the entity hierarchy.",
                                            DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateVersionAnnotationInHierarchy");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, versionInHierarchyD1);
    }

    @Test
    public void testDuplicateVersionOnMethods() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/DuplicateVersionOnMethods.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic duplicateVersionOnMethodsD1 = d(19, 15, 26,
                                                   "Multiple fields or properties are annotated with @Version. Only one @Version annotation is allowed per entity class.",
                                                   DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateVersionAnnotationInClass");

        Diagnostic duplicateVersionOnMethodsD2 = d(28, 15, 26,
                                                   "Multiple fields or properties are annotated with @Version. Only one @Version annotation is allowed per entity class.",
                                                   DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateVersionAnnotationInClass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, duplicateVersionOnMethodsD1, duplicateVersionOnMethodsD2);
    }

    @Test
    public void testVersionInHierarchyOnMethods() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/VersionInHierarchyChildMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic versionInHierarchyOnMethodsD1 = d(18, 15, 30,
                                                     "The @Version annotation is already present in the parent entity class. Only one @Version annotation is allowed in the entity hierarchy.",
                                                     DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateVersionAnnotationInHierarchy");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, versionInHierarchyOnMethodsD1);
    }

    @Test
    public void testVersionInvalidStringType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/version/VersionInvalidString.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic invalidStringD1 = d(12, 19, 26,
                                       "A field or property annotated with @Version must be of type int, Integer, short, Short, long, Long, or java.sql.Timestamp.",
                                       DiagnosticSeverity.Error, "jakarta-persistence", "InvalidVersionFieldOrPropertyType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, invalidStringD1);
    }

    @Test
    public void testVersionValidIntType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/version/VersionValidInt.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for valid int type
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testVersionValidTimestampType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/version/VersionValidTimestamp.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for valid java.sql.Timestamp type
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testVersionMethodInvalidStringType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/version/VersionMethodInvalidString.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic invalidStringD1 = d(14, 18, 28,
                                       "A field or property annotated with @Version must be of type int, Integer, short, Short, long, Long, or java.sql.Timestamp.",
                                       DiagnosticSeverity.Error, "jakarta-persistence", "InvalidVersionFieldOrPropertyType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, invalidStringD1);
    }

    @Test
    public void testVersionMethodValidLongType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/version/VersionMethodValidLong.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for valid Long return type
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testMapKeyTemporalValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyTemporalValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for valid @MapKeyTemporal usage
        // with Date and Calendar map key types (including FQN)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testMapKeyTemporalInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyTemporalInvalid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Invalid: @MapKeyTemporal on String map key
        Diagnostic mapKeyTemporalOnStringD1 = d(18, 32, 44,
                                                "@MapKeyTemporal can only be used when the map key type is java.util.Date or java.util.Calendar.",
                                                DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyTemporalOnNonTemporalType");

        // Invalid: @MapKeyTemporal on Integer map key
        Diagnostic mapKeyTemporalOnIntegerD2 = d(23, 33, 46,
                                                 "@MapKeyTemporal can only be used when the map key type is java.util.Date or java.util.Calendar.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyTemporalOnNonTemporalType");

        // Invalid: @MapKeyTemporal on Long map key
        Diagnostic mapKeyTemporalOnLongD3 = d(28, 30, 40,
                                              "@MapKeyTemporal can only be used when the map key type is java.util.Date or java.util.Calendar.",
                                              DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyTemporalOnNonTemporalType");

        // Invalid: @MapKeyTemporal on getter with String map key
        Diagnostic mapKeyTemporalOnGetterD4 = d(33, 31, 46,
                                                "@MapKeyTemporal can only be used when the map key type is java.util.Date or java.util.Calendar.",
                                                DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyTemporalOnNonTemporalType");

        // Invalid: @MapKeyTemporal on FQN String map key (should still be detected)
        Diagnostic mapKeyTemporalOnFqnStringD5 = d(44, 42, 57,
                                                   "@MapKeyTemporal can only be used when the map key type is java.util.Date or java.util.Calendar.",
                                                   DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyTemporalOnNonTemporalType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, mapKeyTemporalOnStringD1, mapKeyTemporalOnIntegerD2,
                              mapKeyTemporalOnLongD3, mapKeyTemporalOnGetterD4, mapKeyTemporalOnFqnStringD5);

        // Test the quickfix for removing @MapKeyTemporal
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, mapKeyTemporalOnStringD1);
        TextEdit te = te(17, 4, 18, 4, "");
        CodeAction ca = ca(uri, "Remove @MapKeyTemporal", mapKeyTemporalOnStringD1, te);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);
    }

    @Test
    public void testInvalidIdType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/InvalidIdType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Invalid: Custom class type (line 18: private CustomType customId;)
        Diagnostic customTypeD1 = d(17, 23, 31,
                                    "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                    DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: UUID type (line 22: private UUID uuidId;)
        Diagnostic uuidTypeD2 = d(21, 17, 23,
                                  "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                  DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: Collection type (line 26: private List<String> listId;)
        Diagnostic listTypeD3 = d(25, 25, 31,
                                  "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                  DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: Set type (line 29: private Set<String> setId;)
        Diagnostic setTypeD4 = d(28, 24, 29,
                                 "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                 DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: Map type (line 32: private Map<String, String> mapId;)
        Diagnostic mapTypeD5 = d(31, 32, 37,
                                 "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                 DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: Object type (line 36: private Object objectId;)
        Diagnostic objectTypeD6 = d(35, 19, 27,
                                    "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                    DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: Array type (line 40: private int[] arrayId;)
        Diagnostic arrayTypeD7 = d(39, 18, 25,
                                   "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                   DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: Getter with CustomType return type (line 51: public CustomType getCustomId())
        Diagnostic customTypeGetterD8 = d(50, 22, 33,
                                          "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        // Invalid: Getter with UUID return type (line 57: public UUID getUuidId())
        Diagnostic uuidTypeGetterD9 = d(56, 16, 25,
                                        "The @Id annotation must use a valid identifier type (primitives, wrapper types, String, Date types, BigDecimal, or BigInteger).",
                                        DiagnosticSeverity.Error, "jakarta-persistence", "InvalidIdType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, customTypeD1, uuidTypeD2, listTypeD3,
                              setTypeD4, mapTypeD5, objectTypeD6, arrayTypeD7, customTypeGetterD8, uuidTypeGetterD9);
    }

    @Test
    public void testValidIdTypes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/ValidIdTypes.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for valid @Id types
        // This includes primitives, wrapper types, String, Date types, BigDecimal, and BigInteger
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testMultipleEmbeddedId() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMultipleEmbeddedId.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic firstEmbeddedIdDiagnostic = d(9, 25, 28,
                                                 "An entity must not declare more than one @EmbeddedId annotation.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "MultipleEmbeddedIdAnnotations");

        Diagnostic secondEmbeddedIdDiagnostic = d(12, 25, 28,
                                                  "An entity must not declare more than one @EmbeddedId annotation.",
                                                  DiagnosticSeverity.Error, "jakarta-persistence", "MultipleEmbeddedIdAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, firstEmbeddedIdDiagnostic, secondEmbeddedIdDiagnostic);

        // Quick fix: Remove @EmbeddedId from id1
        JakartaJavaCodeActionParams removeFirstEmbeddedIdParams = createCodeActionParams(uri, firstEmbeddedIdDiagnostic);
        TextEdit removeFirstEmbeddedIdEdit = te(8, 4, 9, 4, "");
        CodeAction removeFirstEmbeddedIdAction = ca(uri, "Remove @EmbeddedId", firstEmbeddedIdDiagnostic, removeFirstEmbeddedIdEdit);
        assertJavaCodeAction(removeFirstEmbeddedIdParams, IJDT_UTILS, removeFirstEmbeddedIdAction);

        // Quick fix: Remove @EmbeddedId from id2
        JakartaJavaCodeActionParams removeSecondEmbeddedIdParams = createCodeActionParams(uri, secondEmbeddedIdDiagnostic);
        TextEdit removeSecondEmbeddedIdEdit = te(11, 4, 12, 4, "");
        CodeAction removeSecondEmbeddedIdAction = ca(uri, "Remove @EmbeddedId", secondEmbeddedIdDiagnostic, removeSecondEmbeddedIdEdit);
        assertJavaCodeAction(removeSecondEmbeddedIdParams, IJDT_UTILS, removeSecondEmbeddedIdAction);
    }

    @Test
    public void testMixedIdentifiers() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMixedIdentifiers.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Diagnostic on @EmbeddedId field (compositeId)
        Diagnostic mixedEmbeddedIdDiagnostic = d(13, 25, 36,
                                                 "@EmbeddedId cannot be combined with @Id in the same entity.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        // Diagnostic on @Id field (id)
        Diagnostic mixedIdDiagnostic = d(10, 17, 19,
                                         "@Id cannot be combined with @EmbeddedId in the same entity.",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, mixedEmbeddedIdDiagnostic, mixedIdDiagnostic);

        // Quick fix on @EmbeddedId field (compositeId): only offers Remove @EmbeddedId
        // because the field only has @EmbeddedId — @Id is on a different field
        JakartaJavaCodeActionParams embeddedIdFieldParams = createCodeActionParams(uri, mixedEmbeddedIdDiagnostic);
        TextEdit removeEmbeddedIdFromCompositeEdit = te(12, 4, 13, 4, "");
        CodeAction removeEmbeddedIdFromCompositeAction = ca(uri, "Remove @EmbeddedId", mixedEmbeddedIdDiagnostic, removeEmbeddedIdFromCompositeEdit);
        assertJavaCodeAction(embeddedIdFieldParams, IJDT_UTILS, removeEmbeddedIdFromCompositeAction);

        // Quick fix on @Id field (id): only offers Remove @Id
        // because the field only has @Id — @EmbeddedId is on a different field
        JakartaJavaCodeActionParams idFieldParams = createCodeActionParams(uri, mixedIdDiagnostic);
        TextEdit removeIdFromIdEdit = te(9, 4, 10, 4, "");
        CodeAction removeIdFromIdAction = ca(uri, "Remove @Id", mixedIdDiagnostic, removeIdFromIdEdit);
        assertJavaCodeAction(idFieldParams, IJDT_UTILS, removeIdFromIdAction);
    }

    @Test
    public void testMultipleEmbeddedIdOnGetter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMultipleEmbeddedIdOnGetter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Diagnostic on getId1() getter
        Diagnostic firstEmbeddedIdDiagnostic = d(15, 24, 30,
                                                 "An entity must not declare more than one @EmbeddedId annotation.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "MultipleEmbeddedIdAnnotations");

        // Diagnostic on getId2() getter
        Diagnostic secondEmbeddedIdDiagnostic = d(20, 24, 30,
                                                  "An entity must not declare more than one @EmbeddedId annotation.",
                                                  DiagnosticSeverity.Error, "jakarta-persistence", "MultipleEmbeddedIdAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, firstEmbeddedIdDiagnostic, secondEmbeddedIdDiagnostic);

        // Quick fix: Remove @EmbeddedId from getId1
        JakartaJavaCodeActionParams removeFirstEmbeddedIdParams = createCodeActionParams(uri, firstEmbeddedIdDiagnostic);
        TextEdit removeFirstEmbeddedIdEdit = te(14, 4, 15, 4, "");
        CodeAction removeFirstEmbeddedIdAction = ca(uri, "Remove @EmbeddedId", firstEmbeddedIdDiagnostic, removeFirstEmbeddedIdEdit);
        assertJavaCodeAction(removeFirstEmbeddedIdParams, IJDT_UTILS, removeFirstEmbeddedIdAction);

        // Quick fix: Remove @EmbeddedId from getId2
        JakartaJavaCodeActionParams removeSecondEmbeddedIdParams = createCodeActionParams(uri, secondEmbeddedIdDiagnostic);
        TextEdit removeSecondEmbeddedIdEdit = te(19, 4, 20, 4, "");
        CodeAction removeSecondEmbeddedIdAction = ca(uri, "Remove @EmbeddedId", secondEmbeddedIdDiagnostic, removeSecondEmbeddedIdEdit);
        assertJavaCodeAction(removeSecondEmbeddedIdParams, IJDT_UTILS, removeSecondEmbeddedIdAction);
    }

    @Test
    public void testMixedIdentifiersOnGetter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMixedIdentifiersOnGetter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Diagnostic on @Id getter (getId)
        Diagnostic mixedIdDiagnostic = d(16, 16, 21,
                                         "@Id cannot be combined with @EmbeddedId in the same entity.",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        // Diagnostic on @EmbeddedId getter (getCompositeId)
        Diagnostic mixedEmbeddedIdDiagnostic = d(21, 24, 38,
                                                 "@EmbeddedId cannot be combined with @Id in the same entity.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, mixedIdDiagnostic, mixedEmbeddedIdDiagnostic);

        // Quick fix on @Id getter (getId): only offers Remove @Id
        JakartaJavaCodeActionParams idGetterParams = createCodeActionParams(uri, mixedIdDiagnostic);
        TextEdit removeIdEdit = te(15, 4, 16, 4, "");
        CodeAction removeIdAction = ca(uri, "Remove @Id", mixedIdDiagnostic, removeIdEdit);
        assertJavaCodeAction(idGetterParams, IJDT_UTILS, removeIdAction);

        // Quick fix on @EmbeddedId getter (getCompositeId): only offers Remove @EmbeddedId
        JakartaJavaCodeActionParams embeddedIdGetterParams = createCodeActionParams(uri, mixedEmbeddedIdDiagnostic);
        TextEdit removeEmbeddedIdEdit = te(20, 4, 21, 4, "");
        CodeAction removeEmbeddedIdAction = ca(uri, "Remove @EmbeddedId", mixedEmbeddedIdDiagnostic, removeEmbeddedIdEdit);
        assertJavaCodeAction(embeddedIdGetterParams, IJDT_UTILS, removeEmbeddedIdAction);
    }

    @Test
    public void testMixedIdentifiersFieldAndGetter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMixedIdentifiersFieldAndGetter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Diagnostic on @Id field (id)
        Diagnostic mixedIdDiagnostic = d(10, 17, 19,
                                         "@Id cannot be combined with @EmbeddedId in the same entity.",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        // Diagnostic on @EmbeddedId getter (getCompositeId)
        Diagnostic mixedEmbeddedIdDiagnostic = d(18, 24, 38,
                                                 "@EmbeddedId cannot be combined with @Id in the same entity.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, mixedIdDiagnostic, mixedEmbeddedIdDiagnostic);

        // Quick fix on @Id field (id): only offers Remove @Id
        JakartaJavaCodeActionParams idFieldParams = createCodeActionParams(uri, mixedIdDiagnostic);
        TextEdit removeIdEdit = te(9, 4, 10, 4, "");
        CodeAction removeIdAction = ca(uri, "Remove @Id", mixedIdDiagnostic, removeIdEdit);
        assertJavaCodeAction(idFieldParams, IJDT_UTILS, removeIdAction);

        // Quick fix on @EmbeddedId getter (getCompositeId): only offers Remove @EmbeddedId
        JakartaJavaCodeActionParams embeddedIdGetterParams2 = createCodeActionParams(uri, mixedEmbeddedIdDiagnostic);
        TextEdit removeEmbeddedIdEdit = te(17, 4, 18, 4, "");
        CodeAction removeEmbeddedIdAction = ca(uri, "Remove @EmbeddedId", mixedEmbeddedIdDiagnostic, removeEmbeddedIdEdit);
        assertJavaCodeAction(embeddedIdGetterParams2, IJDT_UTILS, removeEmbeddedIdAction);
    }

    @Test
    public void testMixedIdentifiersGetterAndField() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityMixedIdentifiersGetterAndField.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Diagnostic on @EmbeddedId field (compositeId)
        Diagnostic mixedEmbeddedIdDiagnostic = d(10, 25, 36,
                                                 "@EmbeddedId cannot be combined with @Id in the same entity.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        // Diagnostic on @Id getter (getId)
        Diagnostic mixedIdDiagnostic = d(18, 16, 21,
                                         "@Id cannot be combined with @EmbeddedId in the same entity.",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "MixedIdentifierAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, mixedEmbeddedIdDiagnostic, mixedIdDiagnostic);

        // Quick fix on @EmbeddedId field (compositeId): only offers Remove @EmbeddedId
        JakartaJavaCodeActionParams embeddedIdFieldParams = createCodeActionParams(uri, mixedEmbeddedIdDiagnostic);
        TextEdit removeEmbeddedIdEdit = te(9, 4, 10, 4, "");
        CodeAction removeEmbeddedIdAction = ca(uri, "Remove @EmbeddedId", mixedEmbeddedIdDiagnostic, removeEmbeddedIdEdit);
        assertJavaCodeAction(embeddedIdFieldParams, IJDT_UTILS, removeEmbeddedIdAction);

        // Quick fix on @Id getter (getId): only offers Remove @Id
        JakartaJavaCodeActionParams idGetterParams = createCodeActionParams(uri, mixedIdDiagnostic);
        TextEdit removeIdEdit = te(17, 4, 18, 4, "");
        CodeAction removeIdAction = ca(uri, "Remove @Id", mixedIdDiagnostic, removeIdEdit);
        assertJavaCodeAction(idGetterParams, IJDT_UTILS, removeIdAction);
    }

    @Test
    public void testMapKeyEnumeratedOnNonEnumKey() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyEnumeratedNonEnumKey.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic stringKeyOnMapField = d(19, 32, 48,
                                           "@MapKeyEnumerated can only be applied to a field or property that maps to a java.util.Map whose key type is an enum.",
                                           DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyEnumeratedOnNonEnumType");

        Diagnostic integerKeyOnMapField = d(24, 33, 53,
                                            "@MapKeyEnumerated can only be applied to a field or property that maps to a java.util.Map whose key type is an enum.",
                                            DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyEnumeratedOnNonEnumType");

        Diagnostic rawMapKeyOnMapField = d(28, 16, 33,
                                           "@MapKeyEnumerated can only be applied to a field or property that maps to a java.util.Map whose key type is an enum.",
                                           DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyEnumeratedOnNonEnumType");

        Diagnostic wildcardKeyOnMapField = d(33, 27, 41,
                                             "@MapKeyEnumerated can only be applied to a field or property that maps to a java.util.Map whose key type is an enum.",
                                             DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyEnumeratedOnNonEnumType");

        Diagnostic methodWithStringKeyMap = d(38, 31, 46,
                                              "@MapKeyEnumerated can only be applied to a field or property that maps to a java.util.Map whose key type is an enum.",
                                              DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyEnumeratedOnNonEnumType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, stringKeyOnMapField, integerKeyOnMapField, rawMapKeyOnMapField,
                              wildcardKeyOnMapField, methodWithStringKeyMap);
    }

    @Test
    public void testMapKeyEnumeratedNotOnMap() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyEnumeratedNotOnMap.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic listFieldAnnotated = d(19, 25, 30,
                                          "@MapKeyEnumerated can only be applied to a field or property of type java.util.Map.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyEnumeratedNotOnMapType");

        Diagnostic plainStringFieldAnnotated = d(23, 19, 23,
                                                 "@MapKeyEnumerated can only be applied to a field or property of type java.util.Map.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "InvalidMapKeyEnumeratedNotOnMapType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, listFieldAnnotated, plainStringFieldAnnotated);
    }

    @Test
    public void testMapKeyEnumeratedValidEnumKey() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/MapKeyEnumeratedValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Valid: map key is an enum — no diagnostic expected
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testInheritanceOnPlainClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/InheritanceOnPlainClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic inheritanceOnPlainClassDiagnostic = d(7, 22, 45,
                                                         "A class using the @Inheritance annotation must also be annotated with @Entity.",
                                                         DiagnosticSeverity.Error, "jakarta-persistence", "InheritanceAnnotationOnNonEntityClass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, inheritanceOnPlainClassDiagnostic);
    }

    @Test
    public void testInheritanceOnMappedSuperclass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/InheritanceOnMappedSuperclass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic inheritanceOnMappedSuperclassDiagnostic = d(10, 22, 51,
                                                               "A class using the @Inheritance annotation must also be annotated with @Entity.",
                                                               DiagnosticSeverity.Error, "jakarta-persistence", "InheritanceAnnotationOnNonEntityClass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, inheritanceOnMappedSuperclassDiagnostic);
    }

    @Test
    public void testInheritanceOnValidEntityRoot() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/InheritanceEntityRoot.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @Entity + @Inheritance with no @Entity ancestor — no diagnostic expected
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testInheritanceOnRootEntityExtendsMappedSuperclass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/InheritanceOnRootEntityExtendsMappedSuperclass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @Entity + @Inheritance extending @MappedSuperclass — @MappedSuperclass is not
        // @Entity so no @Entity ancestor exists in the chain — no diagnostic expected
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testInheritanceOnNonRootEntityDirectParent() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/InheritanceOnNonRootEntityDirectParent.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic inheritanceOnNonRootEntityDirectParentDiagnostic = d(10, 13, 51,
                                                                        "A class using the @Inheritance annotation must be the root of the entity class hierarchy.",
                                                                        DiagnosticSeverity.Error, "jakarta-persistence", "InheritanceAnnotationOnNonRootEntity");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, inheritanceOnNonRootEntityDirectParentDiagnostic);
    }

    @Test
    public void testInheritanceOnNonRootEntityWithGap() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/InheritanceOnNonRootEntityWithGap.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Full chain walk required: @Entity ancestor is hidden behind a non-entity abstract gap
        Diagnostic inheritanceOnNonRootEntityWithGapDiagnostic = d(13, 13, 46,
                                                                   "A class using the @Inheritance annotation must be the root of the entity class hierarchy.",
                                                                   DiagnosticSeverity.Error, "jakarta-persistence", "InheritanceAnnotationOnNonRootEntity");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, inheritanceOnNonRootEntityWithGapDiagnostic);
    }

    // -------------------------------------------------------------------------
    // @TableGenerator / @TableGenerators diagnostics
    // -------------------------------------------------------------------------

    @Test
    public void testTableGeneratorEmptyNameOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/TableGeneratorEmptyNameOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic tableGeneratorEmptyNameOnType = d(9, 0, 26,
                                                     "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                     DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorEmptyNameOnField = d(14, 4, 30,
                                                      "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                      DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorEmptyNameOnMethod = d(17, 4, 30,
                                                       "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                       DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              tableGeneratorEmptyNameOnType, tableGeneratorEmptyNameOnField, tableGeneratorEmptyNameOnMethod);
    }

    @Test
    public void testTableGeneratorsMissingTableGeneratorMappingOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/TableGeneratorsMissingTableGeneratorMappingOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic tableGeneratorsMissingMappingOnType = d(9, 0, 20,
                                                           "The @TableGenerators annotation must specify at least one @TableGenerator mapping.",
                                                           DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorsMissingTableGeneratorMapping");
        Diagnostic tableGeneratorsMissingMappingOnField = d(14, 4, 24,
                                                            "The @TableGenerators annotation must specify at least one @TableGenerator mapping.",
                                                            DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorsMissingTableGeneratorMapping");
        Diagnostic tableGeneratorsMissingMappingOnMethod = d(17, 4, 24,
                                                             "The @TableGenerators annotation must specify at least one @TableGenerator mapping.",
                                                             DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorsMissingTableGeneratorMapping");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              tableGeneratorsMissingMappingOnType, tableGeneratorsMissingMappingOnField, tableGeneratorsMissingMappingOnMethod);
    }

    @Test
    public void testTableGeneratorsEmptyArrayOnNonEntityClass() throws Exception {
        // Confirms that the empty-array branch in validateNonEmptyMappingArray is only reached for @Entity-annotated classes.
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/TableGeneratorsEmptyArrayNoEntity.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testTableGeneratorsWithEmptyNameOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/TableGeneratorsWithEmptyNameOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic tableGeneratorEmptyNameOnType = d(10, 19, 45,
                                                     "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                     DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorEmptyNameOnField = d(15, 23, 49,
                                                      "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                      DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorEmptyNameOnMethod = d(18, 23, 49,
                                                       "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                       DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              tableGeneratorEmptyNameOnType, tableGeneratorEmptyNameOnField, tableGeneratorEmptyNameOnMethod);
    }

    @Test
    public void testTableGeneratorsWithMultipleEmptyNamesOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/TableGeneratorsWithMultipleEmptyNamesOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic tableGeneratorFirstEmptyNameOnType = d(10, 19, 48,
                                                          "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                          DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorSecondEmptyNameOnType = d(10, 83, 109,
                                                           "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                           DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorFirstEmptyNameOnField = d(15, 23, 52,
                                                           "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                           DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorSecondEmptyNameOnField = d(15, 87, 113,
                                                            "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                            DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorFirstEmptyNameOnMethod = d(18, 23, 52,
                                                            "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                            DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");
        Diagnostic tableGeneratorSecondEmptyNameOnMethod = d(18, 87, 113,
                                                             "The @TableGenerator annotation must specify a non-empty 'name' attribute.",
                                                             DiagnosticSeverity.Error, "jakarta-persistence", "TableGeneratorInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              tableGeneratorFirstEmptyNameOnType, tableGeneratorSecondEmptyNameOnType,
                              tableGeneratorFirstEmptyNameOnField, tableGeneratorSecondEmptyNameOnField,
                              tableGeneratorFirstEmptyNameOnMethod, tableGeneratorSecondEmptyNameOnMethod);
    }

    // -------------------------------------------------------------------------
    // @SequenceGenerator / @SequenceGenerators diagnostics
    // -------------------------------------------------------------------------

    @Test
    public void testSequenceGeneratorEmptyNameOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SequenceGeneratorEmptyNameOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic sequenceGeneratorEmptyNameOnType = d(9, 0, 29,
                                                        "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                        DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorEmptyNameOnField = d(14, 4, 33,
                                                         "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                         DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorEmptyNameOnMethod = d(17, 4, 33,
                                                          "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                          DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              sequenceGeneratorEmptyNameOnType, sequenceGeneratorEmptyNameOnField, sequenceGeneratorEmptyNameOnMethod);
    }

    @Test
    public void testSequenceGeneratorsMissingSequenceGeneratorMappingOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SequenceGeneratorsMissingSequenceGeneratorMappingOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic sequenceGeneratorsMissingMappingOnType = d(9, 0, 23,
                                                              "The @SequenceGenerators annotation must specify at least one @SequenceGenerator mapping.",
                                                              DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorsMissingSequenceGeneratorMapping");
        Diagnostic sequenceGeneratorsMissingMappingOnField = d(14, 4, 27,
                                                               "The @SequenceGenerators annotation must specify at least one @SequenceGenerator mapping.",
                                                               DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorsMissingSequenceGeneratorMapping");
        Diagnostic sequenceGeneratorsMissingMappingOnMethod = d(17, 4, 27,
                                                                "The @SequenceGenerators annotation must specify at least one @SequenceGenerator mapping.",
                                                                DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorsMissingSequenceGeneratorMapping");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              sequenceGeneratorsMissingMappingOnType, sequenceGeneratorsMissingMappingOnField, sequenceGeneratorsMissingMappingOnMethod);
    }

    @Test
    public void testSequenceGeneratorsWithEmptyNameOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SequenceGeneratorsWithEmptyNameOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic sequenceGeneratorEmptyNameOnType = d(10, 22, 51,
                                                        "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                        DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorEmptyNameOnField = d(15, 26, 55,
                                                         "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                         DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorEmptyNameOnMethod = d(18, 26, 55,
                                                          "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                          DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              sequenceGeneratorEmptyNameOnType, sequenceGeneratorEmptyNameOnField, sequenceGeneratorEmptyNameOnMethod);
    }

    @Test
    public void testSequenceGeneratorsWithMultipleEmptyNamesOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SequenceGeneratorsWithMultipleEmptyNamesOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic sequenceGeneratorFirstEmptyNameOnType = d(10, 22, 54,
                                                             "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                             DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorSecondEmptyNameOnType = d(10, 92, 121,
                                                              "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                              DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorFirstEmptyNameOnField = d(15, 26, 58,
                                                              "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                              DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorSecondEmptyNameOnField = d(15, 96, 125,
                                                               "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                               DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorFirstEmptyNameOnMethod = d(18, 26, 58,
                                                               "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                               DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");
        Diagnostic sequenceGeneratorSecondEmptyNameOnMethod = d(18, 96, 125,
                                                                "The @SequenceGenerator annotation must specify a non-empty 'name' attribute.",
                                                                DiagnosticSeverity.Error, "jakarta-persistence", "SequenceGeneratorInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              sequenceGeneratorFirstEmptyNameOnType, sequenceGeneratorSecondEmptyNameOnType,
                              sequenceGeneratorFirstEmptyNameOnField, sequenceGeneratorSecondEmptyNameOnField,
                              sequenceGeneratorFirstEmptyNameOnMethod, sequenceGeneratorSecondEmptyNameOnMethod);
    }

    // -------------------------------------------------------------------------
    // @SecondaryTable / @SecondaryTables diagnostics (TYPE only)
    // -------------------------------------------------------------------------

    @Test
    public void testSecondaryTableEmptyName() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SecondaryTableEmptyName.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic secondaryTableEmptyName = d(8, 0, 26,
                                               "The @SecondaryTable annotation must specify a non-empty 'name' attribute.",
                                               DiagnosticSeverity.Error, "jakarta-persistence", "SecondaryTableInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, secondaryTableEmptyName);
    }

    @Test
    public void testSecondaryTablesMissingSecondaryTableMapping() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SecondaryTablesMissingSecondaryTableMapping.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic secondaryTablesMissingMapping = d(8, 0, 20,
                                                     "The @SecondaryTables annotation must specify at least one @SecondaryTable mapping.",
                                                     DiagnosticSeverity.Error, "jakarta-persistence", "SecondaryTablesMissingSecondaryTableMapping");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, secondaryTablesMissingMapping);
    }

    @Test
    public void testSecondaryTablesWithEmptyName() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SecondaryTablesWithEmptyName.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic secondaryTableEmptyNameInContainer = d(10, 4, 30,
                                                          "The @SecondaryTable annotation must specify a non-empty 'name' attribute.",
                                                          DiagnosticSeverity.Error, "jakarta-persistence", "SecondaryTableInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, secondaryTableEmptyNameInContainer);
    }

    @Test
    public void testSecondaryTablesWithMultipleEmptyNames() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SecondaryTablesWithMultipleEmptyNames.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic secondaryTableFirstEmptyNameInContainer = d(10, 19, 48,
                                                               "The @SecondaryTable annotation must specify a non-empty 'name' attribute.",
                                                               DiagnosticSeverity.Error, "jakarta-persistence", "SecondaryTableInvalidEmptyName");
        Diagnostic secondaryTableSecondEmptyNameInContainer = d(10, 83, 109,
                                                                "The @SecondaryTable annotation must specify a non-empty 'name' attribute.",
                                                                DiagnosticSeverity.Error, "jakarta-persistence", "SecondaryTableInvalidEmptyName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              secondaryTableFirstEmptyNameInContainer, secondaryTableSecondEmptyNameInContainer);
    }

    // -------------------------------------------------------------------------
    // Valid cases — no diagnostics expected
    // -------------------------------------------------------------------------

    @Test
    public void testTableGeneratorValidNameOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/TableGeneratorValidNameOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — all @TableGenerator names are non-empty
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testTableGeneratorsValidNamesOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/TableGeneratorsValidNamesOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — all nested @TableGenerator names are non-empty
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testSequenceGeneratorValidNameOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SequenceGeneratorValidNameOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — all @SequenceGenerator names are non-empty
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testSequenceGeneratorsValidNamesOnTypeFieldAndMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SequenceGeneratorsValidNamesOnTypeFieldMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — all nested @SequenceGenerator names are non-empty
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testSecondaryTableValidName() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SecondaryTableValidName.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — @SecondaryTable name is non-empty
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testSecondaryTablesValidNames() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/SecondaryTablesValidNames.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — all nested @SecondaryTable names are non-empty
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
    @Test
    public void testNamedEntityGraphOnValidEntityClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphOnValidEntityClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testNamedQueryOnValidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedQueryOnValidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testNamedNativeQueryOnValidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedNativeQueryOnValidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testNamedEntityGraphOnNonEntityClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphOnNonEntityClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        JsonArray diagnosticsData = new JsonArray();
        diagnosticsData.add("jakarta.persistence.NamedEntityGraph");
        Diagnostic expectedDiagnostic = d(4, 0, 38,
                                          "@NamedEntityGraph must only be applied to a class annotated with @Entity.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "NamedEntityGraphOnNonEntityClass",
                                          diagnosticsData);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, expectedDiagnostic);
        TextEdit insertEntityTextEdit = te(2, 0, 4, 0, "import jakarta.persistence.Entity;\nimport jakarta.persistence.NamedEntityGraph;\n\n@Entity\n");
        CodeAction insertEntityCodeAction = ca(uri, "Insert @Entity", expectedDiagnostic, insertEntityTextEdit);
        TextEdit removeTextEdit = te(4, 0, 5, 0, "");
        CodeAction removeCodeAction = ca(uri, "Remove @NamedEntityGraph", expectedDiagnostic, removeTextEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertEntityCodeAction, removeCodeAction);
    }

    @Test
    public void testNamedQueryOnInvalidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedQueryOnInvalidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        JsonArray diagnosticsData = new JsonArray();
        diagnosticsData.add("jakarta.persistence.NamedQuery");
        Diagnostic expectedDiagnostic = d(4, 0, 66,
                                          "@NamedQuery must only be applied to a class annotated with @Entity or @MappedSuperclass.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "NamedQueryOnInvalidClass",
                                          diagnosticsData);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, expectedDiagnostic);
        TextEdit insertEntityTextEdit = te(2, 0, 4, 0, "import jakarta.persistence.Entity;\nimport jakarta.persistence.NamedQuery;\n\n@Entity\n");
        CodeAction insertEntityCodeAction = ca(uri, "Insert @Entity", expectedDiagnostic, insertEntityTextEdit);
        TextEdit insertMappedSuperclassTextEdit = te(2, 0, 4, 0, "import jakarta.persistence.MappedSuperclass;\nimport jakarta.persistence.NamedQuery;\n\n@MappedSuperclass\n");
        CodeAction insertMappedSuperclassCodeAction = ca(uri, "Insert @MappedSuperclass", expectedDiagnostic, insertMappedSuperclassTextEdit);
        TextEdit removeTextEdit = te(4, 0, 5, 0, "");
        CodeAction removeCodeAction = ca(uri, "Remove @NamedQuery", expectedDiagnostic, removeTextEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertEntityCodeAction, insertMappedSuperclassCodeAction, removeCodeAction);
    }

    @Test
    public void testNamedNativeQueryOnInvalidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedNativeQueryOnInvalidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        JsonArray diagnosticsData = new JsonArray();
        diagnosticsData.add("jakarta.persistence.NamedNativeQuery");
        Diagnostic expectedDiagnostic = d(4, 0, 136,
                                          "@NamedNativeQuery must only be applied to a class annotated with @Entity or @MappedSuperclass.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "NamedNativeQueryOnInvalidClass",
                                          diagnosticsData);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, expectedDiagnostic);
        TextEdit insertEntityTextEdit = te(2, 0, 4, 0, "import jakarta.persistence.Entity;\nimport jakarta.persistence.NamedNativeQuery;\n\n@Entity\n");
        CodeAction insertEntityCodeAction = ca(uri, "Insert @Entity", expectedDiagnostic, insertEntityTextEdit);
        TextEdit insertMappedSuperclassTextEdit = te(2, 0, 4, 0,
                                                     "import jakarta.persistence.MappedSuperclass;\nimport jakarta.persistence.NamedNativeQuery;\n\n@MappedSuperclass\n");
        CodeAction insertMappedSuperclassCodeAction = ca(uri, "Insert @MappedSuperclass", expectedDiagnostic, insertMappedSuperclassTextEdit);
        TextEdit removeTextEdit = te(4, 0, 5, 0, "");
        CodeAction removeCodeAction = ca(uri, "Remove @NamedNativeQuery", expectedDiagnostic, removeTextEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertEntityCodeAction, insertMappedSuperclassCodeAction, removeCodeAction);
    }

    @Test
    public void testNamedEntityGraphsOnValidEntityClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphsOnValidEntityClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testNamedQueriesOnValidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedQueriesOnValidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testNamedNativeQueriesOnValidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedNativeQueriesOnValidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testNamedEntityGraphsOnNonEntityClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphsOnNonEntityClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        JsonArray diagnosticsData = new JsonArray();
        diagnosticsData.add("jakarta.persistence.NamedEntityGraphs");
        Diagnostic expectedDiagnostic = d(5, 0, 103,
                                          "@NamedEntityGraphs must only be applied to a class annotated with @Entity.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "NamedEntityGraphsOnNonEntityClass",
                                          diagnosticsData);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, expectedDiagnostic);
        TextEdit insertEntityTextEdit = te(2, 0, 5, 0,
                                           "import jakarta.persistence.Entity;\nimport jakarta.persistence.NamedEntityGraph;\nimport jakarta.persistence.NamedEntityGraphs;\n\n@Entity\n");
        CodeAction insertEntityCodeAction = ca(uri, "Insert @Entity", expectedDiagnostic, insertEntityTextEdit);
        TextEdit removeTextEdit = te(5, 0, 6, 0, "");
        CodeAction removeCodeAction = ca(uri, "Remove @NamedEntityGraphs", expectedDiagnostic, removeTextEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertEntityCodeAction, removeCodeAction);
    }

    @Test
    public void testNamedQueriesOnInvalidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedQueriesOnInvalidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        JsonArray diagnosticsData = new JsonArray();
        diagnosticsData.add("jakarta.persistence.NamedQueries");
        Diagnostic expectedDiagnostic = d(5, 0, 85,
                                          "@NamedQueries must only be applied to a class annotated with @Entity or @MappedSuperclass.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "NamedQueriesOnInvalidClass",
                                          diagnosticsData);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, expectedDiagnostic);
        TextEdit insertEntityTextEdit = te(2, 0, 5, 0,
                                           "import jakarta.persistence.Entity;\nimport jakarta.persistence.NamedQueries;\nimport jakarta.persistence.NamedQuery;\n\n@Entity\n");
        CodeAction insertEntityCodeAction = ca(uri, "Insert @Entity", expectedDiagnostic, insertEntityTextEdit);
        TextEdit insertMappedSuperclassTextEdit = te(2, 0, 5, 0,
                                                     "import jakarta.persistence.MappedSuperclass;\nimport jakarta.persistence.NamedQueries;\nimport jakarta.persistence.NamedQuery;\n\n@MappedSuperclass\n");
        CodeAction insertMappedSuperclassCodeAction = ca(uri, "Insert @MappedSuperclass", expectedDiagnostic, insertMappedSuperclassTextEdit);
        TextEdit removeTextEdit = te(5, 0, 6, 0, "");
        CodeAction removeCodeAction = ca(uri, "Remove @NamedQueries", expectedDiagnostic, removeTextEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertEntityCodeAction, insertMappedSuperclassCodeAction, removeCodeAction);
    }

    @Test
    public void testNamedNativeQueriesOnInvalidClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedNativeQueriesOnInvalidClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        JsonArray diagnosticsData = new JsonArray();
        diagnosticsData.add("jakarta.persistence.NamedNativeQueries");
        Diagnostic expectedDiagnostic = d(5, 0, 163,
                                          "@NamedNativeQueries must only be applied to a class annotated with @Entity or @MappedSuperclass.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "NamedNativeQueriesOnInvalidClass",
                                          diagnosticsData);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, expectedDiagnostic);
        TextEdit insertEntityTextEdit = te(2, 0, 5, 0,
                                           "import jakarta.persistence.Entity;\nimport jakarta.persistence.NamedNativeQueries;\nimport jakarta.persistence.NamedNativeQuery;\n\n@Entity\n");
        CodeAction insertEntityCodeAction = ca(uri, "Insert @Entity", expectedDiagnostic, insertEntityTextEdit);
        TextEdit insertMappedSuperclassTextEdit = te(2, 0, 5, 0,
                                                     "import jakarta.persistence.MappedSuperclass;\nimport jakarta.persistence.NamedNativeQueries;\nimport jakarta.persistence.NamedNativeQuery;\n\n@MappedSuperclass\n");
        CodeAction insertMappedSuperclassCodeAction = ca(uri, "Insert @MappedSuperclass", expectedDiagnostic, insertMappedSuperclassTextEdit);
        TextEdit removeTextEdit = te(5, 0, 6, 0, "");
        CodeAction removeCodeAction = ca(uri, "Remove @NamedNativeQueries", expectedDiagnostic, removeTextEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertEntityCodeAction, insertMappedSuperclassCodeAction, removeCodeAction);
    }

}
