package com.yakindu.bridges.ea.example.cli.codegen;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.StateMachine;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.yakindu.bridges.ea.example.cli.AbstractResourceProcessor;
import com.yakindu.bridges.ea.example.cli.ExampleCLI;
import com.yakindu.bridges.ea.example.cli.codegen.StmCodeGenerator.LANG;
import com.yakindu.bridges.ea.example.cli.codegen.util.StatechartUtil;
import com.yakindu.bridges.ea.example.cli.load.UMLElementCollector;
import com.yakindu.sct.domain.c.runtime.CSTextRuntimeModule;
import com.yakindu.sct.model.sgraph.Statechart;
import com.yakindu.sct.model.stext.STextRuntimeModule;
import com.yakindu.sct.uml2.transformation.IStatemachineTransformation;
import com.yakindu.sct.uml2.transformation.module.TransformationModule;

public class CodeGeneratorFromUml extends AbstractResourceProcessor {

	@Inject
	private IStatemachineTransformation trafo;

	@Inject
	private StmCodeGenerator generator;

	@Inject
	private StatechartUtil statechartApi;

	private final LANG language;

	public CodeGeneratorFromUml(LANG language) {
		this.language = language;
	}

	@Override
	public String run(Resource resource, String[] args) {
		if (args == null || args[0].isBlank())
			throw new IllegalArgumentException("Mandatory argument <output-folder> is missing.");
		final String outputFolder = getAndValidateOutputFolder(args);
		final String nameOrGuid = getNameOrGuidFromArguments(skip(args, 1));
		final boolean verbose = getVerboseFlagFromArguments(skip(args, 1));

		final Set<String> stateMachinesFailed = new TreeSet<>();
		final Set<String> stateMachinesSuccess = new TreeSet<>();
		genStms(resource, outputFolder, nameOrGuid, verbose, stateMachinesFailed, stateMachinesSuccess);

		if (!stateMachinesFailed.isEmpty()) {
			if (!stateMachinesSuccess.isEmpty()) {
				throw new RuntimeException("Failed to generate for: " + stateMachinesFailed.toString()
						+ "\nSuccessfully generated for: " + stateMachinesSuccess.toString());
			} else {
				throw new RuntimeException("Failed to generate for: " + stateMachinesFailed.toString());
			}
		}
		return stateMachinesSuccess.toString();
	}

	protected void genStms(Resource resource, final String outputFolder, final String nameOrGuid, final boolean verbose,
			final Set<String> stateMachinesFailed, final Set<String> stateMachinesSuccess) {
		final List<Element> elements = UMLElementCollector.loadElements(resource, nameOrGuid, verbose);
		final Collection<StateMachine> loadedStatemachines = UMLElementCollector.collectStatemachines(elements);
		if (loadedStatemachines.isEmpty() && !elements.isEmpty())
			throw new IllegalArgumentException(
					"The names/guids '" + nameOrGuid + "' do not resolve any state machines");

		for (StateMachine stateMachine : loadedStatemachines) {
			final boolean successfulGeneration = generateStm(stateMachine, outputFolder, verbose);
			if (successfulGeneration) {
				stateMachinesSuccess.add(stateMachine.getName());
			} else {
				stateMachinesFailed.add(stateMachine.getName());
			}
		}
	}

	private boolean generateStm(StateMachine stateMachine, final String outputFolder, final boolean verbose) {
		final Statechart statechart = transform(stateMachine);
		return generate(statechart, outputFolder, verbose);
	}

	private Statechart transform(StateMachine statemachine) {
		final String msg = String.format("Transforming statemachine: %s", statemachine.getName());
		return report(msg, () -> doTransform(statemachine));
	}

	private boolean generate(Statechart statechart, final String outputFolder, boolean verbose) {
		final String msg = String.format("Generating code for statechart: %s", statechart.getName());
		return report(msg, () -> doGenerate(statechart, outputFolder, verbose));
	}

	private boolean doGenerate(Statechart statechart, final String outputFolder, boolean verbose) {
		try {
			if (verbose) {
				statechartApi.saveStatechart(statechart, outputFolder, verbose);
			}
			generator.codeGen(statechart, outputFolder, language);
			return true;
		} catch (Exception e) {
			if (verbose)
				e.printStackTrace(System.out);
			return false;
		}
	}

	private Statechart doTransform(StateMachine statemachine) {
		initialize();
		final Resource res = statechartApi.createResource(statemachine.getName());
		final Statechart sct = trafo.transformStatemachine(statemachine, res);
		if (language == LANG.CPP || language == LANG.C) {
			statechartApi.makeCStatechart(sct);
		} else if (language == LANG.JAVA) {
			statechartApi.makeJavaStatechart(sct);
		}
		return sct;
	}

	private void initialize() {
		trafo = null;
		generator = null;
		final Module transformationModule = targetLanguageSpecificModule(language);
		Guice.createInjector(Modules.combine(transformationModule, new StmCodeGenerator())).injectMembers(this);
	}

	public static Module targetLanguageSpecificModule(LANG language) {
		if (language == LANG.C || language == LANG.CPP)
			return Modules.override(new TransformationModule()).with(new CSTextRuntimeModule());
		else
			return Modules.override(new TransformationModule()).with(new STextRuntimeModule());
	}

	private String getAndValidateOutputFolder(String[] args) {
		if (args != null && args.length > 0 && ExampleCLI.VERBOSE_OUTPUT.equals(args[0])) {
			throw new IllegalArgumentException("argument for output folder missing or at wrong location");
		}
		final String folder = args[0];
		if (!isValidFolderPath(folder))
			throw new IllegalArgumentException(
					"Please make sure that the output folder is a valid folder name: " + folder);
		final File folderFile = new File(folder);
		if (!folderFile.exists()) {
			if (!folderFile.mkdirs())
				throw new RuntimeException("Failed to create folder: " + folder);
			System.out.println("Folder created because it did not yet exist: " + folder);
		}
		return folder;
	}
}
