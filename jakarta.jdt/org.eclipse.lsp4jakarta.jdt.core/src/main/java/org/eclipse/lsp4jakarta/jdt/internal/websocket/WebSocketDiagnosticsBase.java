package org.eclipse.lsp4jakarta.jdt.internal.websocket;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

public class WebSocketDiagnosticsBase {

    protected void invalidParamsCheck(JavaDiagnosticsContext context, String uri, IType type, ICompilationUnit unit,
                                      List<Diagnostic> diagnostics) throws JavaModelException {
        IMethod[] allMethods = type.getMethods();
        for (IMethod method : allMethods) {
            IAnnotation[] allAnnotations = method.getAnnotations();
            Set<String> specialParamTypes = null, rawSpecialParamTypes = null;

            for (IAnnotation annotation : allAnnotations) {
                String annotationName = annotation.getElementName();
                ErrorCode diagnosticErrorCode = null;

                if (DiagnosticUtils.isMatchedJavaElement(type, annotationName, Constants.ON_OPEN)) {
                    specialParamTypes = Constants.ON_OPEN_PARAM_OPT_TYPES;
                    rawSpecialParamTypes = Constants.RAW_ON_OPEN_PARAM_OPT_TYPES;
                    diagnosticErrorCode = ErrorCode.InvalidOnOpenParams;
                } else if (DiagnosticUtils.isMatchedJavaElement(type, annotationName, Constants.ON_CLOSE)) {
                    specialParamTypes = Constants.ON_CLOSE_PARAM_OPT_TYPES;
                    rawSpecialParamTypes = Constants.RAW_ON_CLOSE_PARAM_OPT_TYPES;
                    diagnosticErrorCode = ErrorCode.InvalidOnCloseParams;
                }
                if (diagnosticErrorCode != null) {
                    ILocalVariable[] allParams = method.getParameters();
                    for (ILocalVariable param : allParams) {
                        String signature = param.getTypeSignature();
                        String formatSignature = signature.replace("/", ".");
                        String resolvedTypeName = JavaModelUtil.getResolvedTypeName(formatSignature, type);
                        boolean isPrimitive = JavaModelUtil.isPrimitive(formatSignature);
                        boolean isSpecialType;
                        boolean isPrimWrapped;

                        if (resolvedTypeName != null) {
                            isSpecialType = specialParamTypes.contains(resolvedTypeName);
                            isPrimWrapped = isWrapper(resolvedTypeName);
                        } else {
                            String simpleParamType = Signature.getSignatureSimpleName(signature);
                            isSpecialType = rawSpecialParamTypes.contains(simpleParamType);
                            isPrimWrapped = isWrapper(simpleParamType);
                        }

                        // check parameters valid types
                        if (!(isSpecialType || isPrimWrapped || isPrimitive)) {
                            Range range = PositionUtils.toNameRange(param, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     createParamTypeDiagMsg(specialParamTypes, annotationName), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     diagnosticErrorCode, DiagnosticSeverity.Error));
                            continue;
                        }

                        if (!isSpecialType) {
                            // check that if parameter is not a specialType, it has a @PathParam annotation
                            IAnnotation[] param_annotations = param.getAnnotations();
                            boolean hasPathParamAnnot = Arrays.asList(param_annotations).stream().anyMatch(annot -> {
                                try {
                                    return DiagnosticUtils.isMatchedJavaElement(type, annot.getElementName(),
                                                                                Constants.PATH_PARAM_ANNOTATION);
                                } catch (JavaModelException e) {
                                    JakartaCorePlugin.logException("Failed to get matched annotation", e);
                                    return false;
                                }
                            });
                            if (!hasPathParamAnnot) {
                                Range range = PositionUtils.toNameRange(param, context.getUtils());
                                diagnostics.add(context.createDiagnostic(uri,
                                                                         Messages.getMessage("PathParamsAnnotationMissing"), range,
                                                                         Constants.DIAGNOSTIC_SOURCE, null,
                                                                         ErrorCode.PathParamsMissingFromParam, DiagnosticSeverity.Error));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if valueClass is a wrapper object for a primitive value Based on
     * https://github.com/eclipse/lsp4mp/blob/9789a1a996811fade43029605c014c7825e8f1da/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/core/utils/JDTTypeUtils.java#L294-L298
     *
     * @param valueClass the resolved type of valueClass in string or the simple
     *            type of valueClass
     * @return if valueClass is a wrapper object
     */
    private boolean isWrapper(String valueClass) {
        return Constants.WRAPPER_OBJS.contains(valueClass)
               || Constants.RAW_WRAPPER_OBJS.contains(valueClass);
    }

    private String createParamTypeDiagMsg(Set<String> methodParamOptTypes, String methodAnnotTarget) {
        String paramMessage = String.join("\n- ", methodParamOptTypes);
        return Messages.getMessage("WebSocketParamType", "@" + methodAnnotTarget, paramMessage);
    }

}
