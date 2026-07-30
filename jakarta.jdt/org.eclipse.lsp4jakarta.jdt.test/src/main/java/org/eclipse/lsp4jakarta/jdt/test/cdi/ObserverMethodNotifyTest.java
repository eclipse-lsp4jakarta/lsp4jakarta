package org.eclipse.lsp4jakarta.jdt.test.cdi;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.test.core.BaseJakartaTest;
import org.junit.Test;

/**
 * Tests for CDI ObserverMethod notify-method validation.
 *
 * A class that directly implements ObserverMethod must override at least one of
 * notify(T) or notify(EventContext&lt;T&gt;).
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#after_bean_discovery">CDI 3.0 spec</a>
 */
public class ObserverMethodNotifyTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * Verifies that a class implementing ObserverMethod without overriding any
     * notify method triggers {@code InvalidObserverMethodWithoutNotify} and that
     * two quick-fix code actions are offered.
     */
    @Test
    public void testObserverMethodWithoutNotify() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/AuditObserver.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic expectedDiagnostic = d(19, 13, 26,
                                          "The class 'AuditObserver' implements ObserverMethod but does not override notify(T) or notify(EventContext<T>). "
                                                      + "At least one of these methods must be overridden for the container to invoke the observer.",
                                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidObserverMethodWithoutNotify");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, expectedDiagnostic);

        // notify(AuditEvent event) quick fix — type arg resolved from ObserverMethod<AuditEvent>
        TextEdit notifyEventEdit = te(46, 5, 46, 5, "\n\n\t@Override\n\tpublic void notify(AuditEvent event) {\n\t}");
        CodeAction notifyEventAction = ca(uri, "Override notify(AuditEvent event)", expectedDiagnostic, notifyEventEdit);

        // notify(EventContext<AuditEvent> eventContext) quick fix
        TextEdit notifyEventContextEdit = te(46, 5, 46, 5, "\n\n\t@Override\n\tpublic void notify(EventContext<AuditEvent> eventContext) {\n\t}");
        CodeAction notifyEventContextAction = ca(uri, "Override notify(EventContext<AuditEvent> eventContext)", expectedDiagnostic, notifyEventContextEdit);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, notifyEventAction, notifyEventContextAction);
    }

    /**
     * Verifies that a class implementing ObserverMethod that overrides notify(T)
     * does NOT trigger a diagnostic.
     */
    @Test
    public void testObserverMethodWithNotifyOverride() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/AuditObserverWithNotify.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Verifies that a class implementing ObserverMethod that overrides only the
     * {@code notify(EventContext<T>)} overload does NOT trigger a diagnostic.
     * Either overload satisfies the contract.
     */
    @Test
    public void testObserverMethodWithEventContextNotifyOverride() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/AuditObserverWithEventContext.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Verifies that a class implementing the raw (non-parameterised) {@code ObserverMethod}
     * type without a notify override triggers the diagnostic. The quick-fix labels must
     * fall back to {@code Object} because no type argument is present.
     */
    @Test
    public void testObserverMethodRawTypeWithoutNotify() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/AuditObserverRawType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // "AuditObserverRawType" starts at col 13 and ends at col 33 (length 20).
        Diagnostic rawTypeDiagnostic = d(19, 13, 33,
                                         "The class 'AuditObserverRawType' implements ObserverMethod but does not override notify(T) or notify(EventContext<T>). "
                                                     + "At least one of these methods must be overridden for the container to invoke the observer.",
                                         DiagnosticSeverity.Error, "jakarta-cdi", "InvalidObserverMethodWithoutNotify");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, rawTypeDiagnostic);
    }

    /**
     * Verifies that an abstract class implementing ObserverMethod does NOT trigger a
     * diagnostic. Abstract classes are exempt — they may defer notify to concrete subclasses.
     */
    @Test
    public void testObserverMethodAbstractClassIsExempt() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/AbstractAuditObserver.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
