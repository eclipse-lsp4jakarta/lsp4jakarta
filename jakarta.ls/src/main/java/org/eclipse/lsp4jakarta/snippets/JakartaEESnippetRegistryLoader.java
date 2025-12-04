/*******************************************************************************
* Copyright (c) 2020, 2025 IBM Corporation, Ankush Sharma and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Ankush Sharma - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.snippets;

import java.util.logging.Logger;

import org.eclipse.lsp4jakarta.ls.commons.snippets.ISnippetRegistryLoader;
import org.eclipse.lsp4jakarta.ls.commons.snippets.SnippetRegistry;

/**
 * Loads in JakartaEE Specific Snippets
 *
 * @author Ankush Sharma
 */
public class JakartaEESnippetRegistryLoader implements ISnippetRegistryLoader {
    private static final Logger LOGGER = Logger.getLogger(JakartaEESnippetRegistryLoader.class.getName());

    @Override
    public void load(SnippetRegistry registry) throws Exception {
        LOGGER.info("Loading snippets into registry...");
        registry.registerSnippets(
                                  JakartaEESnippetRegistryLoader.class.getClassLoader().getResourceAsStream("restfulWs.json"),
                                  SnippetContextForJava.TYPE_ADAPTER);
        registry.registerSnippets(
                                  JakartaEESnippetRegistryLoader.class.getClassLoader().getResourceAsStream("servlet.json"),
                                  SnippetContextForJava.TYPE_ADAPTER);
        registry.registerSnippets(
                                  JakartaEESnippetRegistryLoader.class.getClassLoader().getResourceAsStream("persistence.json"),
                                  SnippetContextForJava.TYPE_ADAPTER);
        registry.registerSnippets(
                                  JakartaEESnippetRegistryLoader.class.getClassLoader().getResourceAsStream("bean-validation.json"),
                                  SnippetContextForJava.TYPE_ADAPTER);
        registry.registerSnippets(
                                  JakartaEESnippetRegistryLoader.class.getClassLoader().getResourceAsStream("transactions.json"),
                                  SnippetContextForJava.TYPE_ADAPTER);
        registry.registerSnippets(
                                  JakartaEESnippetRegistryLoader.class.getClassLoader().getResourceAsStream("websocket.json"),
                                  SnippetContextForJava.TYPE_ADAPTER);
        registry.registerSnippets(
                                  JakartaEESnippetRegistryLoader.class.getClassLoader().getResourceAsStream("dependencyinjection.json"),
                                  SnippetContextForJava.TYPE_ADAPTER);
    }

    @Override
    public String getLanguageId() {
        return LanguageId.java.name();
    }

}
