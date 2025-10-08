package com.yakindu.bridges.ea.example.cli.test.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.xtext.diagnostics.Severity;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.yakindu.bridges.ea.core.utils.JoinStr;
import com.yakindu.bridges.ea.example.cli.AbstractResourceProcessor;
import com.yakindu.bridges.ea.example.cli.codegen.CodeGeneratorFromUml;
import com.yakindu.bridges.ea.example.cli.codegen.StmCodeGenerator.LANG;
import com.yakindu.bridges.ea.example.cli.codegen.util.StatechartUtil;
import com.yakindu.bridges.ea.example.cli.validation.SCTValidation;
import com.yakindu.sct.model.sgraph.Statechart;
import com.yakindu.sct.model.stext.resource.StextResource;
import com.yakindu.sct.uml2.transformation.IStatemachineTransformation;

@RunWith(Parameterized.class)
public class CodeGenerationTest {
	
	static final String PLUGIN_ID = "com.yakindu.bridges.ea.example.cli.codegen";
	
	static final URI TEST_MODEL_REFERENCE = URI.createPlatformPluginURI(PLUGIN_ID + "/exampleModels/Library.eap", true);

	@Parameters(name = "{0}")
	public static Iterable<? extends Object> testParameterGenLanguages() {
		// Java is not supported right now
		return List.of(LANG.C, LANG.CPP, LANG.CSHARP, LANG.PYTHON);
	}

	@Parameter(0)
	public LANG language;

	@Rule
	public TemporaryFolder genFolder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	protected CodeGeneratorFromUml codeGen;

	@Inject
	public StatechartUtil statechartUtil;

	protected CodeGeneratorFromUml generator;

	@Inject
	protected IStatemachineTransformation trafo;
	
	protected void initialize() {
		trafo = null;
		codeGen = new CodeGeneratorFromUml(language);
		Guice.createInjector(CodeGeneratorFromUml.targetLanguageSpecificModule(language)).injectMembers(this);
	}
	
