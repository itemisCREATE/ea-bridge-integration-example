package com.yakindu.bridges.ea.example.cli.codegen;

import java.io.File;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.itemis.create.statechart.generator.csharp.CsharpCodeGeneratorModule;
import com.yakindu.base.types.typesystem.ITypeValueProvider;
import com.yakindu.bridges.ea.example.cli.codegen.StmCodeGenerator.LANG;
import com.yakindu.sct.domain.c.runtime.CSTextRuntimeModule;
import com.yakindu.sct.domain.java.typesystem.JavaTypeValueProvider;
import com.yakindu.sct.generator.c.CCodeGeneratorModule;
import com.yakindu.sct.generator.c.typesystem.CTypeValueProvider;
import com.yakindu.sct.generator.core.IExecutionFlowGenerator;
import com.yakindu.sct.generator.core.IGeneratorModule;
import com.yakindu.sct.generator.core.artifacts.DefaultGenArtifactConfigurations;
import com.yakindu.sct.generator.core.artifacts.IGenArtifactConfigurations;
import com.yakindu.sct.generator.core.execution.FlowOptimizerFactory;
import com.yakindu.sct.generator.core.extensions.GeneratorExtensions.GeneratorModuleAdapter;
import com.yakindu.sct.generator.core.filesystem.DefaultFileSystemAccessFactory;
import com.yakindu.sct.generator.core.filesystem.ISCTFileSystemAccess;
import com.yakindu.sct.generator.core.filesystem.ProjectBasedFileSystemAccess;
import com.yakindu.sct.generator.cpp11.Cpp11CodeGeneratorModule;
import com.yakindu.sct.generator.java.JavaGeneratorModule;
import com.yakindu.sct.generator.python.PythonCodeGeneratorModule;
import com.yakindu.sct.model.sexec.ExecutionFlow;
import com.yakindu.sct.model.sexec.transformation.FlowOptimizer;
import com.yakindu.sct.model.sexec.transformation.IModelSequencer;
import com.yakindu.sct.model.sgen.GeneratorEntry;
import com.yakindu.sct.model.sgraph.Statechart;
import com.yakindu.sct.model.stext.STextRuntimeModule;

public class Generator {

	@Inject
	protected IExecutionFlowGenerator generator;
	@Inject
	protected IModelSequencer sequencer;
	@Inject
	private FlowOptimizerFactory optimizerFactory;
	@Inject
	protected DefaultFileSystemAccessFactory fsaFactory;

	final private LANG language;
	final private String outputPath;

	public Generator(LANG language, String outputFolder) {
		this.language = language;
		this.outputPath = outputFolder;
	}

	public void generate(Statechart statechart, GeneratorEntry entry) {
		invokeGenerator(statechart, entry);
	}

	public static Generator forLang(LANG language, String outputFolder) {
		final File outFolderFile = new File(outputFolder);
		if (!outFolderFile.isDirectory() && !outFolderFile.exists())
			if (!outFolderFile.mkdirs())
				throw new IllegalArgumentException(
						"Output folder does not exist and cannot be created: " + outputFolder);

		final String absolutOutputFolder = outFolderFile.getAbsolutePath();
		return new Generator(language, absolutOutputFolder);
	}

	private IGeneratorModule createGeneratorModule() {
		switch (language) {
		case C:
			return new CCodeGeneratorModule();
		case CPP:
			return new Cpp11CodeGeneratorModule();
		case CSHARP:
			return new CsharpCodeGeneratorModule();
		case JAVA:
			return new JavaGeneratorModule();
		case PYTHON:
			return new PythonCodeGeneratorModule();
		default:
			throw new IllegalArgumentException("Unexpected language: " + language);
		}
	}

	protected Module getGeneratorModule(GeneratorEntry entry) {
		final IGeneratorModule module = createGeneratorModule();
		return Modules.override(new GeneratorModuleAdapter(module, entry)).with(new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(ISCTFileSystemAccess.class).to(ProjectBasedFileSystemAccess.class);
				binder.bind(String.class).annotatedWith(Names.named(ProjectBasedFileSystemAccess.BASE_DIR))
						.toInstance(outputPath);
				binder.bind(IGenArtifactConfigurations.class).to(DefaultGenArtifactConfigurations.class);
				targetLanguageDependantBinding(binder);
			}
		});
	}

	private void targetLanguageDependantBinding(Binder binder) {
		switch (language) {
		case C:
			binder.bind(LANG.class).annotatedWith(Names.named("Language")).toInstance(LANG.C);
			binder.bind(ITypeValueProvider.class).to(CTypeValueProvider.class);
			break;
		case CPP:
			binder.bind(LANG.class).annotatedWith(Names.named("Language")).toInstance(LANG.CPP);
			binder.bind(ITypeValueProvider.class).to(CTypeValueProvider.class);
			break;
		case CSHARP:
			binder.bind(LANG.class).annotatedWith(Names.named("Language")).toInstance(LANG.CSHARP);
			break;
		case JAVA:
			binder.bind(LANG.class).annotatedWith(Names.named("Language")).toInstance(LANG.JAVA);
			binder.bind(ITypeValueProvider.class).to(JavaTypeValueProvider.class);
			break;
		case PYTHON:
			binder.bind(LANG.class).annotatedWith(Names.named("Language")).toInstance(LANG.PYTHON);
			break;
		default:
			throw new IllegalArgumentException("Unexpected language: " + language);
		}
	}

	protected void invokeGenerator(Statechart statechart, GeneratorEntry entry) {
		System.out.println(String.format("Generating %s to folder %s\\%s ...", statechart.getName(), outputPath,
				statechart.getName()));
		//TODO: https://github.com/itemisCREATE/ea-bridge-integration-example/issues/11
//		if (language == LANG.CPP || language == LANG.C) {
//			Guice.createInjector(Modules.override(new CSTextRuntimeModule()).with(getGeneratorModule(entry)))
//					.injectMembers(this);
//		} else {
			Guice.createInjector(Modules.override(new STextRuntimeModule()).with(getGeneratorModule(entry)))
					.injectMembers(this);
//		}
		final ExecutionFlow flow = createExecutionFlow(statechart, entry);
		generator.generate(flow, entry, fsaFactory.create(entry));
	}

	protected ExecutionFlow createExecutionFlow(Statechart statechart, GeneratorEntry entry) {
		final ExecutionFlow flow = sequencer.transform(statechart);
		final FlowOptimizer optimizer = optimizerFactory.create(entry);
		return optimizer.optimize(flow);
	}

}
