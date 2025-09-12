/*******************************************************************************
 * Copyright (c) 2022, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;

/**
 *
 * Abstract class for collecting Java diagnostics.
 *
 */
@SuppressWarnings("restriction")
public class DiagnosticUtils {

    private static final String LEVEL1_URI_REGEX = "(?:\\/(?:(?:\\{(\\w|-|%20|%21|%23|%24|%25|%26|%27|%28|%29|%2A|%2B|%2C|%2F|%3A|%3B|%3D|%3F|%40|%5B|%5D)+\\})|(?:(\\w|%20|%21|%23|%24|%25|%26|%27|%28|%29|%2A|%2B|%2C|%2F|%3A|%3B|%3D|%3F|%40|%5B|%5D)+)))*\\/?";

    /**
     * Returns true if the given annotation matches the given annotation name and
     * false otherwise.
     *
     * @param unit compilation unit of Java class.
     * @param annotation given annotation object.
     * @param annotationFQName the fully qualified annotation name.
     * @return true if the given annotation matches the given annotation name and
     *         false otherwise.
     */
    public static boolean isMatchedAnnotation(ICompilationUnit unit, IAnnotation annotation, String annotationFQName) throws JavaModelException {
        String elementName = annotation.getElementName();
        if (nameEndsWith(annotationFQName, elementName) && unit != null) {
            // For performance reason, we check if the import of annotation name is
            // declared
            if (isImportedJavaElement(unit, annotationFQName) == true)
                return true;
            // only check fully qualified annotations
            if (annotationFQName.equals(elementName)) {
                IJavaElement parent = annotation.getParent();
                IType declaringType = (parent instanceof IType) ? (IType) parent : ((parent instanceof IMember) ? ((IMember) parent).getDeclaringType() : null);
                if (declaringType != null) {
                    String[][] fqName = declaringType.resolveType(elementName); // the call could be expensive
                    if (fqName != null && fqName.length == 1) {
                        return annotationFQName.equals(JavaModelUtil.concatenateName(fqName[0][0], fqName[0][1]));
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the java element name matches the given fully qualified java
     * element name and false otherwise.
     *
     * @param unit compilation unit of Java class.
     * @param annotation given annotation object.
     * @param annotationFQName the fully qualified annotation name.
     * @return true if the java element name matches the given fully qualified java
     *         element name and false otherwise.
     */
    public static boolean isMatchedJavaElement(IType type, String javaElementName, String javaElementFQName) throws JavaModelException {
        if (nameEndsWith(javaElementFQName, javaElementName)) {
            // For performance reason, we check if the import of annotation name is
            // declared
            if (isImportedJavaElement(type.getCompilationUnit(), javaElementFQName) == true)
                return true;
            // only check fully qualified java element
            // The second condition handles implicit java.lang types, which don't require explicit imports.
            if (javaElementFQName.equals(javaElementName) || javaElementFQName.startsWith("java.lang")) {
                String[][] fqName = type.resolveType(javaElementName); // the call could be expensive
                if (fqName != null && fqName.length == 1) {
                    return javaElementFQName.equals(JavaModelUtil.concatenateName(fqName[0][0], fqName[0][1]));
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given Java class imports the given Java element and false
     * otherwise.
     *
     * @param type Java class.
     * @param javaElementFQName given Java element fully qualified name.
     * @return true if the Java class imports the given Java element and false
     *         otherwise.
     */
    public static boolean isImportedJavaElement(ICompilationUnit unit, String javaElementFQName) throws JavaModelException {

        if (!unit.isOpen()) {
            unit.open(null);
        }

        IImportContainer container = unit.getImportContainer();
        if (container == null) {
            return false;
        }

        IImportDeclaration[] importDeclArray = unit.getImports();

        for (IImportDeclaration importDeclaration : importDeclArray) {
            if (importDeclaration.isOnDemand()) {
                String fqn = importDeclaration.getElementName();
                String qualifier = fqn.substring(0, fqn.lastIndexOf('.'));
                if (qualifier.equals(javaElementFQName.substring(0, javaElementFQName.lastIndexOf('.')))) {
                    return true;
                }
            } else if (importDeclaration.getElementName().equals(javaElementFQName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given Java class imports one of the given Java elements
     * and false otherwise.
     *
     * @param type Java class.
     * @param javaElementFQName given Java element fully qualified names.
     * @return true if the Java class imports one of the given Java elements and
     *         false otherwise.
     */
    protected static boolean isImportedJavaElement(ICompilationUnit unit, String[] javaElementFQNames) throws JavaModelException {
        if (!unit.isOpen()) {
            unit.open(null);
        }

        IImportContainer container = unit.getImportContainer();
        if (container == null) {
            return false;
        }

        IImportDeclaration[] importDeclArray = unit.getImports();

        for (IImportDeclaration importDeclaration : importDeclArray) {
            if (importDeclaration.isOnDemand()) {
                String fqn = importDeclaration.getElementName();
                String qualifier = fqn.substring(0, fqn.lastIndexOf('.'));
                boolean imports = Stream.of(javaElementFQNames).anyMatch(elementFQName -> {
                    return qualifier.equals(elementFQName.substring(0, elementFQName.lastIndexOf('.')));
                });
                if (imports == true) {
                    return true;
                }
            } else {
                String importName = importDeclaration.getElementName();
                if (Stream.of(javaElementFQNames).anyMatch(elementFQName -> importName.equals(elementFQName)) == true)
                    return true;
            }
        }
        return false;

    }

    /**
     * Returns true if the given Java class implements one of the given interfaces
     * and false otherwise.
     *
     * @param type Java class.
     * @param interfaceFQNames given interfaces with fully qualified name.
     * @return true if the Java class implements one of the given interfaces and
     *         false otherwise.
     */
    public static boolean doesImplementInterfaces(IType type, String[] interfaceFQNames) throws JavaModelException {
        String[] interfaceNames = type.getSuperInterfaceNames();

        // should check import statements first for the performance?

        // check super hierarchy
        if (interfaceNames.length > 0) { // the type implements interface(s)
            ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
            IType[] interfaces = typeHierarchy.getAllInterfaces();
            for (IType interfase : interfaces) {
                String fqName = interfase.getFullyQualifiedName();
                if (Stream.of(interfaceFQNames).anyMatch(name -> fqName.equals(name)) == true)
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns matched Java element fully qualified name.
     *
     * @param type Java class.
     * @param javaElement Java element name
     * @param javaElementFQNames given fully qualified name array.
     * @return Matched fully qualified name and null otherwise.
     */
    public static String getMatchedJavaElementName(IType type, String javaElementName, String[] javaElementFQNames) throws JavaModelException {
        String[] matches = (String[]) Stream.of(javaElementFQNames).filter(fqName -> nameEndsWith(fqName, javaElementName)).toArray(String[]::new);
        if (matches.length > 0) {
            if (isMatchedJavaElement(type, javaElementName, matches[0]) == true) // only check the first one for now
                return matches[0];
        }
        return null;
    }

    /**
     * Returns matched Java element fully qualified names.
     *
     * @param type the type representing the class
     * @param javaElementNames Java element names
     * @param javaElementFQNames given fully qualified name array
     * @return matched Java element fully qualified names
     */
    public static List<String> getMatchedJavaElementNames(IType type, String[] javaElementNames,
                                                          String[] javaElementFQNames) {
        return Stream.of(javaElementFQNames).filter(fqName -> {
            boolean anyMatch = Stream.of(javaElementNames).anyMatch(name -> {
                try {
                    return isMatchedJavaElement(type, name, fqName);
                } catch (JavaModelException e) {
                    JakartaCorePlugin.logException("Failed to get matched Java element FQ names", e);
                    return false;
                }
            });
            return anyMatch;
        }).collect(Collectors.toList());
    }

    /**
     * Returns true if the given fully qualified name ends with the given name and
     * false otherwise
     *
     * @param fqName fully qualified name
     * @param name either simple name or fully qualified name
     * @return true if the given fully qualified name ends with the given name and
     *         false otherwise
     */
    protected static boolean nameEndsWith(String fqName, String name) {
        // add a prefix '.' to simple name
        // e.g. 'jakarta.validation.constraints.DecimalMin' should NOT end with 'Min'
        // here
        return fqName.equals(name) || fqName.endsWith("." + name);
    }

    /**
     * Returns simple name for the given fully qualified name.
     *
     * @param fqName a fully qualified name or simple name
     * @return simple name for given fully qualified name
     */
    public static String getSimpleName(String fqName) {
        int idx = fqName.lastIndexOf('.');
        if (idx != -1 && idx != fqName.length() - 1) {
            return fqName.substring(idx + 1);
        }
        return fqName;
    }

    /**
     * Returns true if the given method is a constructor and false otherwise.
     *
     * @param m method
     * @return true if the given method is a constructor and false otherwise
     */
    public static boolean isConstructorMethod(IMethod m) {
        try {
            return m.isConstructor();
        } catch (JavaModelException e) {
            JakartaCorePlugin.logException("Failed to check constructor method", e);
            return false;
        }
    }

    /**
     * Returns a list of all accessors (getter and setter) of the given field.
     * Note that for boolean fields the accessor of the form "isField" is retuned
     * "getField" is not present.
     *
     * @param unit the compilation unit the field belongs to
     * @param field the accesors of this field are returned
     * @return a list of accessor methods
     * @throws JavaModelException
     */
    public static List<IMethod> getFieldAccessors(ICompilationUnit unit, IField field) throws JavaModelException {
        List<IMethod> accessors = new ArrayList<IMethod>();
        String fieldName = field.getElementName();
        fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        List<String> accessorNames = new ArrayList<String>();
        accessorNames.add("get" + fieldName);
        accessorNames.add("set" + fieldName);
        accessorNames.add("is" + fieldName);

        for (IType type : unit.getAllTypes()) {
            for (IMethod method : type.getMethods()) {
                String methodName = method.getElementName();
                if (accessorNames.contains(methodName))
                    accessors.add(method);
            }
        }
        return accessors;
    }

    /**
     * Returns true if the input URI starts with a leading slash.
     *
     * @param uri The string URI.
     * @return True if the input URI starts with a leading slash. False, otherwise.
     */
    public static boolean hasLeadingSlash(String uri) {
        return uri.startsWith("/");
    }

    /**
     * Returns true if the input URI represents a valid level 1 (URI template) path.
     * <a href="https://datatracker.ietf.org/doc/html/rfc6570">RFC 6570</a>.
     *
     * @param uriString The URI.
     * @return Returns true if the input URI represents a valid level 1 (URI
     *         template) path.
     */
    public static boolean isValidLevel1URI(String uriString) {
        return uriString.matches(LEVEL1_URI_REGEX);
    }
}
