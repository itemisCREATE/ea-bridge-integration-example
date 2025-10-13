package com.yakindu.bridges.ea.example.cli.validation.uml;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;

public class UMLValidation {

	private final Resource resource;
	private final UMLNameBeautifier nameBeautifier;

	public UMLValidation(Resource resource) {
		this.resource = resource;
		this.nameBeautifier = new UMLNameBeautifier(resource);
	}

	public void validate(ValidationResult result) {
		final Diagnostic diagnostics = UMLDiagnostician.INSTANCE.validate(resource);

		if (diagnostics != null && diagnostics.getSeverity() >= Diagnostic.WARNING) {
			collectErrorsAndWarnings(diagnostics, result);
		}
	}

	private void collectErrorsAndWarnings(Diagnostic diagnostic, ValidationResult result) {
		if (diagnostic.getSeverity() >= Diagnostic.WARNING) {

			// check current message
			final EObject object = getModelElement(diagnostic.getData());
			if (isRelevantIssue(object, diagnostic)) {
				if (diagnostic.getSeverity() == Diagnostic.WARNING) {
					result.addUmlWarning(object, formatMessage(diagnostic));
				} else {
					result.addUmlError(object, formatMessage(diagnostic));
				}
			}

			// recursive call
			for (final Diagnostic child : diagnostic.getChildren()) {
				collectErrorsAndWarnings(child, result);
			}
		} // else: only ok / info
	}

	private String formatMessage(Diagnostic diagnostic) {
		return nameBeautifier.format(diagnostic.getMessage());
	}

	private EObject getModelElement(Object data) {
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

	private boolean isRelevantIssue(final EObject object, final Diagnostic diagnostic) {
		final String message = formatMessage(diagnostic);

		if (message == null || message.isEmpty())
			return false; // no message - nothing reasonable to report

		if (!diagnostic.getChildren().isEmpty() && message.startsWith("Diagnosis of "))
			return false; // skip 'container' diagnostics objects without actual issues

		return true; // good location for a break point
	}
}
