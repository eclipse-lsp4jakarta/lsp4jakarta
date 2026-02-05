package org.eclipse.lsp4jakarta.jdt.internal.jaxrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

public class JaxrsDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

	private static final Logger LOGGER = Logger.getLogger(JaxrsDiagnosticsParticipant.class.getName());

	@Override
	public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		String uri = context.getUri();
		IJDTUtils utils = JDTUtilsLSImpl.getInstance();
		ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		List<Diagnostic> diagnostics = new ArrayList<>();

		if (unit == null) {
			return diagnostics;
		}

		IType[] alltypes = unit.getAllTypes();
		IMethod[] methods;

		for (IType type : alltypes) {
			if (!type.isClass()) {
				continue;
			}

			boolean isJaxrsClass = false;
			// Iterate class level annotations
			for (IAnnotation annotation : type.getAnnotations()) {
				isJaxrsClass = DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
						Constants.PATH_ANNOTATION);
			}

			if (isJaxrsClass) {
				methods = type.getMethods();
				for (IMethod method : methods) {
					if (DiagnosticUtils.isConstructorMethod(method) || validateSetterMethod(method)) {
						for (ILocalVariable param : method.getParameters()) {
							Stream.of(param.getAnnotations())
						    .filter(paramAnnotation -> {
								try {
									return isConstraintAnnotation(paramAnnotation, type, unit);
								} catch (JavaModelException e) {			
									return false;
								}
							})
						    .map(paramAnnotation -> {
								try {
									return PositionUtils.toNameRange(paramAnnotation, context.getUtils());
								} catch (JavaModelException e) {
									return null;
								}
							})
						    .map(annotationRange -> context.createDiagnostic(uri,
						        Messages.getMessage("InvalidConstraintTarget"),
						        annotationRange, 
						        Constants.DIAGNOSTIC_SOURCE, 
						        ErrorCode.InvalidConstraintTarget,
						        DiagnosticSeverity.Error))
						    .forEach(diagnostics::add);
						}
					}
				}
			}
		}

		return diagnostics;

	}

	private boolean isConstraintAnnotation(IAnnotation annotation, IType type, ICompilationUnit cu)
			throws JavaModelException {
		boolean isConstraint = false;
		String annotationFQ = ManagedBean.getFullyQualifiedClassName(type, annotation.getElementName());
		IJavaProject project = annotation.getJavaProject();

		if (project != null && annotationFQ != null) {
			IType annotationType = project.findType(annotationFQ);
			if (annotationType != null) {
				isConstraint = Arrays.stream(annotationType.getAnnotations()).anyMatch(constraintAnnotation -> {
					try {
						return DiagnosticUtils.isMatchedAnnotation(cu, constraintAnnotation,
								Constants.CONSTRAINT_ANNOTATION);
					} catch (JavaModelException e) {
						LOGGER.log(Level.INFO, "Unable to fetch constraint information", e.getMessage());
						return false;
					}
				});

			}
		}
		return isConstraint;
	}

	private boolean validateSetterMethod(IMethod method) throws JavaModelException {
		if (!method.getElementName().startsWith("set") || !"V".equalsIgnoreCase(method.getReturnType()) || method.getParameterTypes().length != 1) {
			return false;
		}
		return true;
	}

}
