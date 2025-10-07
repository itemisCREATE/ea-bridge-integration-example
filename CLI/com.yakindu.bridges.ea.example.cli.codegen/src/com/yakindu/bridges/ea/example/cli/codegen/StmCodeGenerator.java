package com.yakindu.bridges.ea.example.cli.codegen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.service.AbstractGenericModule;
import org.eclipse.xtext.xbase.lib.Pair;

import com.google.inject.Inject;
import com.yakindu.bridges.ea.example.cli.codegen.builders.GeneratorEntryFactory;
import com.yakindu.sct.model.sgen.GeneratorEntry;
import com.yakindu.sct.model.sgraph.Statechart;

public class StmCodeGenerator extends AbstractGenericModule{
	
	private static final String OPTION_OUTLET = "Outlet";
	private static final String OPTION_TARGET_PROJECT = "targetProject";
	private static final String OPTION_TARGET_FOLDER = "targetFolder";
	
	@Inject
	private GeneratorEntryFactory factory;

	public static enum LANG {
		C, CPP, CSHARP, JAVA, PYTHON
	};
	
	public void codeGen(Statechart statechart, String outputFolder, LANG language) throws Exception {
		if (outputFolder.isBlank())
			throw new Exception("No output folder was given");
		if (statechart == null)
			throw new Exception("Can't perform code generation beacuse resource is empty");

		final Map<String, List<Pair<String, Object>>> genOpt = new HashMap<String, List<Pair<String, Object>>>();

		genOpt.put(OPTION_OUTLET, List.of(Pair.of(OPTION_TARGET_PROJECT, ""), //
				Pair.of(OPTION_TARGET_FOLDER, statechart.getName())));

		generate(statechart, genOpt, outputFolder, language);
	}
	
	private void generate(Statechart statechart, Map<String, List<Pair<String, Object>>> generatorOptions,
			String outputFolder, LANG language) {
		final GeneratorEntry entry = factory.create(generatorOptions, statechart);
		final Generator generator = Generator.forLang(language, outputFolder);
		generator.generate(statechart, entry);
	}
}
