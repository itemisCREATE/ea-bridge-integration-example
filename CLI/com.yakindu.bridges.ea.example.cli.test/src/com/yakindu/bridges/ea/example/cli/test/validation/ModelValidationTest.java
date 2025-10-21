package com.yakindu.bridges.ea.example.cli.test.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import com.yakindu.bridges.ea.example.cli.validation.ModelValidation;
import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;

public class ModelValidationTest {

	@Test
	public void missingReportFileArgument() {
		// given
		final Resource resource = new ResourceSetImpl().createResource(URI.createURI("mem:/test.uml"));
		try {
			// when
			new ModelValidation(resource, null, Collections.emptyList(), " ");
			fail("exception expected about missing report file argument");
		} catch (Exception e) {
			// then
			assertTrue("CLI exception expected", e instanceof Exception);
			assertEquals("exception expected about missing report file argument", "Report file argument missing",
					e.getMessage());
		}
	}

	@Test
	public void validationForSingleElementInModel() throws FileNotFoundException, IOException {
		final Model model = (Model) EcoreUtil.create((EClass) UMLPackage.eINSTANCE.getEClassifier("Model"));
		final URI uri = URI.createURI("mem:/test.uml", true);
		final Resource resource = new ResourceSetImpl().createResource(uri);
		resource.getContents().add(model);

		Package pack = model.createNestedPackage("pack");

		final Class abstractClass = pack.createOwnedClass("someClass", false);
		abstractClass.setIsAbstract(true);

		final Class childClass = pack.createOwnedClass("childClass", false);
		final Class anotherChildClass = pack.createOwnedClass("anotherChildClass", false);

		// create inheritance connectors
		childClass.createGeneralization(abstractClass);
		anotherChildClass.createGeneralization(abstractClass);

		// when

		ModelValidation validation = new ModelValidation(resource, Arrays.asList(abstractClass), null,
				File.createTempFile(getClass().getName(), ".json").getAbsolutePath());

		validation.validate(false);
		ValidationResult validationResult = validation.getValidationResult();

		// then

		assertEquals("Unexpected number of warnings reported", 1, validationResult.getCustomWarnings().size());
		assertEquals("Unexpected number of errors reported", 0, validationResult.getCustomErrors().size());
		List<String> warning = validationResult.getCustomWarnings().get(abstractClass);

		// validation for the other two classes in this package should report report the same warning
		// because the validator will validate all elements in the nearest package.

		// when

		validation = new ModelValidation(resource, Arrays.asList(childClass, anotherChildClass), null,
				File.createTempFile(getClass().getName(), ".json").getAbsolutePath());
		validation.validate(false);
		validationResult = validation.getValidationResult();

		// then

		assertEquals("Unexpected number of warnings reported", 1, validationResult.getCustomWarnings().size());
		assertEquals("Unexpected number of errors reported", 0, validationResult.getCustomErrors().size());
		assertNotNull("warning is not reported to the correct model element",
				validationResult.getCustomWarnings().get(abstractClass));
		assertEquals(validationResult.getCustomWarnings().get(abstractClass), warning);

		// validation of a different package should not include issues found in the package `context`
		// when

		Package pack2 = model.createNestedPackage("pack2");
		pack2.createOwnedClass("someClass", false);
		validation = new ModelValidation(resource, Arrays.asList(pack2), null,
				File.createTempFile(getClass().getName(), ".json").getAbsolutePath());
		validation.validate(false);
		validationResult = validation.getValidationResult();

		// then

		assertEquals("Unexpected number of warnings reported", 0, validationResult.getCustomWarnings().size());
		assertEquals("Unexpected number of errors reported", 0, validationResult.getCustomErrors().size());

	}

}
