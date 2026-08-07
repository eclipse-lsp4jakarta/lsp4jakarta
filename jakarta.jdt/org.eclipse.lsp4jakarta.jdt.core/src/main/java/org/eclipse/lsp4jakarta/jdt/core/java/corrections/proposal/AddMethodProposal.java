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
package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.lsp4j.CodeActionKind;

/**
 * Code action proposal for adding a method to a class.
 *
 * <p>Supports configurable return type, modifier, method-level annotations,
 * and a list of parameters (each optionally wrapping its type in a generic
 * type argument).
 */
@SuppressWarnings("restriction")
public class AddMethodProposal extends ASTRewriteCorrectionProposal {

    /**
     * Describes a single method parameter.
     *
     * <p>A parameter always has a fully-qualified type name and a variable name.
     * When {@code typeArgFQName} is non-{@code null} the type is rendered as a
     * parameterised type: {@code paramTypeFQName<typeArgFQName>}.
     */
    public static class MethodParam {

        /** Fully-qualified type of the parameter (or raw type when generic). */
        public final String typeFQName;

        /** Variable name used in the generated source. */
        public final String name;

        /**
         * Optional fully-qualified type argument that turns {@code typeFQName} into a
         * parameterised type, e.g. {@code EventContext<AuditEvent>}.
         * {@code null} means use {@code typeFQName} as a simple (non-generic) type.
         */
        public final String typeArgFQName;

        /**
         * Creates a simple (non-generic) parameter.
         *
         * @param typeFQName fully-qualified type name
         * @param name variable name used in generated source
         */
        public MethodParam(String typeFQName, String name) {
            this(typeFQName, name, null);
        }

        /**
         * Creates a parameter, optionally with a type argument.
         *
         * @param typeFQName fully-qualified type name (or raw type when generic)
         * @param name variable name used in generated source
         * @param typeArgFQName if non-{@code null}, wraps {@code typeFQName} as
         *            {@code typeFQName<typeArgFQName>}
         */
        public MethodParam(String typeFQName, String name, String typeArgFQName) {
            this.typeFQName = typeFQName;
            this.name = name;
            this.typeArgFQName = typeArgFQName;
        }
    }

    private final CompilationUnit invocationNode;
    private final IBinding binding;

    /** Simple name of the method to insert (e.g. {@code "notify"}). */
    private final String methodName;

    /**
     * Fully-qualified return type name (e.g. {@code "void"}, {@code "java.lang.String"}).
     * Use {@code "void"} for methods with no return value.
     */
    private final String returnTypeFQName;

    /**
     * Visibility modifier keyword (e.g. {@code "public"}, {@code "protected"},
     * {@code "private"}). May be {@code null} or empty for package-private.
     */
    private final String modifier;

    /**
     * Ordered list of fully-qualified annotation names to prepend to the method
     * (e.g. {@code "java.lang.Override"}). May be {@code null} or empty.
     */
    private final List<String> annotationFQNames;

    /**
     * Ordered list of parameters to declare on the generated method.
     * May be {@code null} or empty for a no-arg method.
     */
    private final List<MethodParam> params;

    /**
     * Creates an {@code AddMethodProposal}.
     *
     * @param label the label shown in the quick-fix menu
     * @param targetCU the compilation unit to modify
     * @param invocationNode the compilation unit AST root
     * @param binding the type binding of the class to modify
     * @param relevance relevance ordering hint
     * @param methodName simple name of the method to insert
     * @param returnTypeFQName fully-qualified return type (use {@code "void"} for void methods)
     * @param modifier visibility modifier keyword (e.g. {@code "public"}), or {@code null} for package-private
     * @param annotationFQNames list of fully-qualified annotation names to prepend, or {@code null}
     * @param params list of method parameters, or {@code null} for a no-arg method
     */
    public AddMethodProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                             IBinding binding, int relevance,
                             String methodName, String returnTypeFQName, String modifier,
                             List<String> annotationFQNames, List<MethodParam> params) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
        this.methodName = methodName;
        this.returnTypeFQName = returnTypeFQName;
        this.modifier = modifier;
        this.annotationFQNames = annotationFQNames;
        this.params = params;
    }

    /**
     * Constructs and returns the {@link ASTRewrite} that inserts the new method
     * declaration into the target class body.
     *
     * <p>The generated method has the form:
     *
     * <pre>
     *   &#64;Annotation1 &#64;Annotation2     // if annotations are specified
     *   [modifier] [returnType] &lt;methodName&gt;([paramType paramName], ...) {}
     * </pre>
     *
     * Required imports are added automatically.
     *
     * @return the AST rewrite containing the new method insertion
     * @throws CoreException if the AST cannot be resolved or the rewrite cannot be created
     */
    @SuppressWarnings("unchecked")
    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        ASTNode declNode = null;
        ASTNode boundNode = invocationNode.findDeclaringNode(binding);
        CompilationUnit newRoot = invocationNode;

        if (boundNode != null) {
            declNode = boundNode;
        } else {
            newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
            declNode = newRoot.findDeclaringNode(binding.getKey());
        }

        AST ast = declNode.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        ImportRewrite imports = createImportRewrite(newRoot);
        ImportRewriteContext importContext = new ContextSensitiveImportRewriteContext(declNode, imports);

        // Build the method declaration.
        MethodDeclaration md = ast.newMethodDeclaration();
        md.setName(ast.newSimpleName(methodName));

        // Return type.
        if ("void".equals(returnTypeFQName)) {
            md.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        } else {
            String importedReturn = imports.addImport(returnTypeFQName, importContext);
            md.setReturnType2(ast.newSimpleType(ast.newName(importedReturn)));
        }

        // Annotations.
        if (annotationFQNames != null) {
            for (String annotFQName : annotationFQNames) {
                MarkerAnnotation annotation = ast.newMarkerAnnotation();
                String simpleName = annotFQName.substring(annotFQName.lastIndexOf('.') + 1);
                annotation.setTypeName(ast.newSimpleName(simpleName));
                md.modifiers().add(annotation);
            }
        }

        // Modifier.
        if (modifier != null && !modifier.isEmpty()) {
            Modifier.ModifierKeyword keyword = Modifier.ModifierKeyword.toKeyword(modifier);
            if (keyword != null) {
                md.modifiers().add(ast.newModifier(keyword));
            }
        }

        // Parameters.
        if (params != null) {
            for (MethodParam p : params) {
                String importedType = imports.addImport(p.typeFQName, importContext);
                Type paramType;
                if (p.typeArgFQName != null) {
                    String importedTypeArg = imports.addImport(p.typeArgFQName, importContext);
                    SimpleType rawType = ast.newSimpleType(ast.newName(importedType));
                    ParameterizedType parameterizedType = ast.newParameterizedType(rawType);
                    parameterizedType.typeArguments().add(ast.newSimpleType(ast.newName(importedTypeArg)));
                    paramType = parameterizedType;
                } else {
                    paramType = ast.newSimpleType(ast.newName(importedType));
                }
                SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
                param.setType(paramType);
                param.setName(ast.newSimpleName(p.name));
                md.parameters().add(param);
            }
        }

        Block body = ast.newBlock();
        md.setBody(body);

        // Insert the method into the class body.
        ListRewrite listRewrite = rewrite.getListRewrite(declNode, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertLast(md, null);

        return rewrite;
    }
}
