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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.AddMethodProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Base quick fix that inserts a required {@code notify} override into a class
 * that implements {@code ObserverMethod} without providing the method.
 *
 * <p>Two concrete subclasses cover the two overloads:
 * <ul>
 * <li>{@code notify(T event)} — uses the concrete type argument of {@code ObserverMethod<T>}</li>
 * <li>{@code notify(EventContext<T> eventContext)} — uses {@code EventContext<ConcreteType>}</li>
 * </ul>
 *
 * <p>The concrete type argument is resolved from the class's superinterface binding at
 * quick-fix time (e.g. {@code ObserverMethod<AuditEvent>} → {@code AuditEvent}).
 * If resolution fails, {@code Object} / raw {@code EventContext} is used as a fallback.
 */
@SuppressWarnings("restriction")
abstract class InsertNotifyMethodQuickFix implements IJavaCodeActionParticipant {

    private static final Logger LOGGER = Logger.getLogger(InsertNotifyMethodQuickFix.class.getName());

    /** Which of the two notify overloads this instance inserts. */
    private final NotifyVariant variant;

    /** Message-key used to look up the label in {@code messages.properties}. */
    private final String labelKey;

    /**
     * Constructs a quick fix for one of the two {@code notify} overloads.
     *
     * @param variant which overload to insert — {@link NotifyVariant#EVENT} or
     *            {@link NotifyVariant#EVENT_CONTEXT}
     * @param labelKey resource-bundle key used to look up the quick-fix label
     */
    protected InsertNotifyMethodQuickFix(NotifyVariant variant, String labelKey) {
        this.variant = variant;
        this.labelKey = labelKey;
    }

    /**
     * Returns a single unresolved code action whose title is derived from the
     * resolved type argument of {@code ObserverMethod<T>} on the enclosing class.
     *
     * @param context the code action context
     * @param diagnostic the diagnostic that triggered this quick fix
     * @param monitor progress monitor
     * @return a list containing the unresolved code action, or an empty list if
     *         the enclosing type binding cannot be determined
     * @throws CoreException if an error occurs during code action construction
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);
        List<CodeAction> codeActions = new ArrayList<>();

        if (parentType != null) {
            String label = getLabel(DiagnosticUtils.resolveObserverMethodTypeArgSimpleName(parentType));
            ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
            codeAction.setRelevance(0);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeAction.setDiagnostics(Arrays.asList(diagnostic));
            codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
            codeActions.add(codeAction);
        }

        return codeActions;
    }

    /**
     * Resolves the code action by constructing an {@link AddMethodProposal} that
     * inserts the appropriate {@code notify} override and converts it to a
     * workspace edit.
     *
     * @param context the resolve context carrying the unresolved code action and AST root
     * @return the resolved code action with its {@code edit} populated, or the
     *         original unresolved action if the enclosing type cannot be determined
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);

        if (parentType == null) {
            return toResolve;
        }

        // Resolve the concrete type argument T from ObserverMethod<T>.
        String typeArgFQName = DiagnosticUtils.resolveObserverMethodTypeArgFQName(parentType);

        AddMethodProposal.MethodParam param;
        if (variant == NotifyVariant.EVENT) {
            // notify(ConcreteType event)
            param = new AddMethodProposal.MethodParam(typeArgFQName, "event");
        } else {
            // notify(EventContext<ConcreteType> eventContext)
            param = new AddMethodProposal.MethodParam(Constants.EVENT_CONTEXT_FQ_NAME, "eventContext", typeArgFQName);
        }

        String label = getLabel(DiagnosticUtils.resolveObserverMethodTypeArgSimpleName(parentType));
        ChangeCorrectionProposal proposal = new AddMethodProposal(label, context.getCompilationUnit(), context.getASTRoot(), parentType, 0, "notify", "void", "public", Collections.singletonList("java.lang.Override"), Collections.singletonList(param));

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action to insert notify method override", e);
        }

        return toResolve;
    }

    /**
     * Returns the code-action label, parameterised with the simple type-arg name.
     *
     * @param typeArgSimpleName the simple name of the resolved type argument
     * @return the localised label string
     */
    private String getLabel(String typeArgSimpleName) {
        return Messages.getMessage(labelKey, typeArgSimpleName);
    }

    /**
     * Returns the {@link ICodeActionId} that uniquely identifies this quick fix.
     *
     * @return the code action ID for this participant
     */
    protected abstract ICodeActionId getCodeActionId();

    // -------------------------------------------------------------------------
    // Concrete subclasses
    // -------------------------------------------------------------------------

    /**
     * Quick fix that inserts {@code @Override public void notify(T event) {}}
     * using the concrete type argument resolved from {@code ObserverMethod<T>}.
     */
    public static class NotifyEvent extends InsertNotifyMethodQuickFix {

        /**
         * Creates the {@code notify(T event)} quick fix.
         */
        public NotifyEvent() {
            super(NotifyVariant.EVENT, "InsertNotifyEventMethod");
        }

        /**
         * {@inheritDoc}
         *
         * @return the fully-qualified class name of {@link NotifyEvent}
         */
        @Override
        public String getParticipantId() {
            return NotifyEvent.class.getName();
        }

        /**
         * {@inheritDoc}
         *
         * @return {@link JakartaCodeActionId#CDIInsertNotifyEventMethod}
         */
        @Override
        protected ICodeActionId getCodeActionId() {
            return JakartaCodeActionId.CDIInsertNotifyEventMethod;
        }
    }

    /**
     * Quick fix that inserts {@code @Override public void notify(EventContext<T> eventContext) {}}
     * using the concrete type argument resolved from {@code ObserverMethod<T>}.
     */
    public static class NotifyEventContext extends InsertNotifyMethodQuickFix {

        /**
         * Creates the {@code notify(EventContext&lt;T&gt; eventContext)} quick fix.
         */
        public NotifyEventContext() {
            super(NotifyVariant.EVENT_CONTEXT, "InsertNotifyEventContextMethod");
        }

        /**
         * {@inheritDoc}
         *
         * @return the fully-qualified class name of {@link NotifyEventContext}
         */
        @Override
        public String getParticipantId() {
            return NotifyEventContext.class.getName();
        }

        /**
         * {@inheritDoc}
         *
         * @return {@link JakartaCodeActionId#CDIInsertNotifyEventContextMethod}
         */
        @Override
        protected ICodeActionId getCodeActionId() {
            return JakartaCodeActionId.CDIInsertNotifyEventContextMethod;
        }
    }
}
