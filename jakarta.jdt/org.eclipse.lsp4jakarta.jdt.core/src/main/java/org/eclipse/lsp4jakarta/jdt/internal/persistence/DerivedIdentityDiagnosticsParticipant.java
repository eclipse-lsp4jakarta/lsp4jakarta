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
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.internal.persistence.CompositeKeyResolver.KeyStrategy;
import org.eclipse.lsp4jakarta.jdt.internal.persistence.CompositeKeyResolver.ParentPk;

/**
 * Diagnostic participant for Jakarta Persistence 3.0 derived-identity rules
 * (specification section 2.4.1).
 *
 * <h3>Checks performed</h3>
 * <ol>
 * <li><b>Phase 1 – {@code @MapsId} structural checks</b> (single-file):
 * <ul>
 * <li>{@code @MapsId} must be placed on a field or property also annotated
 * with {@code @OneToOne} or {@code @ManyToOne}.</li>
 * <li>{@code @MapsId} that specifies a {@code value} names an attribute within
 * a composite key, so it can only be used on an entity that declares a
 * composite primary key via {@code @EmbeddedId} or {@code @IdClass}. A
 * {@code @MapsId} without a value is valid with a simple {@code @Id}
 * (spec §2.4.1.1, Example 4b).</li>
 * </ul></li>
 * <li><b>Phase 2 – {@code @MapsId} cross-file checks</b>:
 * <ul>
 * <li>When {@code @MapsId} specifies a value (e.g. {@code @MapsId("empPK")}),
 * that value must name an attribute in the {@code @EmbeddedId} class.</li>
 * <li>The type of that attribute must be compatible with the primary key type
 * of the referenced parent entity, resolved via
 * {@code IJavaProject#findType(String)} — no project-wide scan needed.</li>
 * </ul></li>
 * </ol>
 *
 * <p><b>Note:</b> {@code @IdClass} member alignment is covered by a separate
 * diagnostic participant and is intentionally out of scope here.</p>
 *
 * <p><b>Specification:</b>
 * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a149">
 * §2.4.1 Primary Keys Corresponding to Derived Identities</a>.</p>
 */
