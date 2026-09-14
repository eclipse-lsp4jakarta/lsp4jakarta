/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4jakarta.commons.DocumentFormat;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCompletionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsSettings;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfo;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfoParams;
import org.eclipse.lsp4jakarta.commons.JavaCursorContextKind;
import org.eclipse.lsp4jakarta.commons.JavaCursorContextResult;
import org.eclipse.lsp4jakarta.jdt.core.java.completion.JavaCompletionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.JavaFeaturesRegistry;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.codeaction.CodeActionHandler;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.completion.JavaCompletionDefinition;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.diagnostics.JavaDiagnosticsDefinition;

/**
 * JDT Jakarta manager for Java files.
 *
 * Based on: https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/core/PropertiesManagerForJava.java
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerForJava {

    private static final PropertiesManagerForJava INSTANCE = new PropertiesManagerForJava();

    private final CodeActionHandler codeActionHandler;

    public static PropertiesManagerForJava getInstance() {
        return INSTANCE;
    }

    private PropertiesManagerForJava() {
        this.codeActionHandler = new CodeActionHandler();
    }

    /**
     * Returns the CompletionItems given the completion item params
     *
     * @param params the completion item params
     * @param utils the IJDTUtils
     * @param monitor the progress monitors
     * @return the CompletionItems for the given the completion item params
     * @throws JavaModelException
     */
    public CompletionList completion(JakartaJavaCompletionParams params, IJDTUtils utils, IProgressMonitor monitor) throws JavaModelException {
        String uri = params.getUri();
        ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
        if (typeRoot == null) {
            return null;
        }

        Position completionPosition = params.getPosition();
        int completionOffset = utils.toOffset(typeRoot.getBuffer(), completionPosition.getLine(),
                                              completionPosition.getCharacter());

        List<CompletionItem> completionItems = new ArrayList<>();
        JavaCompletionContext completionContext = new JavaCompletionContext(uri, typeRoot, utils, completionOffset);

        List<JavaCompletionDefinition> completions = JavaFeaturesRegistry.getInstance().getJavaCompletionDefinitions().stream().filter(completion -> completion.isAdaptedForCompletion(completionContext,
                                                                                                                                                                                       monitor)).collect(Collectors.toList());

        if (completions.isEmpty()) {
            return null;
        }

        completions.forEach(completion -> {
            List<? extends CompletionItem> collectedCompletionItems = completion.collectCompletionItems(completionContext, monitor);
            if (collectedCompletionItems != null) {
                completionItems.addAll(collectedCompletionItems);
            }
        });

        if (monitor.isCanceled()) {
            return null;
        }
        CompletionList completionList = new CompletionList();
        completionList.setItems(completionItems);
        return completionList;
    }

    /**
     * Returns the cursor context for the given file and cursor position.
     *
     * @param params the completion params that provide the file and cursor
     *            position to get the context for
     * @param utils the jdt utils
     * @param monitor the progress monitor
     * @return the cursor context for the given file and cursor position
     * @throws JavaModelException when the buffer for the file cannot be accessed or
     *             the Java model cannot be accessed
     */
    public JavaCursorContextResult javaCursorContext(JakartaJavaCompletionParams params, IJDTUtils utils,
                                                     IProgressMonitor monitor) throws JavaModelException {
        String uri = params.getUri();
        ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);

        if (typeRoot == null) {
            return new JavaCursorContextResult(JavaCursorContextKind.IN_EMPTY_FILE, "");
        }
        CompilationUnit ast = ASTResolving.createQuickFixAST((ICompilationUnit) typeRoot, monitor);

        JavaCursorContextKind kind = getJavaCursorContextKind(params, typeRoot, ast, utils, monitor);
        String prefix = getJavaCursorPrefix(params, typeRoot, ast, utils, monitor);

        return new JavaCursorContextResult(kind, prefix);
    }

    private static JavaCursorContextKind getJavaCursorContextKind(JakartaJavaCompletionParams params,
                                                                  ITypeRoot typeRoot, CompilationUnit ast, IJDTUtils utils, IProgressMonitor monitor) throws JavaModelException {

        if (typeRoot.findPrimaryType() == null) {
            return JavaCursorContextKind.IN_EMPTY_FILE;
        }

        Position completionPosition = params.getPosition();
        int completionOffset = utils.toOffset(typeRoot.getBuffer(), completionPosition.getLine(),
                                              completionPosition.getCharacter());

        NodeFinder nodeFinder = new NodeFinder(ast, completionOffset, 0);
        ASTNode node = nodeFinder.getCoveringNode();
        ASTNode oldNode = node;
        while (node != null && (!(node instanceof AbstractTypeDeclaration)
                                || offsetOfFirstNonAnnotationModifier((BodyDeclaration) node) >= completionOffset)) {
            if (node.getParent() != null) {
                switch (node.getParent().getNodeType()) {
                    case ASTNode.METHOD_DECLARATION:
                    case ASTNode.FIELD_DECLARATION:
                    case ASTNode.ENUM_CONSTANT_DECLARATION:
                    case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
                        if (!ASTNodeUtils.isAnnotation(node) && node.getStartPosition() < completionOffset) {
                            return JavaCursorContextKind.NONE;
                        }
                        break;
                }
            }
            oldNode = node;
            node = node.getParent();
        }

        if (node == null) {
            // we are likely before or after the type root class declaration
            FindWhatsBeingAnnotatedASTVisitor visitor = new FindWhatsBeingAnnotatedASTVisitor(completionOffset, false);
            oldNode.accept(visitor);
            switch (visitor.getAnnotatedNodeType()) {
                case ASTNode.TYPE_DECLARATION:
                case ASTNode.ANNOTATION_TYPE_DECLARATION:
                case ASTNode.ENUM_DECLARATION:
                case ASTNode.RECORD_DECLARATION: {
                    if (visitor.isInAnnotations()) {
                        return JavaCursorContextKind.IN_CLASS_ANNOTATIONS;
                    }
                    return JavaCursorContextKind.BEFORE_CLASS;
                }
                default:
                    return JavaCursorContextKind.NONE;
            }
        }

        AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) node;
        FindWhatsBeingAnnotatedASTVisitor visitor = new FindWhatsBeingAnnotatedASTVisitor(completionOffset);
        typeDeclaration.accept(visitor);
        switch (visitor.getAnnotatedNodeType()) {
            case ASTNode.TYPE_DECLARATION:
            case ASTNode.ANNOTATION_TYPE_DECLARATION:
            case ASTNode.ENUM_DECLARATION:
            case ASTNode.RECORD_DECLARATION:
                return visitor.isInAnnotations() ? JavaCursorContextKind.IN_CLASS_ANNOTATIONS : JavaCursorContextKind.BEFORE_CLASS;
            case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
            case ASTNode.METHOD_DECLARATION:
                return visitor.isInAnnotations() ? JavaCursorContextKind.IN_METHOD_ANNOTATIONS : JavaCursorContextKind.BEFORE_METHOD;
            case ASTNode.FIELD_DECLARATION:
            case ASTNode.ENUM_CONSTANT_DECLARATION:
                return visitor.isInAnnotations() ? JavaCursorContextKind.IN_FIELD_ANNOTATIONS : JavaCursorContextKind.BEFORE_FIELD;
            default:
                return JavaCursorContextKind.IN_CLASS;
        }
    }

    private static @NonNull String getJavaCursorPrefix(JakartaJavaCompletionParams params, ITypeRoot typeRoot,
                                                       CompilationUnit ast, IJDTUtils utils, IProgressMonitor monitor) throws JavaModelException {
        Position completionPosition = params.getPosition();
        int completionOffset = utils.toOffset(typeRoot.getBuffer(), completionPosition.getLine(),
                                              completionPosition.getCharacter());

        String fileContents = null;
        try {
            IBuffer buffer = typeRoot.getBuffer();
            if (buffer == null) {
                return null;
            }
            fileContents = buffer.getContents();
        } catch (JavaModelException e) {
            return "";
        }
        if (fileContents == null) {
            return "";
        }
        int i;
        for (i = completionOffset; i > 0 && !Character.isWhitespace(fileContents.charAt(i - 1)); i--) {
        }
        return fileContents.substring(i, completionOffset);
    }

    /**
     * Given the uri returns a {@link ITypeRoot}. May return null if it can not
     * associate the uri with a Java file or class file.
     *
     * @param uri
     * @param utils JDT LS utilities
     * @param monitor the progress monitor
     * @return compilation unit
     */
    private static ITypeRoot resolveTypeRoot(String uri, IJDTUtils utils, IProgressMonitor monitor) {
        utils.waitForLifecycleJobs(monitor);
        final ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        IClassFile classFile = null;
        if (unit == null) {
            classFile = utils.resolveClassFile(uri);
            if (classFile == null) {
                return null;
            }
        } else {
            if (!unit.getResource().exists() || monitor.isCanceled()) {
                return null;
            }
        }
        return unit != null ? unit : classFile;
    }

    /**
     * Returns the codeAction list according the given codeAction parameters.
     *
     * @param params the codeAction parameters
     * @param utils the utilities class
     * @param monitor the monitor
     * @return the codeAction list according the given codeAction parameters.
     * @throws JavaModelException
     */
    public List<? extends CodeAction> codeAction(JakartaJavaCodeActionParams params, IJDTUtils utils,
                                                 IProgressMonitor monitor) throws JavaModelException {
        return codeActionHandler.codeAction(params, utils, monitor);
    }

    /**
     * Returns the codeAction list according the given codeAction parameters.
     *
     * @param unresolved the CodeAction to resolve
     * @param utils the utilities class
     * @param monitor the monitor
     * @return the codeAction list according the given codeAction parameters.
     * @throws JavaModelException
     */
    public CodeAction resolveCodeAction(CodeAction unresolved, IJDTUtils utils, IProgressMonitor monitor) throws JavaModelException {
        return codeActionHandler.resolveCodeAction(unresolved, utils, monitor);
    }

    /**
     * Returns diagnostics for the given uris list.
     *
     * @param params the diagnostics parameters
     * @param utils the utilities class
     * @return diagnostics for the given uris list.
     * @throws JavaModelException
     */
    public List<PublishDiagnosticsParams> diagnostics(JakartaJavaDiagnosticsParams params, IJDTUtils utils,
                                                      IProgressMonitor monitor) throws JavaModelException {
        System.out.println("diagnostics--------" + params.getJakartaVersion());
        List<String> uris = params.getUris();
        if (uris == null) {
            return Collections.emptyList();
        }
        DocumentFormat documentFormat = params.getDocumentFormat();
        List<PublishDiagnosticsParams> publishDiagnostics = new ArrayList<PublishDiagnosticsParams>();
        for (String uri : uris) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            PublishDiagnosticsParams publishDiagnostic = new PublishDiagnosticsParams(uri, diagnostics);
            publishDiagnostics.add(publishDiagnostic);
            collectDiagnostics(uri, utils, documentFormat, params.getSettings(), diagnostics, monitor, params.getJakartaVersion());
        }
        if (monitor.isCanceled()) {
            return Collections.emptyList();
        }
        return publishDiagnostics;
    }

    private void collectDiagnostics(String uri, IJDTUtils utils, DocumentFormat documentFormat,
                                    JakartaJavaDiagnosticsSettings settings, List<Diagnostic> diagnostics, IProgressMonitor monitor, int version) {
        ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
        if (typeRoot == null) {
            return;
        }

        // Collect all adapted diagnostics participant
        JavaDiagnosticsContext context = new JavaDiagnosticsContext(uri, typeRoot, utils, documentFormat, settings);
        context.setJakartaVersion(version);
        List<JavaDiagnosticsDefinition> definitions = JavaFeaturesRegistry.getInstance().getJavaDiagnosticsDefinitions().stream().filter(definition -> definition.isAdaptedForDiagnostics(context,
                                                                                                                                                                                          monitor)).collect(Collectors.toList());
        if (definitions.isEmpty()) {
            return;
        }

        // Begin, collect, end participants
        definitions.forEach(definition -> definition.beginDiagnostics(context, monitor));
        definitions.forEach(definition -> {
            List<Diagnostic> collectedDiagnostics = definition.collectDiagnostics(context, monitor);
            if (collectedDiagnostics != null && !collectedDiagnostics.isEmpty()) {
                diagnostics.addAll(collectedDiagnostics);
            }
        });
        definitions.forEach(definition -> definition.endDiagnostics(context, monitor));
    }

    /**
     * Searches through the AST to figure out the following:
     * <ul>
     * <li>If an annotation were to be placed at the completionOffset, what type of
     * node would it be annotating?</li>
     * <li>Is the completionOffset within the list of annotations before a
     * member?</li>
     * </ul>
     */
    private static class FindWhatsBeingAnnotatedASTVisitor extends ASTVisitor {

        private int completionOffset;
        private int closest = Integer.MAX_VALUE;
        private int annotatedNode = 0;
        private boolean visitedParentType;
        private boolean inAnnotations = false;

        public FindWhatsBeingAnnotatedASTVisitor(int completionOffset, boolean startingInParent) {
            this.completionOffset = completionOffset;
            this.visitedParentType = !startingInParent;
        }

        public FindWhatsBeingAnnotatedASTVisitor(int completionOffset) {
            this(completionOffset, true);
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            return visitNode(node);
        }

        @Override
        public boolean visit(FieldDeclaration node) {
            return visitNode(node);
        }

        @Override
        public boolean visit(EnumConstantDeclaration node) {
            return visitNode(node);
        }

        @Override
        public boolean visit(AnnotationTypeMemberDeclaration node) {
            return visitNode(node);
        }

        @Override
        public boolean visit(TypeDeclaration node) {
            return visitAbstractType(node);
        }

        @Override
        public boolean visit(EnumDeclaration node) {
            return visitAbstractType(node);
        }

        @Override
        public boolean visit(AnnotationTypeDeclaration node) {
            return visitAbstractType(node);
        }

        @Override
        public boolean visit(RecordDeclaration node) {
            return visitAbstractType(node);
        }

        private boolean visitAbstractType(AbstractTypeDeclaration node) {
            // we need to visit the children of the first type declaration,
            // since the visitor start visiting from the supplied node.
            if (!visitedParentType) {
                visitedParentType = true;
                return true;
            }
            return visitNode(node);
        }

        private boolean visitNode(BodyDeclaration node) {
            // ignore generated nodes
            if (isGenerated(node)) {
                return false;
            }
            // consider the start of the declaration to be after the annotations
            int start = node.modifiers().isEmpty() ? node.getStartPosition() : offsetOfFirstNonAnnotationModifier(node);
            if (start < closest && completionOffset <= start) {
                closest = node.getStartPosition();
                annotatedNode = node.getNodeType();
                inAnnotations = node.getStartPosition() < completionOffset && completionOffset <= start;
            }
            // We don't want to enter nested classes
            return false;
        }

        /**
         * Returns the type of the node that an annotation placed at the completion
         * offset would be annotating.
         *
         * @see org.eclipse.jdt.core.dom.ASTNode#getNodeType()
         * @return the type of the node that an annotation placed at the completion
         *         offset would be annotating
         */
        public int getAnnotatedNodeType() {
            return annotatedNode;
        }

        /**
         * Returns true if the completion offset is within the list of annotations
         * preceding a body declaration (field, method, class declaration) or false
         * otherwise.
         *
         * @return true if the completion offset is within the list of annotations
         *         preceding a body declaration (field, method, class declaration) or
         *         false otherwise
         */
        public boolean isInAnnotations() {
            return inAnnotations;
        }

    }

    private static int offsetOfFirstNonAnnotationModifier(BodyDeclaration node) {
        List modifiers = node.modifiers();
        for (int i = 0; i < modifiers.size(); i++) {
            ASTNode modifier = (ASTNode) modifiers.get(i);
            if (!ASTNodeUtils.isAnnotation(modifier)) {
                return modifier.getStartPosition();
            }
        }
        if (node instanceof MethodDeclaration method) {
            if (method.getReturnType2() != null) {
                return method.getReturnType2().getStartPosition();
            }
            // package protected constructor
            return method.getName().getStartPosition();
        } else if (node instanceof FieldDeclaration field) {
            return field.getType().getStartPosition();
        } else {
            var type = (AbstractTypeDeclaration) node;
            int nameOffset = type.getName().getStartPosition();
            int keywordLength = (switch (type.getNodeType()) {
                case ASTNode.TYPE_DECLARATION -> ((TypeDeclaration) type).isInterface() ? "interface" : "class";
                case ASTNode.ENUM_DECLARATION -> "enum";
                case ASTNode.ANNOTATION_TYPE_DECLARATION -> "@interface";
                case ASTNode.RECORD_DECLARATION -> "record";
                default -> "";
            }).length();

            // HACK: this assumes the code contains one space between the keyword and the
            // type name, which isn't always the case
            return nameOffset - (keywordLength + 1);
        }
    }

    /**
     * Returns true if the given node was generated by Project Lombok and false
     * otherwise.
     *
     * @param node the node to check if it's generated
     * @return true if the given node was generated by Project Lombok and false
     *         otherwise
     */
    private static boolean isGenerated(ASTNode node) {
        try {
            return ((Boolean) node.getClass().getField("$isGenerated").get(node)).booleanValue();
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            return false;
        }
    }

    /**
     * Returns the Java file information (ex : package name) from the given file URI
     * and null otherwise.
     *
     * @param params the file information parameters.
     * @param utils the utilities class
     * @param monitor the monitor
     * @return the Java file information (ex : package name) from the given file URI
     *         and null otherwise.
     */
    public JakartaJavaFileInfo fileInfo(JakartaJavaFileInfoParams params, IJDTUtils utils) {
        String uri = params.getUri();
        final ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        if (unit != null && unit.exists()) {
            JakartaJavaFileInfo fileInfo = new JakartaJavaFileInfo();
            String packageName = unit.getParent() != null ? unit.getParent().getElementName() : "";
            fileInfo.setPackageName(packageName);
            return fileInfo;
        }
        return null;
    }

}
