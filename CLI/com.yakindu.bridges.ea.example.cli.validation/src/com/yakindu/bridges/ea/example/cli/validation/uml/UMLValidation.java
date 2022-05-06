package com.yakindu.bridges.ea.example.cli.validation.uml;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;

public class UMLValidation {

	private UMLValidation() {
		throw new IllegalStateException("Static Class");
	}

	public static void validate(Resource resource, ValidationResult result) {
		final Diagnostic diagnostics = UMLDiagnostician.INSTANCE.validate(resource);

		if (diagnostics != null && diagnostics.getSeverity() >= Diagnostic.WARNING) {
			collectErrorsAndWarnings(diagnostics, result);
		}
	}

	private static void collectErrorsAndWarnings(Diagnostic diagnostic, ValidationResult result) {
		if (diagnostic.getSeverity() >= Diagnostic.WARNING) {

			// check current message
			final EObject object = getModelElement(diagnostic.getData());
			if (isRelevantIssue(object, diagnostic.getMessage())) {
				if (diagnostic.getSeverity() == Diagnostic.WARNING) {
					result.addUmlWarning(object, diagnostic.getMessage());
				} else {
					result.addUmlError(object, diagnostic.getMessage());
				}
			}

			// recursive call
			for (final Diagnostic child : diagnostic.getChildren()) {
				collectErrorsAndWarnings(child, result);
			}
		} // else: only ok / info
	}

	private static EObject getModelElement(Object data) {
		if (data == null || data instanceof EObject)
			return (EObject) data;
		if (data instanceof Iterable<?>) {
			for (Object obj : (Iterable<?>) data) {
				final EObject element = getModelElement(obj);
				if (element != null)
					return element;
			}
		}
		return null;
	}

	private static boolean isRelevantIssue(final EObject object, final String message) {
		if (message == null || message.isEmpty())
			return false; // no message - nothing reasonable to report
		return true; // good location for a break point
	}
}