public class DerivedIdentityDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /** {@inheritDoc} */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context,
                                               IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(context.getUri());
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        for (IType type : unit.getAllTypes()) {
            if (!DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), Constants.ENTITY)) {
                continue;
            }
            validateEntityType(type, unit, diagnostics, context);
        }

        return diagnostics;
    }

    /**
     * Validates all {@code @MapsId}-annotated fields and property getters declared
     * directly on {@code type}.
     *
     * <p>Static fields are skipped; instance fields and no-arg property getters are
     * both checked.</p>
     *
     * @param type the {@code @Entity}-annotated type to inspect
     * @param unit the compilation unit that contains {@code type}
     * @param diagnostics the list to which any violations are appended
     * @param context the diagnostic context for range resolution and diagnostic creation
     * @throws JavaModelException if JDT cannot inspect the type's members or annotations
     */
    private void validateEntityType(IType type, ICompilationUnit unit,
                                    List<Diagnostic> diagnostics,
                                    JavaDiagnosticsContext context) throws JavaModelException {

        // Determine whether the entity declares a composite key and resolve the
        // @EmbeddedId class (if any). Only the direct members of this type are
        // checked; hierarchy walking is intentionally omitted for simplicity.
        KeyStrategy keyStrategy = CompositeKeyResolver.resolveKeyStrategy(type, unit);

        // Validate every @MapsId-annotated field and property of this entity.
        for (IField field : type.getFields()) {
            if (Flags.isStatic(field.getFlags())) {
                continue;
            }
            if (DiagnosticUtils.isMatchedAnnotation(unit, field.getAnnotations(), Constants.MAPSID)) {
                validateMapsIdMember(field, unit, keyStrategy, diagnostics, context);
            }
        }
        for (IMethod method : type.getMethods()) {
            if (DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(), Constants.MAPSID)) {
                validateMapsIdMember(method, unit, keyStrategy, diagnostics, context);
            }
        }
    }

    /**
     * Coordinator: validates structural placement first, then cross-file attribute
     * binding. Attribute binding is skipped when structural validation reports a
     * fatal violation.
     */
    private void validateMapsIdMember(IMember member, ICompilationUnit unit, KeyStrategy keyStrategy,
                                      List<Diagnostic> diagnostics,
                                      JavaDiagnosticsContext context) throws JavaModelException {
        IAnnotation[] memberAnnotations = DiagnosticUtils.getAnnotations(member);
        // Guaranteed non-null: caller confirmed @MapsId is present before dispatching here.
        IAnnotation mapsIdAnn = DiagnosticUtils.getMatchedAnnotation(unit, memberAnnotations, Constants.MAPSID);
        String mapsIdValue = DiagnosticUtils.getAnnotationMemberValue(mapsIdAnn, Constants.VALUE, String.class);
        boolean hasValue = mapsIdValue != null && !mapsIdValue.isEmpty();

        boolean fatal = validateMapsIdStructure(member, unit, memberAnnotations, keyStrategy, hasValue, context, diagnostics);
        if (fatal) {
            return;
        }
        if (hasValue) {
            validateMapsIdAttributeBinding(member, keyStrategy, mapsIdValue, context, diagnostics);
        }
        // Value-less @MapsId: skipped for @IdClass entities, since @IdClass member
        // alignment is validated by a separate participant, and when the relationship
        // itself is the @Id (its type is the parent entity, not a key type).
        else if (!keyStrategy.hasIdClass
                 && !DiagnosticUtils.isMatchedAnnotation(unit, memberAnnotations, Constants.ID)) {
            validateValuelessMapsId(member, context, diagnostics);
        }
    }

    /**
     * Validates a {@code @MapsId} with no {@code value} (spec §2.4.1.1, Examples 4b, 5b, 6b):
     * the relationship maps the dependent's entire primary key, so that key (the
     * {@code @EmbeddedId} type or the simple {@code @Id} type) must have the same type as
     * the primary key of the parent entity. Skipped when the relationship itself is the
     * {@code @Id}, or when the entity uses {@code @IdClass}.
     */
    private void validateValuelessMapsId(IMember member,
                                         JavaDiagnosticsContext context,
                                         List<Diagnostic> diagnostics) throws JavaModelException {
        ParentPk parentPk = CompositeKeyResolver.resolveParentPk(member);
        if (parentPk == null) {
            return;
        }
        String ownPkType = CompositeKeyResolver.resolvePkType(member.getDeclaringType());
        if (ownPkType != null && !CompositeKeyResolver.typesCompatible(ownPkType, parentPk.pkFqType)) {
            addDiagnostic(member,
                          Messages.getMessage("MapsIdPrimaryKeyTypeMismatch",
                                              ownPkType,
                                              parentPk.entity.getElementName(),
                                              parentPk.pkFqType),
                          ErrorCode.MapsIdPrimaryKeyTypeMismatch, context, diagnostics);
        }
    }

    /**
     * Validates the structural constraints of a {@code @MapsId}-annotated member
     * (Rules 1–2, spec §2.4.1): the member must be a to-one
     * relationship, and a {@code @MapsId} with a value requires a composite key.
     *
     * @return {@code true} if a fatal violation was found and attribute-binding
     *         checks must not run, {@code false} otherwise.
     */
    private boolean validateMapsIdStructure(IMember member, ICompilationUnit unit,
                                            IAnnotation[] memberAnnotations, KeyStrategy keyStrategy,
                                            boolean mapsIdHasValue,
                                            JavaDiagnosticsContext context,
                                            List<Diagnostic> diagnostics) throws JavaModelException {
        // Rule 1: @MapsId must be on @OneToOne or @ManyToOne (spec §2.4.1).
        boolean hasToOne = DiagnosticUtils.isMatchedAnnotation(unit, memberAnnotations, Constants.ONE_TO_ONE)
                           || DiagnosticUtils.isMatchedAnnotation(unit, memberAnnotations, Constants.MANY_TO_ONE);
        if (!hasToOne) {
            addDiagnostic(member, Messages.getMessage("MapsIdOnInvalidRelationship"),
                          ErrorCode.MapsIdOnInvalidRelationship, context, diagnostics);
            return true; // @MapsId is misplaced — further checks are meaningless
        }

        // Rule 2: a @MapsId value names an attribute within a composite key, so the entity
        // must declare one (spec §2.4.1.1). A value-less @MapsId is valid with a simple @Id.
        if (mapsIdHasValue && !keyStrategy.isComposite()) {
            addDiagnostic(member, Messages.getMessage("MapsIdRequiresCompositeKey"),
                          ErrorCode.MapsIdRequiresCompositeKey, context, diagnostics);
            return true;
        }

        return false;
    }

    /**
     * Validates that the value attribute of {@code @MapsId} names an existing
     * attribute in the {@code @EmbeddedId} class and that its type is compatible
     * with the parent entity's primary key type (Rules 4–5, spec §2.4.1).
     * Only runs when the entity uses {@code @EmbeddedId} and {@code @MapsId}
     * names a specific attribute.
     */
    private void validateMapsIdAttributeBinding(IMember member, KeyStrategy keyStrategy,
                                                String mapsIdValue,
                                                JavaDiagnosticsContext context,
                                                List<Diagnostic> diagnostics) throws JavaModelException {
        // Only applicable when the entity uses @EmbeddedId.
        if (keyStrategy.embeddedIdType == null) {
            return; // @IdClass path — attribute-name checks do not apply here
        }

        // Resolve the parent entity chain first — it is the expensive cross-file lookup.
        // Attribute resolution only runs if the parent chain is fully resolvable.
        ParentPk parentPk = CompositeKeyResolver.resolveParentPk(member);
        if (parentPk == null) {
            return;
        }

        // Rule 4: the named value must be an attribute in the @EmbeddedId class.
        IType embeddedIdClass = keyStrategy.embeddedIdType;
        String embeddedAttrFqType = CompositeKeyResolver.resolveAttributeType(embeddedIdClass, mapsIdValue);
        if (embeddedAttrFqType == null) {
            addDiagnostic(member,
                          Messages.getMessage("MapsIdValueNotInEmbeddedIdClass",
                                              mapsIdValue, embeddedIdClass.getElementName()),
                          ErrorCode.MapsIdValueNotInEmbeddedIdClass, context, diagnostics);
            return;
        }

        // Rule 5: the attribute's type must be compatible with the parent entity's PK type.
        if (!CompositeKeyResolver.typesCompatible(embeddedAttrFqType, parentPk.pkFqType)) {
            addDiagnostic(member,
                          Messages.getMessage("MapsIdValueTypeMismatch",
                                              mapsIdValue,
                                              embeddedIdClass.getElementName(),
                                              embeddedAttrFqType,
                                              parentPk.entity.getElementName(),
                                              parentPk.pkFqType),
                          ErrorCode.MapsIdValueTypeMismatch, context, diagnostics);
        }
    }

    /**
     * Creates an {@link ErrorCode#Error}-severity diagnostic for {@code member} and adds it to
     * {@code diagnostics}. Centralises the repeated boilerplate of resolving the name range,
     * supplying the fixed source and severity, and calling
     * {@link JavaDiagnosticsContext#createDiagnostic}.
     */
    private void addDiagnostic(IMember member, String message, ErrorCode errorCode,
                               JavaDiagnosticsContext context, List<Diagnostic> diagnostics) throws JavaModelException {
        Range range = PositionUtils.toNameRange(member, context.getUtils());
        diagnostics.add(context.createDiagnostic(context.getUri(), message, range,
                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                 errorCode, DiagnosticSeverity.Error));
    }
}
