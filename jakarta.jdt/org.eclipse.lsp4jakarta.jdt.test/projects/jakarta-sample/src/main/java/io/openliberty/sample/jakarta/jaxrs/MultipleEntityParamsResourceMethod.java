/*******************************************************************************
* Copyright (c) 2021 IBM Corporation.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Bera Sogut
*******************************************************************************/

package io.openliberty.sample.jakarta.jaxrs;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;

public class MultipleEntityParamsResourceMethod {

	@DELETE
	public void resourceMethodWithTwoEntityParams(String entityParam1, @FormParam(value = "") String nonEntityParam, int entityParam2) {
        
    }
	
	
	@DELETE
	public void resourceMethodWithTwoBeanParams(@BeanParam RequestBean requestBean, @BeanParam SessionBean sessionBean) {
        
    }
	
	
	@DELETE
	public void resourceMethodWithBeanParamsAndEnityParams(String entityString, int entityInt, @BeanParam RequestBean requestBean, @BeanParam SessionBean sessionBean) {
        
    }
}
