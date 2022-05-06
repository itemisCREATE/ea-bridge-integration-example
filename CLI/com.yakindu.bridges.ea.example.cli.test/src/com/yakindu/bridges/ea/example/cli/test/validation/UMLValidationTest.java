package com.yakindu.bridges.ea.example.cli.test.validation;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;

import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;
import com.yakindu.bridges.ea.example.cli.validation.report.ValidationReport;
import com.yakindu.bridges.ea.example.cli.validation.uml.UMLValidation;

public class UMLValidationTest {

	@Test
	public void validationWithoutErrors() {
		// given: simple UML model without errors
		final Model model = (Model) EcoreUtil.create((EClass) UMLPackage.eINSTANCE.getEClassifier("Model"));
		final Package pack = model.createNestedPackage("pack");
		pack.createPackagedElement("someClass", UMLPackage.Literals.CLASS);
		final URI uri = URI.createURI("mem:/test.uml");
		final Resource resource = new ResourceSetImpl().createResource(uri);
		resource.getContents().add(model);
		// when
		final ValidationResult result = new ValidationResult(resource);
		UMLValidation.validate(resource, result);
		// then
		assertEquals("unexpected validation results: " + result, 0, result.count());
	}

	@Test
	public void validateWithUmlErrorsAndWarnings() {
		// given: simple model with two identical classes in the same package
		final Model model = (Model) EcoreUtil.create((EClass) UMLPackage.eINSTANCE.getEClassifier("Model"));
		final Package pack = model.createNestedPackage("pack");
		pack.createPackagedElement("duplicateClass", UMLPackage.Literals.CLASS);
		pack.createPackagedElement("duplicateClass", UMLPackage.Literals.CLASS);

		final URI uri = URI.createURI("mem:/test.uml");
		final Resource resource = new ResourceSetImpl().createResource(uri);
		resource.getContents().add(model);
		// when
		final ValidationResult result = new ValidationResult(resource);
		UMLValidation.validate(resource, result);
		ValidationReport report = new ValidationReport(result);
		System.out.println(report.create());
		// then
		assertEquals("unexpected validation results: " + result, 4,
				result.getUmlErrors().size() + result.getUmlWarnings().size());
	}

}
