/*******************************************************************************
* Copyright (c) 2020, 2025 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Matheus Cruz, Yijia Jing - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.jsonb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * JSON-B diagnostic participant that manages the use of @JsonbTransient,
 * and @JsonbCreator annotations.
 */
public class JsonbDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        IMethod[] methods;
        IAnnotation[] allAnnotations;

        for (IType type : types) {
            methods = type.getMethods();
            List<IMethod> jonbMethods = new ArrayList<IMethod>();
            // methods
            for (IMethod method : type.getMethods()) {
                if (DiagnosticUtils.isConstructorMethod(method) || Flags.isStatic(method.getFlags())) {
                    allAnnotations = method.getAnnotations();
                    for (IAnnotation annotation : allAnnotations) {
                        if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                                 Constants.JSONB_CREATOR))
                            jonbMethods.add(method);
                    }
                }
            }
            if (jonbMethods.size() > Constants.MAX_METHOD_WITH_JSONBCREATOR) {
                for (IMethod method : methods) {
                    String msg = Messages.getMessage("ErrorMessageJsonbCreator");
                    Range range = PositionUtils.toNameRange(method, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidNumerOfJsonbCreatorAnnotationsInClass, DiagnosticSeverity.Error));
                }
            }

            //Changes to support lsp4jakarta issue #291 to add diagnostic for Property Name Uniqueness
            List<String> propertyNames = new ArrayList<String>();
            List<String> uniquePropertyNames = new ArrayList<String>();
            // fields
            for (IField field : type.getFields()) {
                collectJsonbTransientFieldDiagnostics(context, uri, unit, type, diagnostics, field);
                collectJsonbTransientAccessorDiagnostics(context, uri, unit, type, diagnostics, field);
                uniquePropertyNames = collectJsonbUniquePropertyNames(context, uri, diagnostics, type, propertyNames,
                                                                      field);
            }
            collectJsonbPropertyUniquenessDiagnostics(uniquePropertyNames, context, uri, diagnostics, type,
                                                      propertyNames);
        }
        return diagnostics;
    }

    private void collectJsonbPropertyUniquenessDiagnostics(List<String> uniquePropertyNames,
                                                           JavaDiagnosticsContext context, String uri, List<Diagnostic> diagnostics, IType type,
                                                           List<String> propertyNames) throws JavaModelException {
        Set<IType> hierarchy = new LinkedHashSet<>();
        collectSuperTypes(type, hierarchy);
        Map<String, List<IField>> jsonbMap = buildPropertyMap(uniquePropertyNames, hierarchy);
        for (Map.Entry<String, List<IField>> entry : jsonbMap.entrySet()) {
            List<IField> fields = entry.getValue();
            if (fields.size() > Constants.DUPLICATE_PROPERTY_VALUE) {
                for (IField f : fields) {
                    if (f.getDeclaringType().equals(type))
                        createJsonbPropertyUniquenessDiagnostics(context, uri, diagnostics, f, type);
                }
            }
        }
    }

    private Map<String, List<IField>> buildPropertyMap(List<String> uniquePropertyNames, Set<IType> hierarchy) throws JavaModelException {
        Map<String, List<IField>> jsonbMap = new HashMap<>();
        for (IType finaltype : hierarchy) {
            for (IField field : finaltype.getFields()) {
                for (IAnnotation annotation : field.getAnnotations()) {
                    if (Constants.JSONB_PROPERTY.contains(annotation.getElementName())) {
                        String propertyName = extractPropertyNameFromJsonField(annotation);
                        if (propertyName != null) {
                            propertyName = decodeUniCodeName(propertyName);
                            if (uniquePropertyNames.contains(propertyName)) {
                                jsonbMap.computeIfAbsent(propertyName, k -> new ArrayList<>()).add(field);
                            }
                        }
                    }
                }
            }
        }
        return jsonbMap;
    }

    private void collectSuperTypes(IType type, Set<IType> hierarchy) throws JavaModelException {
        if (type == null && hierarchy.contains(type))
            return;
        hierarchy.add(type);
        String superClassName = type.getSuperclassName();
        if (superClassName != null) {
            String[][] fullQualifiedSuperName = type.resolveType(superClassName);
            if (fullQualifiedSuperName != null && fullQualifiedSuperName.length > 0) {
                String fqSuper = fullQualifiedSuperName[0][0] + "." + fullQualifiedSuperName[0][1];
                IType superType = type.getJavaProject().findType(fqSuper);
                collectSuperTypes(superType, hierarchy);
            }
        }
    }

    private List<String> collectJsonbUniquePropertyNames(JavaDiagnosticsContext context, String uri,
                                                         List<Diagnostic> diagnostics, IType type, List<String> propertyNames, IField field) throws JavaModelException {
        for (IAnnotation annotation : field.getAnnotations()) {
            if (Constants.JSONB_PROPERTY.contains(annotation.getElementName())) {
                String propertyName = extractPropertyNameFromJsonField(annotation);
                if (propertyName != null) {
                    propertyName = decodeUniCodeName(propertyName);
                    propertyNames.add(propertyName);
                }
            }
        }
        return propertyNames.stream().distinct().collect(Collectors.toList());
    }

    private String decodeUniCodeName(String propertyName) {
        Pattern pattern = Pattern.compile(Constants.JSONB_PROPERTYNAME_UNICODE);
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

    private String extractPropertyNameFromJsonField(IAnnotation annotation) throws JavaModelException {
        for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
            if (pair.getValue() instanceof String) {
                return (String) pair.getValue();
            }
        }
        return null;
    }

    private void createJsonbPropertyUniquenessDiagnostics(JavaDiagnosticsContext context, String uri,
                                                          List<Diagnostic> diagnostics, IField field, IType type) throws JavaModelException {
        String msg = Messages.getMessage("ErrorMessageJsonbPropertyUniquenessField");
        Range range = PositionUtils.toNameRange(field, context.getUtils());
        diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                 ErrorCode.InvalidPropertyNamesOnJsonbFields, DiagnosticSeverity.Error));
    }

    private void collectJsonbTransientFieldDiagnostics(JavaDiagnosticsContext context, String uri,
                                                       ICompilationUnit unit, IType type, List<Diagnostic> diagnostics, IField field) throws JavaModelException {
        List<String> jsonbAnnotationsForField = getJsonbAnnotationNames(type, field);
        if (jsonbAnnotationsForField.contains(Constants.JSONB_TRANSIENT_FQ_NAME)) {
            boolean hasAccessorConflict = false;
            // Diagnostics on the accessors of the field are created when they are
            // annotated with Jsonb annotations other than JsonbTransient.
            List<IMethod> accessors = DiagnosticUtils.getFieldAccessors(unit, field);
            for (IMethod accessor : accessors) {
                List<String> jsonbAnnotationsForAccessor = getJsonbAnnotationNames(type, accessor);
                if (hasJsonbAnnotationOtherThanTransient(jsonbAnnotationsForAccessor)) {
                    Range range = PositionUtils.toNameRange(accessor, context.getUtils());
                    createJsonbTransientDiagnostic(context, uri, range, unit, diagnostics, accessor,
                                                   jsonbAnnotationsForAccessor,
                                                   ErrorCode.InvalidJSonBindindAnnotationWithJsonbTransientOnField);
                    hasAccessorConflict = true;
                }
            }
            // Diagnostic is created on the field if @JsonbTransient is not mutually
            // exclusive or
            // accessor has annotations other than JsonbTransient
            if (hasAccessorConflict || hasJsonbAnnotationOtherThanTransient(jsonbAnnotationsForField)) {
                Range range = PositionUtils.toNameRange(field, context.getUtils());
                createJsonbTransientDiagnostic(context, uri, range, unit, diagnostics, field, jsonbAnnotationsForField,
                                               ErrorCode.InvalidJSonBindindAnnotationWithJsonbTransientOnField);
            }
        }
    }

    private void collectJsonbTransientAccessorDiagnostics(JavaDiagnosticsContext context, String uri,
                                                          ICompilationUnit unit, IType type,
                                                          List<Diagnostic> diagnostics, IField field) throws JavaModelException {
        boolean createDiagnosticForField = false;
        List<String> jsonbAnnotationsForField = getJsonbAnnotationNames(type, field);
        List<IMethod> accessors = DiagnosticUtils.getFieldAccessors(unit, field);
        for (IMethod accessor : accessors) {
            List<String> jsonbAnnotationsForAccessor = getJsonbAnnotationNames(type, accessor);
            boolean hasFieldConflict = false;
            if (jsonbAnnotationsForAccessor.contains(Constants.JSONB_TRANSIENT_FQ_NAME)) {
                // Diagnostic is created if the field of this accessor has a annotation other
                // then JsonbTransient
                if (hasJsonbAnnotationOtherThanTransient(jsonbAnnotationsForField)) {
                    createDiagnosticForField = true;
                    hasFieldConflict = true;
                }

                // Diagnostic is created on the accessor if field has annotation other than
                // JsonbTransient
                // or if @JsonbTransient is not mutually exclusive
                if (hasFieldConflict || hasJsonbAnnotationOtherThanTransient(jsonbAnnotationsForAccessor)) {
                    Range range = PositionUtils.toNameRange(accessor, context.getUtils());
                    createJsonbTransientDiagnostic(context, uri, range, unit, diagnostics, accessor,
                                                   jsonbAnnotationsForAccessor,
                                                   ErrorCode.InvalidJSonBindindAnnotationWithJsonbTransientOnAccessor);
                }

            }
        }
        if (createDiagnosticForField) {
            Range range = PositionUtils.toNameRange(field, context.getUtils());
            createJsonbTransientDiagnostic(context, uri, range, unit, diagnostics, field,
                                           jsonbAnnotationsForField,
                                           ErrorCode.InvalidJSonBindindAnnotationWithJsonbTransientOnAccessor);
        }
    }

    private boolean createJsonbTransientDiagnostic(JavaDiagnosticsContext context, String uri, Range range,
                                                   ICompilationUnit unit,
                                                   List<Diagnostic> diagnostics,
                                                   IMember member,
                                                   List<String> jsonbAnnotations, ErrorCode errorCode) throws JavaModelException {
        String diagnosticErrorMessage = null;
        if (errorCode.equals(ErrorCode.InvalidJSonBindindAnnotationWithJsonbTransientOnField)) {
            diagnosticErrorMessage = Messages.getMessage("ErrorMessageJsonbTransientOnField");
        } else if (errorCode.equals(ErrorCode.InvalidJSonBindindAnnotationWithJsonbTransientOnAccessor)) {
            diagnosticErrorMessage = Messages.getMessage("ErrorMessageJsonbTransientOnAccessor");
        }
        diagnostics.add(context.createDiagnostic(uri, diagnosticErrorMessage, range, Constants.DIAGNOSTIC_SOURCE,
                                                 (JsonArray) (new Gson().toJsonTree(jsonbAnnotations)),
                                                 errorCode, DiagnosticSeverity.Error));

        return true;
    }

    private List<String> getJsonbAnnotationNames(IType type, IAnnotatable annotable) throws JavaModelException {
        List<String> jsonbAnnotationNames = new ArrayList<String>();
        IAnnotation annotations[] = annotable.getAnnotations();
        for (IAnnotation annotation : annotations) {
            String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type, annotation.getElementName(),
                                                                                 Constants.JSONB_ANNOTATIONS.toArray(String[]::new));
            if (matchedAnnotation != null) {
                jsonbAnnotationNames.add(matchedAnnotation);
            }
        }
        return jsonbAnnotationNames;
    }

    private boolean hasJsonbAnnotationOtherThanTransient(List<String> jsonbAnnotations) throws JavaModelException {
        for (String annotationName : jsonbAnnotations)
            if (Constants.JSONB_ANNOTATIONS.contains(annotationName)
                && !annotationName.equals(Constants.JSONB_TRANSIENT_FQ_NAME))
                return true;
        return false;
    }

}