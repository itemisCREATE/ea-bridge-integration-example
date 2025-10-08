package com.yakindu.bridges.ea.example.cli.test.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;

import com.google.gson.Gson;
import com.yakindu.bridges.ea.example.cli.validation.ModelValidation;
import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;
import com.yakindu.bridges.ea.example.cli.validation.report.ValidationReport;
import com.yakindu.bridges.ea.example.cli.validation.report.ValidationResultsJsonContainer;

public class ValidationReportTest {

	@Test
	public void emptyReport() {
		// given
		final Resource resource = new ResourceSetImpl().createResource(URI.createURI("mem:/test.uml"));
		final ValidationResult result = new ValidationResult(resource);
		// when
		final String report = new ValidationReport(result).create();
		// then
		assertEquals("unexpected empty report",
				"{\"resourceIssues\":[],\"loadIssues\":[],\"umlIssues\":[],\"customIssues\":[]}", report);

	}

	@Test
	public void jsonReportShouldBeValid() throws FileNotFoundException, IOException {
		final Model model = (Model) EcoreUtil.create((EClass) UMLPackage.eINSTANCE.getEClassifier("Model"));
		final URI uri = URI.createURI("mem:/test.uml", true);
		final Resource resource = new ResourceSetImpl().createResource(uri);
		resource.getContents().add(model);

		Package pack = model.createNestedPackage("pack");

		final Class abstractClass = pack.createOwnedClass("someClass", false);
		abstractClass.setIsAbstract(true);

		final Class childClass = pack.createOwnedClass("childClass", false);
		final Class anotherChildClass = pack.createOwnedClass("anotherChildClass", false);

		final Package pack2 = model.createNestedPackage("pack2");
		pack2.createOwnedClass("someClass", false);

		// create inheritance connectors
		childClass.createGeneralization(abstractClass);
		anotherChildClass.createGeneralization(abstractClass);

		// when
		ModelValidation validation = new ModelValidation(resource, Arrays.asList(abstractClass), null,
				File.createTempFile(getClass().getName(), ".json").getAbsolutePath());

		validation.validate(false);
		ValidationResult validationResult = validation.getValidationResult();

		validation = new ModelValidation(resource, Arrays.asList(pack2), null,
				File.createTempFile(getClass().getName(), ".json").getAbsolutePath());
		validation.validate(false);
		validationResult = validation.getValidationResult();
		String report = new ValidationReport(validationResult).create();

		Gson jsonHandler = new Gson();
		ValidationResultsJsonContainer deserializedJson = jsonHandler.fromJson(report,
				ValidationResultsJsonContainer.class);
		assertNotNull("json validation result is not written correctly", deserializedJson);
		assertEquals(validationResult.count(), deserializedJson.count());
	}
}
