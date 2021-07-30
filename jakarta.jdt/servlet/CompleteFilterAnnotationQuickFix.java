/*******************************************************************************
* Copyright (c) 2020, 2021 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/

package org.jakarta.jdt.servlet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.jakarta.codeAction.JavaCodeActionContext;
import org.jakarta.codeAction.proposal.ChangeCorrectionProposal;
import org.jakarta.codeAction.proposal.ModifyAnnotationProposal;
import org.jakarta.codeAction.proposal.quickfix.InsertAnnotationMissingQuickFix;

/**
 * QuickFix for fixing {@link ServletConstants#DIAGNOSTIC_CODE_FILTER_MISSING_ATTRIBUTE} error
 * and {@link ServletConstants#DIAGNOSTIC_CODE_FILTER_MISSING_ATTRIBUTE} error
 * by providing several code actions:
 *
 *
 * {@link ServletConstants#DIAGNOSTIC_CODE_FILTER_MISSING_ATTRIBUTE}
 * <ul>
 * <li> Add the `value` attribute to the `@WebFilter` annotation
 * <li> Add the `urlPatterns` attribute to the `@WebFilter` annotation
 * <li> Add the `servletNames` attribute to the `@WebFilter` annotation
 * </ul>
 *
 * {@link ServletConstants#DIAGNOSTIC_CODE_FILTER_MISSING_ATTRIBUTE}
 * <ul>
 * <li> Remove the `value` attribute from the `@WebFilter` annotation
 * <li> Remove the `urlPatterns` attribute from the `@WebFilter` annotation
 * </ul>
 *
 * @author Kathryn Kodama
 *
 */
public class CompleteFilterAnnotationQuickFix extends InsertAnnotationMissingQuickFix {

    public CompleteFilterAnnotationQuickFix() {
        super("jakarta.servlet.annotation.WebFilter");
    }

    @Override
    protected void insertAnnotations(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType,
            List<CodeAction> codeActions) throws CoreException {
        String[] annotations = getAnnotations();
        for (String annotation : annotations) {
            insertAndReplaceAnnotation(diagnostic, context, parentType, codeActions, annotation);
        }
    }

    private static void insertAndReplaceAnnotation(Diagnostic diagnostic, JavaCodeActionContext context,
            IBinding parentType, List<CodeAction> codeActions, String annotation) throws CoreException {

        // Insert the annotation and the proper import by using JDT Core Manipulation
        // API

    	
    	// if missing an attribute, do value insertion
    	if (diagnostic.getCode().getLeft().equals(ServletConstants.DIAGNOSTIC_CODE_FILTER_MISSING_ATTRIBUTE)) {
    		ArrayList<String> attributes = new ArrayList<>();
    		attributes.add("value"); attributes.add("urlPatterns");attributes.add("servletNames");
    		// Code Action 1: add value attribute to the WebServlet annotation
    		// Code Action 2: add urlPatterns attribute to the WebServlet annotation
    		for (int i = 0; i < attributes.size(); i++) {
    			String attribute = attributes.get(i);
    			
    			ArrayList<String> attributesToAdd = new ArrayList<>();
    	        attributesToAdd.add(attribute);
                String name = getLabel(annotation, attribute, "Add");
    	        ChangeCorrectionProposal proposal = new ModifyAnnotationProposal(name, context.getCompilationUnit(),
    	                context.getASTRoot(), parentType, 0, annotation, attributesToAdd);
    	        // Convert the proposal to LSP4J CodeAction
    	        CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
    	        codeAction.setTitle(name);
    	        if (codeAction != null) {
    	            codeActions.add(codeAction);
    	        }
    		}
    	}
    	// if duplicate attributes exist in annotations, remove attributes from annotation
    	if (diagnostic.getCode().getLeft().equals(ServletConstants.DIAGNOSTIC_CODE_FILTER_DUPLICATE_ATTRIBUTES)) {
    		ArrayList<String> attributes = new ArrayList<>();
    		attributes.add("value"); attributes.add("urlPatterns");
    		// Code Action 1: remove value attribute from the WebServlet annotation
    		// Code Action 2: remove urlPatterns attribute from the WebServlet annotation
    		for (int i = 0; i < attributes.size(); i++) {
    			String attribute = attributes.get(i);
    			
    			ArrayList<String> attributesToRemove = new ArrayList<>();
    	        attributesToRemove.add(attribute);
                String name = getLabel(annotation, attribute, "Remove");
    	        ChangeCorrectionProposal proposal = new ModifyAnnotationProposal(name, context.getCompilationUnit(),
    	                context.getASTRoot(), parentType, 0, annotation, new ArrayList<String>(), attributesToRemove);
    	        // Convert the proposal to LSP4J CodeAction
    	        CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
    	        codeAction.setTitle(name);
    	        if (codeAction != null) {
    	            codeActions.add(codeAction);
    	        }
    		}
    	}
    }

    private static String getLabel(String annotation, String attribute, String labelType) {
    	StringBuilder name = new StringBuilder("Add the `" + attribute + "` attribute to ");
        if (labelType.equals("Remove")) {
            name = new StringBuilder("Remove the `" + attribute + "` attribute from ");
    	}
        String annotationName = annotation.substring(annotation.lastIndexOf('.') + 1, annotation.length());
        name.append("@");
        name.append(annotationName);
        return name.toString();
    }
}