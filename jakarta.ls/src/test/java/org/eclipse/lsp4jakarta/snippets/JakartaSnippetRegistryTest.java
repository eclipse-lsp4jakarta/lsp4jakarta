/*******************************************************************************
* Copyright (c) 2023, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.snippets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.lsp4jakarta.commons.JavaCursorContextKind;
import org.eclipse.lsp4jakarta.commons.JavaCursorContextResult;
import org.eclipse.lsp4jakarta.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4jakarta.ls.commons.snippets.ISnippetContext;
import org.eclipse.lsp4jakarta.ls.commons.snippets.Snippet;
import org.eclipse.lsp4jakarta.ls.java.JavaTextDocumentSnippetRegistry;
import org.junit.Test;

/**
 * Test for JakartaEE snippet registry.
 **/

public class JakartaSnippetRegistryTest {

    JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry();

    @Test
    public void haveJavaSnippets() {
        assertFalse("Tests has no Jakarta Java snippets", registry.getSnippets().isEmpty());
    }

    /**
     * Jakarta Bean Validation snippets - @Email
     */
    @Test
    public void beanValidationSnippetsTest() {
        Optional<Snippet> beanValidationSnippet = findByPrefix("@Email", registry);
        assertTrue("@Email Java snippet is not present in SnippetRegistry", beanValidationSnippet.isPresent());

        snippetsContextTest(beanValidationSnippet, "jakarta.validation.constraints.Email",
                            JavaCursorContextKind.BEFORE_METHOD);

    }

