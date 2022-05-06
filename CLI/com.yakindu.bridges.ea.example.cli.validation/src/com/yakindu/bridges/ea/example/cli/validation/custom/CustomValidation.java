package com.yakindu.bridges.ea.example.cli.validation.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;

import com.yakindu.bridges.ea.core.utils.MapUtils;
import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;

public class CustomValidation {
	private static List<AbstractValidationRule<?>> VALIDATION_RULES = Arrays.asList(
			// custom validation rules can be enabled here
			new RuleAbstractClassPrefix());

	private final Resource resource;
	private final List<Element> elements;
	private final Map<Class<? extends Element>, List<AbstractValidationRule<?>>> rules;

	public CustomValidation(Resource resource, List<Element> elementsToValidate) {
		if (resource == null)
			throw new IllegalArgumentException("Resource must not be null");
		this.resource = resource;
		this.elements = elementsToValidate;
		rules = initRules();
	}

	private Map<Class<? extends Element>, List<AbstractValidationRule<?>>> initRules() {
		final Map<Class<? extends Element>, List<AbstractValidationRule<?>>> retval = new HashMap<>(
				VALIDATION_RULES.size());
		VALIDATION_RULES.forEach(rule -> MapUtils.addToListMap(retval, rule.applicableTo, rule));
		return Collections.unmodifiableMap(retval);
	}

	public void validate(ValidationResult result) {
		if (elements == null || elements.isEmpty()) {
			resource.getContents().stream() //
					.filter(Package.class::isInstance).map(Package.class::cast) //
					.forEach(pack -> doValidate(pack, result));
		} else {
			elements.stream().map(Element::getNearestPackage).distinct() //
					.forEach(pack -> doValidate(pack, result));
		}
	}

	public void doValidate(Package pack, ValidationResult result) {
		for (TreeIterator<EObject> iter = pack.eAllContents(); iter.hasNext();) {
			final EObject obj = iter.next();

			for (Entry<Class<? extends Element>, List<AbstractValidationRule<?>>> classWithRule : rules.entrySet()) {
				if (classWithRule.getKey().isInstance(obj)) {
					classWithRule.getValue().forEach(rule -> rule.validate((Element) obj, result));
				}
			}
		}
	}
}
