package com.yakindu.bridges.ea.example.cli.validation.report;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.resource.UMLResource;

import com.google.gson.Gson;
import com.yakindu.bridges.ea.core.EAResource;
import com.yakindu.bridges.ea.core.utils.EAResourceUtils;
import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;

public class ValidationReport {

	private final ValidationResult result;
	private final StringBuilder output;

	public ValidationReport(ValidationResult result) {
		this.result = result;
		this.output = new StringBuilder();
	}

	public String create() {
		if (output.length() == 0) {
			List<ValidationIssueJsonContainer> customIssues = createValidationIssuesForElement(result.getCustomErrors(),
					result.getCustomWarnings());

			List<ValidationIssueJsonContainer> loadIssues = createValidationIssuesForElement(result.getLoadErrors(),
					result.getLoadWarnings());

			List<ValidationIssueJsonContainer> umlErrors = createValidationIssuesForElement(result.getUmlErrors(),
					result.getUmlWarnings());

			List<ValidationIssueJsonContainer> resourceIssues = appendResourceIssues();

			ValidationResultsJsonContainer serializableResult = new ValidationResultsJsonContainer(resourceIssues,
					loadIssues, umlErrors, customIssues);

			// create Json serializer
			Gson jsonHandler = new Gson();
			output.append(jsonHandler.toJson(serializableResult));

		}
		return output.toString();
	}

	private List<ValidationIssueJsonContainer> createValidationIssuesForElement(Map<EObject, List<String>> errors,
			Map<EObject, List<String>> warnings) {
		List<ValidationIssueJsonContainer> retVal = new ArrayList<>();

		for (Entry<EObject, List<String>> elementEntry : errors.entrySet()) {
			final Entry<String, String> ids = getIds(elementEntry.getKey());

			for (String error : elementEntry.getValue()) {
				String type = ids.getKey() != null ? ids.getKey() : "";
				String id = ids.getValue() != null ? ids.getValue() : "";
				retVal.add(new ValidationIssueJsonContainer("E", type, id, error));
			}
		}

		for (Entry<EObject, List<String>> elementEntry : warnings.entrySet()) {
			final Entry<String, String> ids = getIds(elementEntry.getKey());
			for (String error : elementEntry.getValue()) {
				String type = ids.getKey() != null ? ids.getKey() : "";
				String id = ids.getValue() != null ? ids.getValue() : "";
				retVal.add(new ValidationIssueJsonContainer("W", type, id, error));
			}
		}

		return retVal;
	}

	private List<ValidationIssueJsonContainer> appendResourceIssues() {
		List<ValidationIssueJsonContainer> retVal = new ArrayList<>();

		for (String error : result.getResourceErrors()) {
			retVal.add(new ValidationIssueJsonContainer("E", "", "", error));
		}
		for (String warning : result.getResourceWarnings()) {
			retVal.add(new ValidationIssueJsonContainer("W", "", "", warning));
		}

		return retVal;
	}

	private Entry<String, String> getIds(EObject element) {
		if (element == null)
			return null;
		if (element.eResource() instanceof EAResource) {
			final String objectType = EAResourceUtils.getObjectTypeForElement(element);
			if (objectType != null) {
				final Integer id = EAResourceUtils.getIdForElement(element);
				final String idStr = id == null ? EAResourceUtils.getGuidForElement(element) : String.valueOf(id);
				if (idStr != null) {
					return new SimpleImmutableEntry<>(objectType, idStr);
				}
			}
			return getIds(element.eContainer());
		}
		if (element instanceof UMLResource || element instanceof Model || element instanceof Class
				|| element instanceof Package) {
			final String clazz = element.eClass().getName();
			final String fragment = element.eResource().getURIFragment(element);
			return new SimpleImmutableEntry<>(clazz, fragment);
		}
		throw new IllegalArgumentException("Unexpected element: " + element);
	}
}