    /**
     * Jakarta Persistence snippets. - persist_context, persist_context_extended,
     * persist_context_extended_unsync, persist_entity.
     */
    @Test
    public void persistenceSnippetsTest() {
        Optional<Snippet> persistContextSnippet = findByPrefix("persist_context", registry);
        assertTrue("persist_context Java snippet is not present in SnippetRegistry", persistContextSnippet.isPresent());

        Optional<Snippet> persistContextExtendedSnippet = findByPrefix("persist_context_extended", registry);
        assertTrue("persist_context_extended Java snippet is not present in SnippetRegistry",
                   persistContextExtendedSnippet.isPresent());

        Optional<Snippet> persistContextExtendedunsyncSnippet = findByPrefix("persist_context_extended_unsync",
                                                                             registry);
        assertTrue("persist_context_extended_unsync Java snippet is not present in SnippetRegistry",
                   persistContextExtendedunsyncSnippet.isPresent());

        Optional<Snippet> persistEntitySnippet = findByPrefix("persist_entity", registry);
        assertTrue("persist_entity Java snippet is not present in SnippetRegistry", persistEntitySnippet.isPresent());

        Optional<Snippet> persistNamedEntityGraphSnippet = findByPrefix("persist_named_entitygraph", registry);
        assertTrue("persist_named_entitygraph Java snippet is not present in SnippetRegistry", persistNamedEntityGraphSnippet.isPresent());

        snippetsContextTest(persistContextSnippet, "jakarta.persistence.PersistenceContextType",
                            JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(persistContextExtendedSnippet, "jakarta.persistence.PersistenceContextType",
                            JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(persistContextExtendedunsyncSnippet, "jakarta.persistence.PersistenceContextType",
                            JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(persistEntitySnippet, "jakarta.persistence.Entity", JavaCursorContextKind.IN_EMPTY_FILE);
        snippetsContextTest(persistNamedEntityGraphSnippet, "jakarta.persistence.NamedEntityGraph", JavaCursorContextKind.BEFORE_CLASS);

    }

    /**
     * Jakarta RESTful Web Services snippets - rest_class, rest_get
     * rest_post, rest_put, rest_delete, rest_head
     */
    @Test
    public void restfulWebServicesSnippetsPrefixTest() {
        Optional<Snippet> restClassSnippet = findByPrefix("rest_class", registry);
        assertTrue("rest_class Java snippet is not present in SnippetRegistry", restClassSnippet.isPresent());

        Optional<Snippet> restGetSnippet = findByPrefix("rest_get", registry);
        assertTrue("rest_get Java snippet is not present in SnippetRegistry", restGetSnippet.isPresent());

        Optional<Snippet> restPostSnippet = findByPrefix("rest_post", registry);
        assertTrue("rest_post Java snippet is not present in SnippetRegistry", restPostSnippet.isPresent());

        Optional<Snippet> restPutSnippet = findByPrefix("rest_put", registry);
        assertTrue("rest_put Java snippet is not present in SnippetRegistry", restPutSnippet.isPresent());

        Optional<Snippet> restDeleteSnippet = findByPrefix("rest_delete", registry);
        assertTrue("rest_delete Java snippet is not present in SnippetRegistry", restDeleteSnippet.isPresent());

        Optional<Snippet> restHeadSnippet = findByPrefix("rest_head", registry);
        assertTrue("rest_head Java snippet is not present in SnippetRegistry", restHeadSnippet.isPresent());

        snippetsContextTest(restClassSnippet, "jakarta.ws.rs.GET", JavaCursorContextKind.IN_EMPTY_FILE);
        snippetsContextTest(restGetSnippet, "jakarta.ws.rs.GET", JavaCursorContextKind.NONE);
        snippetsContextTest(restGetSnippet, "jakarta.ws.rs.GET", JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(restPostSnippet, "jakarta.ws.rs.POST", JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(restPutSnippet, "jakarta.ws.rs.PUT", JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(restDeleteSnippet, "jakarta.ws.rs.DELETE", JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(restHeadSnippet, "jakarta.ws.rs.HEAD", JavaCursorContextKind.BEFORE_METHOD);

    }

    /**
     * Jakarta Servlet snippets - servlet_generic, servlet_doget
     * servlet_dopost, servlet_webfilter
     */
    @Test
    public void ServletSnippetsTest() {
        Optional<Snippet> servletGenericSnippet = findByPrefix("servlet_generic", registry);
        assertTrue("servlet_generic Java snippet is not present in SnippetRegistry", servletGenericSnippet.isPresent());

        Optional<Snippet> servletDoGetSnippet = findByPrefix("servlet_doget", registry);
        assertTrue("servlet_doget Java snippet is not present in SnippetRegistry", servletDoGetSnippet.isPresent());

        Optional<Snippet> servletDoPostSnippet = findByPrefix("servlet_dopost", registry);
        assertTrue("servlet_dopost Java snippet is not present in SnippetRegistry", servletDoPostSnippet.isPresent());

        Optional<Snippet> servletWebFilterSnippet = findByPrefix("servlet_webfilter", registry);
        assertTrue("servlet_webfilter Java snippet is not present in SnippetRegistry",
                   servletWebFilterSnippet.isPresent());

        snippetsContextTest(servletGenericSnippet, "jakarta.servlet.GenericServlet",
                            JavaCursorContextKind.IN_EMPTY_FILE);
        snippetsContextTest(servletDoGetSnippet, "jakarta.servlet.http.HttpServlet",
                            JavaCursorContextKind.IN_EMPTY_FILE);
        snippetsContextTest(servletDoPostSnippet, "jakarta.servlet.http.HttpServlet",
                            JavaCursorContextKind.IN_EMPTY_FILE);
        snippetsContextTest(servletWebFilterSnippet, "jakarta.servlet.Filter", JavaCursorContextKind.IN_EMPTY_FILE);

    }

    /**
     * Jakarta Transactions snippets - tx_user_inject,
     * tx_user_jndi, @ Transactional
     */
    @Test
    public void TransactionsSnippetsTest() {
        Optional<Snippet> txUserInjectSnippet = findByPrefix("tx_user_inject", registry);
        assertTrue("tx_user_inject Java snippet is not present in SnippetRegistry", txUserInjectSnippet.isPresent());

        Optional<Snippet> txUserJndiSnippet = findByPrefix("tx_user_jndi", registry);
        assertTrue("tx_user_jndi Java snippet is not present in SnippetRegistry", txUserJndiSnippet.isPresent());

        Optional<Snippet> transactionalSnippet = findByPrefix("@Transactional", registry);
        assertTrue("@Transactional Java snippet is not present in SnippetRegistry", transactionalSnippet.isPresent());

        snippetsContextTest(txUserInjectSnippet, "jakarta.transaction.UserTransaction",
                            JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(txUserJndiSnippet, "jakarta.transaction.UserTransaction",
                            JavaCursorContextKind.BEFORE_METHOD);
        snippetsContextTest(transactionalSnippet, "jakarta.transaction.Transactional",
                            JavaCursorContextKind.IN_METHOD_ANNOTATIONS);

    }

    @Test
    public void websocketSnippetsTest() {
        Optional<Snippet> wsServerSnippet = findByPrefix("server_endpoint", registry);
        Optional<Snippet> wsClientSnippet = findByPrefix("client_endpoint", registry);
        Optional<Snippet> wsProgrammaticSnippet = findByPrefix("programmatic_endpoint", registry);

        assertTrue("server_endpoint Java snippet is not present in SnippetRegistry", wsServerSnippet.isPresent());
        assertTrue("client_endpoint Java snippet is not present in SnippetRegistry", wsClientSnippet.isPresent());
        assertTrue("programmatic_endpoint Java snippet is not present in SnippetRegistry", wsProgrammaticSnippet.isPresent());

        snippetsContextTest(wsServerSnippet, "jakarta.websocket.server.ServerEndpoint",
                            JavaCursorContextKind.IN_EMPTY_FILE);
        snippetsContextTest(wsClientSnippet, "jakarta.websocket.ClientEndpoint",
                            JavaCursorContextKind.IN_EMPTY_FILE);
        snippetsContextTest(wsProgrammaticSnippet, "jakarta.websocket.Endpoint",
                            JavaCursorContextKind.IN_EMPTY_FILE);

    }

    // Verify whether the snippet is present in the registry.
    private static Optional<Snippet> findByPrefix(String prefix, JavaTextDocumentSnippetRegistry registry) {
        return registry.getSnippets().stream().filter(snippet -> snippet.getPrefixes().contains(prefix)).findFirst();
    }

    // Verify whether the snippet context is present in the registry.
    private void snippetsContextTest(Optional<Snippet> snippet, String contextType,
                                     JavaCursorContextKind javaCursorContextKind) {

        ISnippetContext<?> context = snippet.get().getContext();
        assertNotNull(snippet.get().getPrefixes() + " snippet context is null", context);
        assertTrue(snippet.get().getPrefixes() + " snippet context is not a Java context",
                   context instanceof SnippetContextForJava);

        ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", null, new ArrayList<>());
        boolean match = ((SnippetContextForJava) context).isMatch(context(projectInfo, javaCursorContextKind));
        assertFalse("Project should not have " + contextType + " type", match);

        ProjectLabelInfoEntry projectInfo1 = new ProjectLabelInfoEntry("", null, Arrays.asList(contextType));
        boolean match1 = ((SnippetContextForJava) context).isMatch(context(projectInfo1, javaCursorContextKind,
                                                                           snippet.get().getPrefixes().isEmpty() ? "" : snippet.get().getPrefixes().get(0)));
        if (javaCursorContextKind.getValue() == 10) {
            assertFalse("Project should not have " + contextType + " type", match);
        } else {
            assertTrue("Project has no " + contextType + " type", match1);
        }

    }

    private static JavaSnippetCompletionContext context(ProjectLabelInfoEntry projectInfo,
                                                        JavaCursorContextKind javaCursorContext) {
        return context(projectInfo, javaCursorContext, "");
    }

    private static JavaSnippetCompletionContext context(ProjectLabelInfoEntry projectInfo,
                                                        JavaCursorContextKind javaCursorContext, String prefix) {
        return new JavaSnippetCompletionContext(projectInfo, new JavaCursorContextResult(javaCursorContext, prefix));
    }
}
