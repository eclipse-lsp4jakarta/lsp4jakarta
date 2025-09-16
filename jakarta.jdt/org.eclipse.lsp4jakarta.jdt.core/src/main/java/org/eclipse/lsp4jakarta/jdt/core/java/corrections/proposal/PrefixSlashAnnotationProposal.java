package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeActionKind;

public class PrefixSlashAnnotationProposal extends ASTRewriteCorrectionProposal{

	private final CompilationUnit invocationNode;
	private final IBinding binding;
	
	public PrefixSlashAnnotationProposal(String label,  ICompilationUnit cu, CompilationUnit invocationNode,
			int relevance, IBinding binding) {
		super(label, CodeActionKind.QuickFix, cu, null, relevance);
		
		this.invocationNode=invocationNode;
		this.binding=binding;
	}
	
	
	@SuppressWarnings("restriction")
    @Override
    protected ASTRewrite getRewrite() {
        ASTNode declNode = null;
        ASTNode boundNode = invocationNode.findDeclaringNode(binding);
        CompilationUnit newRoot = invocationNode;

        if (boundNode != null) {
            declNode = boundNode;
        } else {
            newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
            declNode = newRoot.findDeclaringNode(binding.getKey());
        }

        if (declNode.getNodeType() == ASTNode.TYPE_DECLARATION) {
            AST ast = null;
            ASTRewrite rewrite = null;
            TypeDeclaration typeDecl = (TypeDeclaration) declNode;
            
            for (IExtendedModifier modifier : (List<IExtendedModifier>) typeDecl.modifiers()) {
            	if (modifier.isAnnotation()) {
            		Annotation annotation = (Annotation) modifier;
                    String name = annotation.getTypeName().getFullyQualifiedName();
                    if ("ServerEndpoint".equals(name)) {
                    	ast = annotation.getAST();
                        rewrite = ASTRewrite.create(ast);
                        
                        if (annotation.isSingleMemberAnnotation()) {
                            // Case: @ServerEndpoint("path")
                            SingleMemberAnnotation single = (SingleMemberAnnotation) annotation;
                            Expression valueExpr = single.getValue();

                            if (valueExpr instanceof StringLiteral oldLiteral) {
                                String oldValue = oldLiteral.getLiteralValue();
                                if (!oldValue.startsWith("/")) {
                                    StringLiteral newLiteral = ast.newStringLiteral();
                                    newLiteral.setLiteralValue("/" + oldValue);
                                    rewrite.set(single, SingleMemberAnnotation.VALUE_PROPERTY, newLiteral, null);
                                }
                            }
                        }
                    }
            	}
            }
			/*
			 * if (declNode instanceof SingleMemberAnnotation) {
			 * SingleMemberAnnotation single = (SingleMemberAnnotation) declNode;
			 * Expression valueExpr = single.getValue();
			 * 
			 * if (valueExpr instanceof StringLiteral oldLiteral) {
			 * String oldValue = oldLiteral.getLiteralValue();
			 * if (!oldValue.startsWith("/")) {
			 * StringLiteral newLiteral = ast.newStringLiteral();
			 * newLiteral.setLiteralValue("/" + oldValue);
			 * rewrite.set(single, SingleMemberAnnotation.VALUE_PROPERTY, newLiteral, null);
			 * }
			 * }
			 * }
			 */
            
            return rewrite;
        }
        return null;
    }
	

}
