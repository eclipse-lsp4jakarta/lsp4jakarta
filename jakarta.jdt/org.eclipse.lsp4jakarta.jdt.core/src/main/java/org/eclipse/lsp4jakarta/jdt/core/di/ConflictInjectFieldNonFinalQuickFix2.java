/*******************************************************************************
* Copyright (c) 2021 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Himanshu Chotwani - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.di;

import org.eclipse.lsp4jakarta.jdt.codeAction.proposal.quickfix.RemoveModifierConflictQuickFix;


/**
 * 
 * Quick fix for removing final when it is used for @Inject field
 * 
 * @author Himanshu Chotwani
 *
 */
public class ConflictInjectFieldNonFinalQuickFix2 extends RemoveModifierConflictQuickFix { 

    public ConflictInjectFieldNonFinalQuickFix2() {
        super(false, "final");
    }
}