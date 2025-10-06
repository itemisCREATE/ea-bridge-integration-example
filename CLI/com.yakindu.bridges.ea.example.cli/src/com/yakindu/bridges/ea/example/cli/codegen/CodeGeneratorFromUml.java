package com.yakindu.bridges.ea.example.cli.codegen;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.StateMachine;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.yakindu.bridges.ea.example.cli.AbstractResourceProcessor;
import com.yakindu.bridges.ea.example.cli.ExampleCLI;
import com.yakindu.bridges.ea.example.cli.codegen.StmCodeGenerator.LANG;
import com.yakindu.bridges.ea.example.cli.codegen.util.StatechartUtil;
import com.yakindu.sct.domain.c.runtime.CSTextRuntimeModule;
import com.yakindu.sct.domain.java.modules.JavaDomainRuntimeModule;
import com.yakindu.sct.model.sgraph.Statechart;
import com.yakindu.sct.model.stext.STextRuntimeModule;
import com.yakindu.sct.uml2.transformation.IStatemachineTransformation;
import com.yakindu.sct.uml2.transformation.module.TransformationModule;

public class CodeGeneratorFromUml extends AbstractResourceProcessor{
	
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
	protected String run(Resource resource, String[] args) throws Exception {
		if (args == null || args[0].isBlank())
			throw new Exception("Mandatory argument <output-folder> is missing.");
		final String outputFolder = getAndValidateOutputFolder(args);
		final String nameOrGuid = getNameOrGuidFromArguments(skip(args, 1));
		final boolean verbose = getVerboseFlagFromArguments(skip(args, 1));

		final Set<String> stateMachinesFailed = new TreeSet<>();
		final Set<String> stateMachinesSuccess = new TreeSet<>();
		genStms(resource, outputFolder, nameOrGuid, verbose, stateMachinesFailed, stateMachinesSuccess);

		if (!stateMachinesFailed.isEmpty()) {
			if (!stateMachinesSuccess.isEmpty()) {
				throw new Exception("Failed to generate for: " + stateMachinesFailed.toString()
						+ "\nSuccessfully generated for: " + stateMachinesSuccess.toString());
			} else {
				throw new Exception("Failed to generate for: " + stateMachinesFailed.toString());
			}
		}
		return stateMachinesSuccess.toString();
	}
	
	protected void genStms(Resource resource, final String outputFolder, final String nameOrGuid, final boolean verbose,
			final Set<String> stateMachinesFailed, final Set<String> stateMachinesSuccess) throws Exception {
		List<StateMachine> loadedStatemachines;
		try {
			loadedStatemachines = loadElements(resource, nameOrGuid, verbose).stream()
			        .map(o -> (StateMachine) o)
			        .toList();
		} catch (Exception e) {
			throw new Exception("Provided element is not a statemachine: " + nameOrGuid);
		}

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
		} else if(language == LANG.JAVA){
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
	
	private Module targetLanguageSpecificModule(LANG language) {
		switch (language) {
			case C:
				return Modules.override(new TransformationModule()).with(new CSTextRuntimeModule());
			case CPP:
				return Modules.override(new TransformationModule()).with(new CSTextRuntimeModule());
			case CSHARP:
				return Modules.override(new TransformationModule()).with(new STextRuntimeModule());
			case JAVA:
				return Modules.override(new TransformationModule()).with(new JavaDomainRuntimeModule());
			case PYTHON:
				return Modules.override(new TransformationModule()).with(new STextRuntimeModule());
			default:
				throw new IllegalArgumentException("Unexpected language: " + language);
		}
	}
	
	private String getAndValidateOutputFolder(String[] args) throws Exception {
		if (args != null && args.length > 0 && ExampleCLI.VERBOSE_OUTPUT.equals(args[0])) {
			throw new Exception("argument for output folder missing or at wrong location");
		}
		final String folder = args[0];
		if (!isValidFolderPath(folder))
			throw new Exception("Please make sure that the output folder is a valid folder name: " + folder);
		final File folderFile = new File(folder);
		if (!folderFile.exists()) {
			if (!folderFile.mkdirs())
				throw new Exception("Failed to create folder: " + folder);
			System.out.println("Folder created because it did not yet exist: " + folder);
		}
		return folder;
	}
}
