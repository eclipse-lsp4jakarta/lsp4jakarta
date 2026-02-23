/*******************************************************************************
* Copyright (c) 2024, 2026 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;

/**
 * Provides Managed Bean related utilities.
 */
public class ManagedBean {

    /**
     * Constants.
     */
    public static final String MANAGED_BEAN_ANNOTATION = "javax.annotation.ManagedBean";
    public static final String VETOED_ANNOTATION = "jakarta.enterprise.inject.Vetoed";
    public static final String DECORATOR_ANNOTATION = "jakarta.decorator.Decorator";
    public static final String INJECT_ANNOTATION = "jakarta.inject.Inject";
    public static final String EXTENSION_SERVICE_IFACE = "jakarta.enterprise.inject.spi.Extension";

    /**
     * Returns true if the input type object is a managed bean. False, otherwise.
     * See: <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#what_classes_are_beans">CDI Specification</a>
     *
     * @param type The type object to process.
     *
     * @return True if the input type object is a managed bean. False, otherwise.
     *
     * @throws JavaModelException
     */
    public static boolean isManagedBean(IType type) throws JavaModelException {
        // Check if the type is an inner class.;
        if (isInnerClass(type)) {
            return false;
        }

        // Check if the type is an abstract class or is not annotated with @Decorator.
        if (isAbstractClass(type) && !isAnnotatedClass(type, DECORATOR_ANNOTATION)) {
            return false;
        }

        // Check if the type implements jakarta.enterprise.inject.spi.Extension
        if (implementsExtends(type, EXTENSION_SERVICE_IFACE)) {
            return false;
        }

        // Check if the type is annotated @Vetoed or in a package annotated @Vetoed.
        if (isAnnotatedClass(type, VETOED_ANNOTATION) || isPackageMetadataAnnotated(type, VETOED_ANNOTATION)) {
            return false;
        }

        // Check if the type does not have a constructor with no parameters or the class declares a constructor that is not annotated @Inject.
        if (!containsValidConstructor(type)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the input variable is a managed bean.
     *
     * @param variable The method/initializer local variable object to process.
     *
     * @return True if the input variable is a managed bean.
     *
     * @throws JavaModelException
     */
    public static boolean isManagedBean(ILocalVariable variable) throws JavaModelException {
        IType variableType = variableSignatureToType(variable);
        return isManagedBean(variableType);
    }

    /**
     * Converts an ILocalVariable object to an IType object.
     *
     * @param variable The local variable object to convert.
     *
     * @return The IType object representing the input ILocalVariable object.
     */
    public static IType variableSignatureToType(ILocalVariable variable) {
        IType varType = null;

        IMember declaringMember = variable.getDeclaringMember();
        if (declaringMember != null) {
            IType declaringType = declaringMember.getDeclaringType();
            try {
                String typeSig = variable.getTypeSignature();
                String typeName = Signature.toString(typeSig);
                varType = getChildITypeByName(declaringType, typeName);
                if (varType == null) {
                    IJavaProject jProject = declaringType.getJavaProject();
                    varType = jProject.findType(typeName);
                    String sn = Signature.toString(typeSig);
                    varType = declaringType.getType(sn);
                }
            } catch (Exception e) {
                JakartaCorePlugin.logException("Unable to convert an ILocalVariable to IType", e);
            }
        }

        return varType;
    }

    /**
     * Returns true if the class represented by the input type object is an inner class. False, otherwise.
     *
     * @param type The type object to check.
     *
     * @return True if the class represented by the input type object is an inner class. False, otherwise.
     *
     * @throws JavaModelException
     */
    public static boolean isInnerClass(IType type) throws JavaModelException {
        // A Non-static member class is an inner class.
        return type.isMember() && !Flags.isStatic(type.getFlags());
    }

    /**
     * Returns true if the class represented by the input type object is an abstract class. False, otherwise.
     *
     * @param iType The type object to check.
     * @return True if the class represented by the input type object is an abstract class. False, otherwise.
     *
     * @throws JavaModelException
     */
    public static boolean isAbstractClass(IType iType) throws JavaModelException {
        boolean isAbstractClass = Flags.isAbstract(iType.getFlags());
        return isAbstractClass;
    }

    /**
     * Returns true if the class represented by the input type object has an annotation that matches
     * the input annotation. False, otherwise.
     *
     * @param iType The type object to check.
     * @param annotation The annotation to find.
     *
     * @return True if the class represented by the input type object has an annotation that matches
     *         the input annotation. False, otherwise.
     *
     * @throws JavaModelException
     */
    public static boolean isAnnotatedClass(IType iType, String annotation) throws JavaModelException {
        IAnnotation[] annotations = iType.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            String[][] resolvedType = iType.resolveType(annotations[i].getElementName());
            if (resolvedType != null && resolvedType.length != 0) {
                String[] annotationParts = resolvedType[0];
                String resolvedAnnotation = String.join(".", annotationParts);
                if (annotation.equals(resolvedAnnotation)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if the class represented by the input type object or its parent type object(s) implement
     * or extends the input type . False, otherwise.
     *
     * @param type The type object to check.
     * @param objName The interface to find.
     *
     * @return True if the class represented by the input type object or its parent type object(s) implement
     *         or extends the input type . False, otherwise.
     *
     * @throws JavaModelException
     */
    public static boolean implementsExtends(IType type, String name) throws JavaModelException {

        String resolvedClassName = getFullyQualifiedClassName(type, type.getElementName());
        if (resolvedClassName != null) {
            if (name.equals(resolvedClassName)) {
                return true;
            }
        }

        String[] interfaces = type.getSuperInterfaceNames();
        for (int i = 0; i < interfaces.length; i++) {
            IType childType = getChildITypeByName(type, interfaces[i]);
            if (childType != null) {
                return implementsExtends(childType, name);
            }
        }

        String superClass = type.getSuperclassName();
        if (superClass != null) {
            IType childType = getChildITypeByName(type, superClass);
            if (childType != null) {
                return implementsExtends(childType, name);
            }
        }

        return false;
    }

    /**
     * Returns true if the package in which the input type class resides is annotated
     * with the specified annotation (package-info.java). False, otherwise.
     *
     * @param type The type object to check.
     * @param annotation The annotation to find.
     *
     * @return True if the package in which the input type class resides is annotated
     *         with the specified annotation (package-info.java). False, otherwise.
     */
    public static boolean isPackageMetadataAnnotated(IType type, String annotation) {
        IPackageFragment packageFragment = type.getPackageFragment();
        ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("package-info.java");
        if (compilationUnit.exists()) {
            IPackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration(packageFragment.getElementName());
            IAnnotation foundAnnotation = packageDeclaration.getAnnotation(annotation);

            if (foundAnnotation != null && !foundAnnotation.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of constructors associated with the class represented by the input type object.
     *
     * @param type The type object to check.
     *
     * @return A list of constructors associated with the class represented by the input type object.
     *
     * @throws JavaModelException
     */
    public static ArrayList<IMethod> getConstructors(IType type) throws JavaModelException {
        ArrayList<IMethod> constructors = new ArrayList<IMethod>();
        IMethod[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            boolean isConstructor = methods[i].isConstructor();
            if (isConstructor) {
                constructors.add(methods[i]);
            }
        }
        return constructors;

    }

    /**
     * Returns true if the class represented by the input type object contains a default constructor or
     * a constructor with the jakarta.inject.Inject annotation.
     *
     * @param type The type object to check.
     *
     * @return True if the class represented by the input type object contains a default constructor or
     *         a constructor with the jakarta.inject.Inject annotation.
     *
     * @throws JavaModelException
     */
    public static boolean containsValidConstructor(IType type) throws JavaModelException {
        List<IMethod> constructors = getConstructors(type);

        for (IMethod constructor : constructors) {
            if (constructor.getNumberOfParameters() == 0) {
                return true;
            }
            IAnnotation injectAnnotation = constructor.getAnnotation(INJECT_ANNOTATION);
            if (injectAnnotation != null && injectAnnotation.exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the IType associated with the input class name or null if not found.
     *
     * @param parentType The parent type object to check.
     * @param simpleName The simple name of the class within the specified parent type.
     *
     * @return The IType associated with the input class name or null if not found.
     *
     * @throws JavaModelException
     */
    public static IType getChildITypeByName(IType parentType, String simpleName) throws JavaModelException {
        IType iType = null;
        String resolvedClassName = getFullyQualifiedClassName(parentType, simpleName);

        if (resolvedClassName != null) {
            IJavaProject jProject = parentType.getJavaProject();
            iType = jProject.findType(resolvedClassName);
        }

        return iType;
    }

    /**
     * Returns a fully qualified class name based on the input class name or null if the simple name cannot be resolved.
     *
     * @param parentType The parent type object to check.
     * @param simpleName The simple name of the class within the specified parent type.
     *
     * @return The IType associated with the input class name or null if not found.
     *
     * @throws JavaModelException
     */
    public static String getFullyQualifiedClassName(IType parentType, String simpleName) throws JavaModelException {
        String resolvedClassName = null;
        String[][] resolvedType = parentType.resolveType(simpleName);

        if (resolvedType != null && resolvedType.length != 0) {
            String[] classParts = resolvedType[0];
            resolvedClassName = String.join(".", classParts);
        }

        return resolvedClassName;
    }

    /**
     * Checks if the given annotation is marked with a specific meta-annotation.
     *
     * @param annotation the annotation to check
     * @param type the type context for resolving the annotation
     * @param cu the compilation unit
     * @param metaAnnotationFQN the fully qualified name of the meta-annotation to
     *            look for
     * @return true if the annotation has the specified meta-annotation, false
     *         otherwise
     * @throws JavaModelException if there's an error accessing the Java model
     */
    public static boolean hasMetaAnnotation(IAnnotation annotation, IType type, ICompilationUnit cu,
                                            String metaAnnotationFQN) throws JavaModelException {
        String annotationFQ = ManagedBean.getFullyQualifiedClassName(type, annotation.getElementName());
        IJavaProject project = annotation.getJavaProject();

        if (project == null || annotationFQ == null) {
            return false;
        }

        IType annotationType = project.findType(annotationFQ);
        if (annotationType == null) {
            return false;
        }

        return Arrays.stream(annotationType.getAnnotations()).anyMatch(metaAnnotation -> {
            try {
                return DiagnosticUtils.isMatchedAnnotation(cu, metaAnnotation, metaAnnotationFQN);
            } catch (JavaModelException e) {
                return false;
            }
        });
    }
}