	@AfterClass
	public static void closeTestProject() throws CoreException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(CodeGenerationTest.class.getSimpleName());
		if (project != null && project.isOpen()) {
			project.close(null);
		}
	}
	
	@Test
	public void checkGeneratedFilesForReferenceModel_allStatemachines() throws Exception {
		initialize();
		checkGeneratedFilesForModel(TEST_MODEL_REFERENCE, "{49E4B594-F092-4ad7-8B83-BC12FB6B3622}", false,
				false);
	}
	
	private List<Set<String>> checkGeneratedFilesForModel(URI uri, String nameOrGuid, boolean ignoreEffectTexts,
			boolean ignoreCustomErrors) throws Exception {
		final ResourceSetImpl set = new ResourceSetImpl();
		final Resource refModel = set.getResource(uri, true);
		AbstractResourceProcessor.fixModel(refModel);
		final List<String> args = Lists.newArrayList(genFolder.getRoot().getAbsolutePath(), nameOrGuid,
				"-v");
		final List<Set<String>> validatedStatecharts = validateStatechartsForResource(uri, nameOrGuid, false);
		final Set<String> successfullyTransformedMachines = validatedStatecharts.get(0);
		final Set<String> failedToTransformMachines = validatedStatecharts.get(1);
		try {
			codeGen.run(refModel, args.toArray(new String[0]));
		} catch (Exception e) {
			System.out.println(e);
		}
		checkCreatedStatechart(refModel, nameOrGuid,
				successfullyTransformedMachines.size() + failedToTransformMachines.size(), validatedStatecharts);
		return validatedStatecharts;
	}
	
	private void checkCreatedStatechart(Resource resource, String nameOrGuid, int expectedNumberOfStateMachines,
			List<Set<String>> validatedStatecharts) throws Exception {
		final Collection<StateMachine> stateMachines = CodeGeneratorFromUml.loadedStatemachines(resource, nameOrGuid, true);
		assertEquals("Unexpected number of state machines in test model", expectedNumberOfStateMachines,
				stateMachines.size());

		final List<String> issues = new ArrayList<>();
		for (StateMachine stm : stateMachines) {
			final File stateChartFile = new File(genFolder.getRoot().getAbsolutePath() + File.separator + stm.getName()
					+ File.separator + stm.getName() + ".ysc");
			// check statechart file exists
			assertTrue("Statechart file for statemachine: " + stm.getName() + " was not generated",
					stateChartFile.exists());

			// check correct and not correct statechart content
			final String issue = checkCreatedStatechartContent(stateChartFile, validatedStatecharts, stm.getName());
			if (issue != null)
				issues.add(issue);

		}
		assertTrue("Unexpected issues in generated statemachines:\n- " + JoinStr.join("\n- ", issues),
				issues.isEmpty());
	}
	
	private String checkCreatedStatechartContent(File stateChartFile, List<Set<String>> validatedStatecharts,
			String statechartName) {
		final Set<String> successfullyTransformedMachines = validatedStatecharts.get(0);
		final Set<String> failedToTransformMachines = validatedStatecharts.get(1);
		final Resource stextResource = statechartUtil
				.loadResource(URI.createFileURI(stateChartFile.getPath().toString()));

		if (stextResource.getContents().isEmpty())
			return "Failed to create SCT resource for statemachine: " + statechartName;

		final Collection<String> resourceErrors = getErrors((StextResource) stextResource);

		if (successfullyTransformedMachines.contains(statechartName)) {
			if (!resourceErrors.isEmpty())
				return String.format("StateMachine '%s' was successfully validated but contains %d SCT issue(s): %s",
						statechartName, resourceErrors.size(), resourceErrors);

		} else if (failedToTransformMachines.contains(statechartName)) {
			if (resourceErrors.isEmpty())
				return String.format(
						"StateMachine '%s' was not successfully validated but resource does unexpectedly not contain any issues",
						statechartName);
		} else {
			return "Unknown statechart: " + statechartName;
		}
		return null; // no issue
	}
	
	protected List<Set<String>> validateStatechartsForResource(URI uri, String nameOrGuid, boolean expectNoErrors) throws Exception {
		final ResourceSetImpl set = new ResourceSetImpl();
		final Resource res = set.getResource(uri, true);
		AbstractResourceProcessor.fixModel(res);
		final Collection<StateMachine> loadedStatemachines = CodeGeneratorFromUml.loadedStatemachines(res, nameOrGuid, true);
		final Set<String> stateMachinesFailed = new TreeSet<>();
		final Set<String> stateMachinesSuccess = new TreeSet<>();
		for (StateMachine stateMachine : loadedStatemachines) {
			initialize();
			final Resource resource = statechartUtil.createResource(stateMachine.getName());
			final Statechart statechart = trafo.transformStatemachine(stateMachine, resource);
			if (language == LANG.CPP || language == LANG.C) {
				statechartUtil.makeCStatechart(statechart);
			}
			final Collection<String> errors = getErrors((StextResource) statechart.eResource());
			if (errors.isEmpty()) {
				stateMachinesSuccess.add(stateMachine.getName());
			} else if (expectNoErrors) {
				fail("No errors expected but found in: " + stateMachine.getName() + "\n- "
						+ errors.stream().collect(Collectors.joining("\n- ")));
			} else {
				stateMachinesFailed.add(stateMachine.getName());
			}
		}
		return Arrays.asList(stateMachinesSuccess, stateMachinesFailed);
	}
	
	protected Collection<String> getErrors(StextResource statechartResource) {
		SCTValidation validator = new SCTValidation(null);
		validator.initialize();
		final Map<EObject, Map<String, Severity>> issues = validator.getTraceableIssues(statechartResource,
				List.of(Severity.ERROR));
		return issues.values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toSet());
	}
	
}
