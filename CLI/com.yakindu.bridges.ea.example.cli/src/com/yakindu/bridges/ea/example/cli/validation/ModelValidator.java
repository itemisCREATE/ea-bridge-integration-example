package com.yakindu.bridges.ea.example.cli.validation;

import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.StateMachine;

import com.yakindu.bridges.ea.example.cli.AbstractResourceProcessor;
import com.yakindu.bridges.ea.example.cli.load.UMLElementCollector;

public class ModelValidator extends AbstractResourceProcessor {

	@Override
	protected String run(Resource resource, String[] args) {
		final String reportFile = args[0];
		final String nameOrGuid = getNameOrGuidFromArguments(skip(args, 1));
		final boolean verbose = getVerboseFlagFromArguments(skip(args, 1));

		final List<Element> loadedElements = report("Collecting UML Elements", () -> {
			try {
				return UMLElementCollector.loadElements(resource, nameOrGuid, verbose);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			return null;
		});

		final List<StateMachine> stms = UMLElementCollector.collectStatemachines(loadedElements);

		final ModelValidation validation;
		try {
			validation = new ModelValidation(resource, loadedElements, stms, reportFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		final int count = report("Validating", () -> {
			final List<String> issues = validation.validate(false);
			if (verbose)
				issues.forEach(System.out::println);
			return issues.size();
		});
		if (count > 0) {
			report(String.format("Writing report (%d issue%s) ", count, count == 1 ? "" : "s"),
					() -> validation.createReport(verbose));

			return String.format("%d issue%s found.", count, count == 1 ? "" : "s");
		} else {
			return "No issues found.";
		}
	}
}
