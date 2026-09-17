/*******************************************************************************
* Copyright (c) 2020, 2023 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.ls.api;

import org.eclipse.lsp4j.services.LanguageClient;

/**
 * API of the client consuming the Language Server for Jakarta EE. Used to send
 * messages back to the client to ask for information about the Java project.
 */
public interface JakartaLanguageClientAPI extends LanguageClient, JakartaJavaCompletionProvider, JakartaJavaProjectLabelsProvider, JakartaJavaFileInfoProvider, JakartaJavaDiagnosticsProvider, JakartaJavaCodeActionProvider, JakartaJavaCodeActionResolveProvider, JakartaVersionSelectorProvider {}
