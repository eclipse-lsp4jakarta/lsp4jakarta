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
 * Code action proposal for adding a public void method with a single parameter
 * to a class.
 *
 * <p>Used to insert required {@code notify} overrides on custom
 * {@code ObserverMethod} implementations.
 */
@SuppressWarnings("restriction")
public class AddMethodProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final IBinding binding;

    /** Simple name of the method to insert (e.g. {@code "notify"}). */
    private final String methodName;

    /**
     * Fully-qualified type name of the single method parameter
     * (e.g. {@code "jakarta.enterprise.event.EventContext"}).
     */
    private final String paramTypeFQName;

    /** Simple display name used as the parameter name in the generated source. */
    private final String paramName;

    /**
     * Optional FQ name of a type argument to wrap {@code paramTypeFQName} in a
     * parameterized type, e.g. {@code EventContext<AuditEvent>}.
     * {@code null} means use {@code paramTypeFQName} as a simple (non-generic) type.
     */
    private final String typeArgFQName;

    /** Whether to prepend {@code @Override} to the inserted method. */
    private final boolean addOverride;

    /**
     * Creates an {@code AddMethodProposal} with a simple (non-generic) parameter type
     * and no {@code @Override} annotation.
     *
     * @param label the label shown in the quick-fix menu
     * @param targetCU the compilation unit to modify
     * @param invocationNode the compilation unit AST root
     * @param binding the type binding of the class to modify
     * @param relevance relevance ordering hint
     * @param methodName simple name of the method to insert
     * @param paramTypeFQName fully-qualified type name of the single parameter
     * @param paramName parameter variable name used in the generated source
     */
    public AddMethodProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                             IBinding binding, int relevance,
                             String methodName, String paramTypeFQName, String paramName) {
        this(label, targetCU, invocationNode, binding, relevance, methodName, paramTypeFQName, paramName, null, false);
    }

    /**
     * Creates an {@code AddMethodProposal}.
     *
     * @param label the label shown in the quick-fix menu
     * @param targetCU the compilation unit to modify
     * @param invocationNode the compilation unit AST root
     * @param binding the type binding of the class to modify
     * @param relevance relevance ordering hint
     * @param methodName simple name of the method to insert
     * @param paramTypeFQName fully-qualified type name of the parameter (or raw type for generic)
     * @param paramName parameter variable name to use in the generated source
     * @param typeArgFQName if non-null, wraps {@code paramTypeFQName} as {@code paramType<typeArg>}
     * @param addOverride if {@code true}, prepends {@code @Override} to the method
     */
    public AddMethodProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                             IBinding binding, int relevance,
                             String methodName, String paramTypeFQName, String paramName,
                             String typeArgFQName, boolean addOverride) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
        this.methodName = methodName;
        this.paramTypeFQName = paramTypeFQName;
        this.paramName = paramName;
        this.typeArgFQName = typeArgFQName;
        this.addOverride = addOverride;
    }

    /**
     * Constructs and returns the {@link ASTRewrite} that inserts the new method
     * declaration into the target class body.
     *
     * <p>The generated method has the form:
     *
     * <pre>
     *   &#64;Override                        // if addOverride is true
     *   public void &lt;methodName&gt;(&lt;paramType&gt; &lt;paramName&gt;) {}
     * </pre>
     *
     * where {@code <paramType>} is the simple name of {@code paramTypeFQName} for a
     * plain type, or {@code RawType<TypeArg>} when {@link #typeArgFQName} is set.
     * The required imports are added automatically.
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

        // Build the parameter type (adding import as needed).
        String importedType = imports.addImport(paramTypeFQName, importContext);
        Type paramType;
        if (typeArgFQName != null) {
            // Build a parameterized type: EventContext<AuditEvent>
            String importedTypeArg = imports.addImport(typeArgFQName, importContext);
            SimpleType rawType = ast.newSimpleType(ast.newName(importedType));
            ParameterizedType parameterizedType = ast.newParameterizedType(rawType);
            parameterizedType.typeArguments().add(ast.newSimpleType(ast.newName(importedTypeArg)));
            paramType = parameterizedType;
        } else {
            paramType = ast.newSimpleType(ast.newName(importedType));
        }

        // Build the method declaration.
        MethodDeclaration md = ast.newMethodDeclaration();
        md.setName(ast.newSimpleName(methodName));
        md.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        if (addOverride) {
            MarkerAnnotation overrideAnnotation = ast.newMarkerAnnotation();
            overrideAnnotation.setTypeName(ast.newSimpleName("Override"));
            md.modifiers().add(overrideAnnotation);
        }
        md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

        SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
        param.setType(paramType);
        param.setName(ast.newSimpleName(paramName));
        md.parameters().add(param);

        Block body = ast.newBlock();
        md.setBody(body);

        // Insert the method into the class body.
        ListRewrite listRewrite = rewrite.getListRewrite(declNode, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertLast(md, null);

        return rewrite;
    }
}
