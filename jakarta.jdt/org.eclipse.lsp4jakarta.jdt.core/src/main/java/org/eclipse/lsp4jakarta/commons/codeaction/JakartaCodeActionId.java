/*******************************************************************************
* Copyright (c) 2023, 2025 Red Hat Inc. and others.
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
package org.eclipse.lsp4jakarta.commons.codeaction;

/**
 * LSP4Jakarta code action id.
 *
 * Based on: https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/commons/codeaction/MicroProfileCodeActionId.java
 */
public enum JakartaCodeActionId implements ICodeActionId {
    // JAXRS
    jaxrsInsertPublicCtrtToClass,
    MakeConstructorPublic,
    MakeMethodPublic,
    RemoveAllEntityParametersExcept,
    // Annotations
    ChangeReturnTypeToVoid,
    InsertResourceAnnotationTypeAttribute,
    InsertResourceAnnotationNameAttribute,
    InsertDefaultResourceAnnotationToResourcesAnnotation,
    RemoveAllParameters,
    RemoveAnnotationPreDestroy,
    RemoveAnnotationPostConstruct,
    AnnotationRemoveStaticModifier,
    // Bean validation
    RemoveConstraintAnnotation,
    BBRemoveStaticModifier,
    // Dependency injection
    DIRemoveInjectAnnotation,
    DIRemoveFinalModifier,
    DIRemoveAbstractModifier,
    DIRemoveStaticModifier,
    // JSON-B
    JSONBRemoveJsonbCreatorAnnotation,
    JSONBRemoveJsonbTransientAnnotation,
    JSONBRemoveAllButJsonbTransientAnnotation,
    // Persistence
    PersistenceRemoveFinalModifier,
    PersistenceRemoveMapKeyAnnotation,
    PersistenceInsertAttributesToMKJCAnnotation,
    PersistenceInsertPublicCtrtToClass,
    PersistenceInsertProtectedCtrtToClass,
    // WebSockets
    WBInsertPathParamAnnotationWithValueAttrib,
    // Servlet
    ServletCompleteWebFilterAnnotation,
    ServletCompleteServletAnnotation,
    ServletFilterImplementation,
    ServletExtendClass,
    ServletListenerImplementation,
    // CDI
    CDIRemoveProducesAndInjectAnnotations,
    CDIInsertInjectAnnotation,
    CDIInsertProtectedCtrtToClass,
    CDIInsertPublicCtrtToClass,
    CDIRemoveInvalidInjectAnnotations,
    CDIRemoveInvalidProducesAnnotations,
    //Added as part of fix that adds two quick fixes which are mutually exclusive issue #540
    CDIRemoveInvalidDisposerAnnotations,
    CDIRemoveInvalidDisposerConflictedAnnotations,
    CDIRemoveProducesAnnotation,
    CDIRemoveInjectAnnotation,
    CDIRemoveScopeDeclarationAnnotationsButOne,
    CDIReplaceScopeAnnotations;

    @Override
    public String getId() {
        return name();
    }
}