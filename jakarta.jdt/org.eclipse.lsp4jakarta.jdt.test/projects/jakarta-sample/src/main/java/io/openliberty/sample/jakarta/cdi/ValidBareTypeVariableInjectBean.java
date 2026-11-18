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
*     IBM Corporation - initial implementation
*******************************************************************************/
package io.openliberty.sample.jakarta.cdi;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class ValidBareTypeVariableInjectBean {

    @Inject
    private String stringField;

    @Inject
    private List<String> listField;

    @Inject
    public void setString(String value) {
    }

    @Inject
    public void setMultiple(String first, int second, List<String> third) {
    }
}
