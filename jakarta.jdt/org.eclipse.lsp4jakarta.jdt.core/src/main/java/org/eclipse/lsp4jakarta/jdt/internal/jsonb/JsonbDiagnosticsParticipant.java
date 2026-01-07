/*******************************************************************************
* Copyright (c) 2020, 2026 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.commons.utils.JsonPropertyUtils;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaErrorCode;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
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

    private static final String NON_STATIC_DIAGNOSTIC = "Please declare the class as static";
    private static final Logger LOGGER = Logger.getLogger(JsonbDiagnosticsParticipant.class.getName());

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
        //Variable for checking parent class is JSONB type or not
        boolean jsonbtypeParent = false;
        //Variable to check if inner class or not
        boolean isInnerClass = false;
        //Variables for determining parameterized constructor in parent and child classes
        boolean parentHasNoArgsConstructor;
        boolean parentHasParameterizedConrtuctor;
        boolean childHasNoArgsConstructor;
        boolean childHasParameterizedConrtuctor;
        boolean missingParentNoArgsConstructor;
        boolean missingChildNoArgsConstructor;

        for (IType type : types) {
            parentHasNoArgsConstructor = false;
            parentHasParameterizedConrtuctor = false;
            childHasNoArgsConstructor = false;
            childHasParameterizedConrtuctor = false;
            missingParentNoArgsConstructor = false;
            missingChildNoArgsConstructor = false;
            isInnerClass = type.getDeclaringType() != null;
            //Checks whether parent class is JSONB type by checking class level annotations
            if (!isInnerClass) {
                jsonbtypeParent = Arrays.stream(type.getAnnotations()).anyMatch(annotation -> {
                    try {
                        return JsonPropertyUtils.isJsonbType(type, annotation);
                    } catch (JavaModelException e) {
                        LOGGER.log(Level.INFO, "Unable to find matching JSONB annotations", e.getMessage());
                        return false;
                    }
                });
            }
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
                //Check whether the class has public or protected no args constructor
                if (DiagnosticUtils.isConstructorMethod(method)) {
                    String[] params = method.getParameterTypes();
                    int flags = method.getFlags();
                    if (params.length == 0 &&
                        (Flags.isPublic(flags) || Flags.isProtected(flags))) {
                        if (!isInnerClass)
                            parentHasNoArgsConstructor = true;
                        else
                            childHasNoArgsConstructor = true;
                    } else {
                        if (type.getDeclaringType() == null)
                            parentHasParameterizedConrtuctor = true;
                        else
                            childHasParameterizedConrtuctor = true;
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
            //Changes to detect if Jsonb property names are not unique
            Set<String> propertyNames = new LinkedHashSet<String>();
            Set<String> uniquePropertyNames = new LinkedHashSet<String>();
            // fields
            for (IField field : type.getFields()) {
                collectJsonbTransientFieldDiagnostics(context, uri, unit, type, diagnostics, field);
                collectJsonbTransientAccessorDiagnostics(context, uri, unit, type, diagnostics, field);
                // Get unique property name values from the fields into a list uniquePropertyNames
                uniquePropertyNames = collectJsonbUniquePropertyNames(unit, context, uri, diagnostics, type, propertyNames,
                                                                      field);
                //Checks whether class fields have JSONB annotations
                if (!isInnerClass && !jsonbtypeParent) {
                    jsonbtypeParent = Arrays.stream(field.getAnnotations()).anyMatch(annotation -> {
                        try {
                            return JsonPropertyUtils.isJsonbType(type, annotation);
                        } catch (JavaModelException e) {
                            LOGGER.log(Level.INFO, "Unable to find matching JSONB annotations", e.getMessage());
                            return false;
                        }
                    });
                }
            }
            // Collect diagnostics for duplicate property names with fields annotated @JsonbProperty
            collectJsonbPropertyUniquenessDiagnostics(unit, uniquePropertyNames, context, uri, diagnostics, type);
            //Parent class conditions for checking missing no-args constructor
            missingParentNoArgsConstructor = jsonbtypeParent && !parentHasNoArgsConstructor && parentHasParameterizedConrtuctor;
            //Child class conditions for checking missing no-args constructor
            missingChildNoArgsConstructor = jsonbtypeParent && !childHasNoArgsConstructor && childHasParameterizedConrtuctor;
            //Generate Jsonb deseriazation diagnostics
            generateJsonbDeserializerDiagnostics(context, uri, diagnostics, jsonbtypeParent, isInnerClass,
                                                 missingParentNoArgsConstructor, missingChildNoArgsConstructor, type);
        }
        return diagnostics;
    }

    /**
     * @param context
     * @param uri
     * @param diagnostics
     * @param jsonbtypeParent
     * @param isInnerClass
     * @param missingParentNoArgs
     * @param missingChildNoArgs
     * @param type
     * @throws JavaModelException
     * @description This method generates diagnostics which deals with deserialization
     */
    private void generateJsonbDeserializerDiagnostics(JavaDiagnosticsContext context, String uri,
                                                      List<Diagnostic> diagnostics, boolean jsonbtypeParent, boolean isInnerClass, boolean missingParentNoArgs,
                                                      boolean missingChildNoArgs, IType type) throws JavaModelException {

        //Setting diagnostic message
        String deSerializeMsg = Messages.getMessage("ErrorMessageJsonbNoArgConstructorMissing", type.getElementName());
        IJavaErrorCode deserializeErrCode = ErrorCode.InvalidJsonBNoArgsConstructorMissing;
        boolean isStaticInner = false;
        //Parent class no args constructor missing diagnostics
        if (!isInnerClass) {
            if (missingParentNoArgs) {
                createJsonbNoArgConstructorDiagnostics(context, uri, diagnostics, type, deSerializeMsg, deserializeErrCode);
            }
        } else {
            //Inner class non-static diagnostics and no args constructor missing diagnostics
            isStaticInner = isInnerClass && Flags.isStatic(type.getFlags());
            if (!isStaticInner && jsonbtypeParent) {
                deSerializeMsg = Messages.getMessage("ErrorMessageJsonbInnerNonStatic", type.getElementName());
                deserializeErrCode = ErrorCode.InvalidJsonBNonStaticInnerClass;
                createJsonbNoArgConstructorDiagnostics(context, uri, diagnostics, type, deSerializeMsg, deserializeErrCode);
            }
            if (isStaticInner && missingChildNoArgs)
                createJsonbNoArgConstructorDiagnostics(context, uri, diagnostics, type, deSerializeMsg, deserializeErrCode);
        }
    }

    /**
     * @param context
     * @param uri
     * @param diagnostics
     * @param type
     * @throws JavaModelException
     * @description Method creates diagnostics with appropriate message and cursor context
     */
    private void createJsonbNoArgConstructorDiagnostics(JavaDiagnosticsContext context, String uri,
                                                        List<Diagnostic> diagnostics, IType type, String msg, IJavaErrorCode deserializeErrCode) throws JavaModelException {
        Range range = PositionUtils.toNameRange(type, context.getUtils());
        if (msg.contains(NON_STATIC_DIAGNOSTIC)) {
            diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                     deserializeErrCode, DiagnosticSeverity.Warning));
        } else {
            diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                     deserializeErrCode, DiagnosticSeverity.Error));
        }
    }

    /**
     * @param uniquePropertyNames
     * @param context
     * @param uri
     * @param diagnostics
     * @param type
     * @param propertyNames
     * @throws JavaModelException
     * @description Method to collect JsonbProperty uniqueness diagnostics
     */
    private void collectJsonbPropertyUniquenessDiagnostics(ICompilationUnit unit, Set<String> uniquePropertyNames,
                                                           JavaDiagnosticsContext context, String uri, List<Diagnostic> diagnostics, IType type) throws JavaModelException {
        Set<IType> hierarchy = new LinkedHashSet<>();
        TypeHierarchyUtils.collectSuperTypes(type, hierarchy);
        Map<String, List<IField>> jsonbMap = buildPropertyMap(uniquePropertyNames, hierarchy, unit);
        for (Map.Entry<String, List<IField>> entry : jsonbMap.entrySet()) { // Iterates through set of all key values pairs inside the map
            List<IField> fields = entry.getValue();
            if (fields.size() > Constants.MAX_DUPLICATE_PROPERTY_COUNT) {
                for (IField f : fields) {
                    if (f.getDeclaringType().equals(type)) // Creates diagnostics in the subclass
                        createJsonbPropertyUniquenessDiagnostics(context, uri, diagnostics, f, type);
                }
            }
        }
    }

    /**
     * @param uniquePropertyNames
     * @param hierarchy
     * @return Map<String, List<IField>> jsonbMap
     * @throws JavaModelException
     * @description This method collects the property name and fields using the same name if it's duplicated and builds it into a Map.
     */
    private Map<String, List<IField>> buildPropertyMap(Set<String> uniquePropertyNames, Set<IType> hierarchy, ICompilationUnit unit) throws JavaModelException {
        Map<String, List<IField>> jsonbMap = new HashMap<>();
        for (IType finaltype : hierarchy) {
            for (IField field : finaltype.getFields()) { // Iterates through all fields in super and subclass
                for (IAnnotation annotation : field.getAnnotations()) {
                    if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.JSONB_PROPERTY)) {
                        String propertyName = JsonPropertyUtils.extractPropertyNameFromJsonField(annotation);
                        if (propertyName != null) {
                            propertyName = JsonPropertyUtils.decodeUniCodeName(propertyName);
                            if (uniquePropertyNames.contains(propertyName)) {
                                // Checks if the propertyName exists, if not, creates a new key for the property with List<IField> as value.
                                // If it exists, add the field into the list.
                                jsonbMap.computeIfAbsent(propertyName, k -> new ArrayList<>()).add(field);
                            }
                        }
                    }
                }
            }
        }
        return jsonbMap;
    }

    /**
     * @param context
     * @param uri
     * @param diagnostics
     * @param type
     * @param propertyNames
     * @param field
     * @return List<String>
     * @throws JavaModelException
     * @description Method collects distinct property name values to be referenced for finding duplicates
     */
    private Set<String> collectJsonbUniquePropertyNames(ICompilationUnit unit, JavaDiagnosticsContext context, String uri,
                                                        List<Diagnostic> diagnostics, IType type, Set<String> propertyNames, IField field) throws JavaModelException {
        for (IAnnotation annotation : field.getAnnotations()) {
            if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.JSONB_PROPERTY)) { // Checks whether annotation is JsonbProperty
                String propertyName = JsonPropertyUtils.extractPropertyNameFromJsonField(annotation);
                if (propertyName != null) {
                    propertyName = JsonPropertyUtils.decodeUniCodeName(propertyName);
                    propertyNames.add(propertyName);
                }
            }
        }
        return propertyNames;
    }

    /**
     * @param context
     * @param uri
     * @param diagnostics
     * @param field
     * @param type
     * @throws JavaModelException
     * @description Method creates diagnostics with appropriate message and cursor context
     */
    private void createJsonbPropertyUniquenessDiagnostics(JavaDiagnosticsContext context, String uri,
                                                          List<Diagnostic> diagnostics, IField field, IType type) throws JavaModelException {
        String msg = Messages.getMessage("ErrorMessageJsonbPropertyUniquenessField");
        List<String> jsonbAnnotationsForField = getJsonbAnnotationNames(type, field);
        Range range = PositionUtils.toNameRange(field, context.getUtils());
        diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                 (JsonArray) (new Gson().toJsonTree(jsonbAnnotationsForField)),
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