package com.yakindu.bridges.ea.example.cli.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.junit.Test;

import com.yakindu.bridges.ea.core.utils.JoinStr;
import com.yakindu.bridges.ea.core.utils.URIToOSPathConverter;
import com.yakindu.bridges.ea.example.cli.ExampleCLI;

public class ExampleCLITest {

	private static final String PLUGIN_ID = "com.yakindu.bridges.ea.example.cli.test";

	private static final URI TEST_MODEL = URI.createPlatformPluginURI(PLUGIN_ID + "/testModels/Example.eap", true);

	@Test
	public void helpOutput() throws Exception {
		final String output = CliTestRunner.run(IApplication.EXIT_OK);
		assertEquals(ExampleCLI.HELP, output);
	}

	@Test
	public void validation_LightSwitchModel() throws Exception {
		assertIssuesInModel("LightSwitchStateMachine",
				"[W:SCT] 'ExampleModel::LightSwitchStateMachine::LightSwitch::Switch::{region [0] Region}::{transition [1] Transition}': Out event 'powerButtonPressed' is never raised in this statechart. The transition trigger is therefore never active.",
				"[W:SCT] 'ExampleModel::LightSwitchStateMachine::LightSwitch::Switch::{region [0] Region}::{transition [2] Transition}': Out event 'dimmerButtonPressed' is never raised in this statechart. The transition trigger is therefore never active.");
	}

	@Test
	public void validation_LibraryModel() throws Exception {
		assertIssuesInModel("{E7FBC00B-5294-4dd0-AC3D-ABC22537573D}", // package "Library"
				"[W:Custom] 'ExampleModel::Library::Media': Abstract class 'Media' should start with 'Abstract'.");
	}

	@Test
	public void validation_StateMachineWithIssues() throws Exception {
		assertIssuesInModel("StateMachineWithIssues",
				"[E:SCT] 'ExampleModel::StateMachineWithIssues::FaultyStateMachine::{region [0] Region}::Unreachable': Node is not reachable.",
				"[W:SCT] 'ExampleModel::StateMachineWithIssues::FaultyStateMachine::{region [0] Region}::{transition [2] Transition}': Dead transition from state 'SomeState'. This transition is never taken due to the precedence of completion transition.");
	}

	private void assertNoIssueInModel(String elementToValidate) throws Exception {
		assertIssuesInModel(elementToValidate); // no expected issues
	}

	private List<String> assertIssuesInModel(String elementToValidate, String... expectedIssues) throws Exception {
		// given
		final String eapFile = URIToOSPathConverter.getFileFromURI(TEST_MODEL);
		final File tmpFile = File.createTempFile(getClass().getName(), ".json");
		tmpFile.deleteOnExit();
		final String[] args = { ExampleCLI.APP_VALIDATE, eapFile, tmpFile.getAbsolutePath(), elementToValidate,
				ExampleCLI.VERBOSE_OUTPUT };

		// when
		final String output = CliTestRunner.run(IApplication.EXIT_OK, args);
		// System.out.println("\n\n" + output); // useful for debugging

		// then
		for (String segment : List.of(">> Loading '", ">> Collecting UML Elements DONE.", ">> Validating DONE.",
				"CLI successfully finished.")) {
			assertTrue("Missing in console output: \"" + segment + "\"\n\n" + output, output.contains(segment));
		}

		if (expectedIssues != null && expectedIssues.length > 0) {
			final List<String> missingIssues = new ArrayList<>(List.of(expectedIssues));
			missingIssues.removeIf(issue -> output.contains(issue));
			assertTrue(
					missingIssues.size() + " expected issue(s) not reported:\n- " + JoinStr.join("\n- ", missingIssues),
					missingIssues.isEmpty());

			assertTrue("Unexpected number of issues written to report:\n" + output,
					output.contains(">> Writing report (" + expectedIssues.length + " issue"));
		} else {
			assertFalse("No issues expected but validation report was created\n" + output,
					output.contains(">> Writing report ("));
		}
		return null;
	}
}