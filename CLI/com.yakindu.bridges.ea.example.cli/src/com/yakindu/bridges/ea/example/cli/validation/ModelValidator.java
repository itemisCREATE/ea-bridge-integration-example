package com.yakindu.bridges.ea.example.cli.validation;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;

import com.yakindu.bridges.ea.example.cli.AbstractResourceProcessor;

public class ModelValidator extends AbstractResourceProcessor {

	@Override
	protected String run(Resource resource, String[] args) throws Exception {
		final String reportFile = args[0];
		final String nameOrGuid = getNameOrGuidFromArguments(skip(args, 1));
		final boolean verbose = getVerboseFlagFromArguments(skip(args, 1));

		final List<Element> loadedElements = report("Collecting UML Elements", () -> {
			try {
				return loadElements(resource, nameOrGuid, verbose);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});

		final ModelValidation validation = new ModelValidation(resource, loadedElements, reportFile);

		final int count = report("Validating", () -> {
			final List<String> issues = validation.validate();
			if (verbose)
				issues.forEach(System.out::println);
			return issues.size();
		});
		if (count > 0) {
			report(String.format("Writing report (%d issue%s)", count, count == 1 ? "" : "s"),
					validation::createReport);

			return String.format("%d issue%s found.", count, count == 1 ? "" : "s");
		} else {
			return "No issues found.";
		}
	}
}
