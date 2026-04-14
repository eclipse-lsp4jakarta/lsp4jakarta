package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.lsp4j.CodeActionKind;

/**
 * Code action proposal for removing all attributes (methods and fields) from an annotation type.
 */
public class RemoveAnnotationAttributesProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final AnnotationTypeDeclaration annotationTypeDeclaration;
    private final List<BodyDeclaration> attributesToRemove;

    /**
     * Constructor for RemoveAnnotationAttributesProposal.
     *
     * @param label The label for this proposal
     * @param targetCU The compilation unit
     * @param invocationNode The compilation unit AST node
     * @param annotationTypeDeclaration The annotation type declaration to modify
     * @param relevance The relevance of this proposal
     * @param attributesToRemove The list of attributes (methods/fields) to remove
     */
    public RemoveAnnotationAttributesProposal(String label, ICompilationUnit targetCU,
                                              CompilationUnit invocationNode,
                                              AnnotationTypeDeclaration annotationTypeDeclaration,
                                              int relevance,
                                              List<BodyDeclaration> attributesToRemove) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.annotationTypeDeclaration = annotationTypeDeclaration;
        this.attributesToRemove = attributesToRemove;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        AST ast = annotationTypeDeclaration.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        // Get the list rewriter for the annotation type's body declarations
        ListRewrite listRewrite = rewrite.getListRewrite(annotationTypeDeclaration, AnnotationTypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        // Remove all attributes
        for (BodyDeclaration attribute : attributesToRemove) {
            listRewrite.remove(attribute, null);
        }

        return rewrite;
    }
}
