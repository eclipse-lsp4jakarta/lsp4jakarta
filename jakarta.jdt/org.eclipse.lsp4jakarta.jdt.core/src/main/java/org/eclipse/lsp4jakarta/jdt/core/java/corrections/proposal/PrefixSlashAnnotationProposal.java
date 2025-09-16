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
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeActionKind;

public class PrefixSlashAnnotationProposal extends ASTRewriteCorrectionProposal {

	private final CompilationUnit invocationNode;
	private final IBinding binding;
	private final ASTNode annotationNode;

	public PrefixSlashAnnotationProposal(String label, ICompilationUnit cu, CompilationUnit invocationNode,
			int relevance, IBinding binding, ASTNode annotationNode) {
		super(label, CodeActionKind.QuickFix, cu, null, relevance);

		this.invocationNode = invocationNode;
		this.binding = binding;
		this.annotationNode = annotationNode;
	}

	@Override
	protected ASTRewrite getRewrite() {
		AST ast = annotationNode.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		if (annotationNode instanceof SingleMemberAnnotation single) {

			String name = single.getTypeName().getFullyQualifiedName();
			System.out.println(name);

			Expression valueExpr = single.getValue();

			if (valueExpr instanceof StringLiteral oldLiteral) {
				String oldValue = oldLiteral.getLiteralValue();
				if (!oldValue.startsWith("/")) {
					StringLiteral newLiteral = ast.newStringLiteral();
					newLiteral.setLiteralValue("/" + oldValue);
					rewrite.set(single, SingleMemberAnnotation.VALUE_PROPERTY, newLiteral, null);
				}
			}

		} else if (annotationNode instanceof NormalAnnotation normal) {

			for (Object obj : normal.values()) {
				MemberValuePair pair = (MemberValuePair) obj;

				if ("value".equals(pair.getName().getIdentifier())) {
					Expression valueExpr = pair.getValue();
					if (valueExpr instanceof StringLiteral oldLiteral) {
						String oldValue = oldLiteral.getLiteralValue();
						if (!oldValue.startsWith("/")) {
							StringLiteral newLiteral = ast.newStringLiteral();
							newLiteral.setLiteralValue("/" + oldValue);
							rewrite.set(pair, MemberValuePair.VALUE_PROPERTY, newLiteral, null);
						}
					}
				}
			}

		}
		return rewrite;

	}

}
