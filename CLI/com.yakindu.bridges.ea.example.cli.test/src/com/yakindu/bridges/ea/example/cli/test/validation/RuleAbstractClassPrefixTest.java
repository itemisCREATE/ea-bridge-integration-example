package com.yakindu.bridges.ea.example.cli.test.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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

import com.yakindu.bridges.ea.example.cli.validation.custom.RuleAbstractClassPrefix;

public class RuleAbstractClassPrefixTest {

	@Test
	public void reportAbstractClassesWithoutPrefix() {
		// given
		final Model model = (Model) EcoreUtil.create((EClass) UMLPackage.eINSTANCE.getEClassifier("Model"));
		final URI uri = URI.createURI("mem:/test.uml", true);
		final Resource resource = new ResourceSetImpl().createResource(uri);
		resource.getContents().add(model);

		Package context = model.createNestedPackage("pack");

		final Class abstractClass = context.createOwnedClass("someClass", false);
		abstractClass.setIsAbstract(true);

		final Class childClass = context.createOwnedClass("childClass", false);
		final Class anotherChildClass = context.createOwnedClass("anotherChildClass", false);

		// create inheritance connectors
		childClass.createGeneralization(abstractClass);
		anotherChildClass.createGeneralization(abstractClass);

		// when
		final List<String> errors = new ArrayList<>();
		final List<String> warnings = new ArrayList<>();
		new RuleAbstractClassPrefix().validate(abstractClass, errors::add, warnings::add);

		// then
		assertEquals("Unexpected number of errors reported: ", 0, errors.size());
		assertEquals("Unexpected warnings reported", 1, warnings.size());

		final String warning = warnings.get(0);
		assertTrue("Unexpected validation error: " + warning,
				warning.startsWith("Abstract class 'someClass' should start with 'Abstract'."));

	}

}
