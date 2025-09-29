/*******************************************************************************
* Copyright (c) 2025 IBM Corporation and others.
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
package io.openliberty.sample.jakarta.di;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.Service;

@ApplicationScoped
public class InnerClassInjection {

	@Inject
	private InnerBean bean;

	public InnerBean getBean() {
		return bean;
	}

	@Inject
	public void setBean(io.openliberty.sample.jakarta.di.InnerClassInjection.InnerBean bean) {
		this.bean = bean;
	}

	public class InnerBean {

		@Inject
		private Service service;
	}
}