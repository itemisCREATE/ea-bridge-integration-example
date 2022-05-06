package com.yakindu.bridges.ea.example.cli.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
//import org.junit.jupiter.api.Test;
import org.junit.Test;

import com.yakindu.bridges.ea.core.utils.URIToOSPathConverter;
import com.yakindu.bridges.ea.example.cli.ExampleCLI;

public class ExampleCLITest {

	String PLUGIN_ID = "com.yakindu.bridges.ea.example.cli.test";

	URI TEST_MODEL = URI.createPlatformPluginURI(PLUGIN_ID + "/testModels/Example.eap", true);

	@Test
	public void helpOutput() throws Exception {
		final String output = run(IApplication.EXIT_OK);
		assertEquals(ExampleCLI.HELP, output);

	}

	private String run(Integer expectedExitCode, String... args) throws Exception {
		// redirect System output (redirect error output if needed, too)
		final PrintStream defaultOutStream = System.out;
		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outputStream));

			final ExampleCLI cli = new ExampleCLI();
			final Object code = cli.start(new ApplicationTestContext(args));
			assertEquals(expectedExitCode, code);
			return outputStream.toString().trim();
		} finally {
			System.setOut(defaultOutStream);
		}
	}

	@Test
	public void noValidationIssueInRefModel() throws Exception {
		// given
		final String eapFile = URIToOSPathConverter.getFileFromURI(TEST_MODEL);
		final File tmpFile = File.createTempFile(getClass().getName(), ".json");
		tmpFile.deleteOnExit();
		final String elementToValidate = "LibraryModel";
		final String[] args = { ExampleCLI.APP_VALIDATE, eapFile, tmpFile.getAbsolutePath(), elementToValidate,
				ExampleCLI.VERBOSE_OUTPUT };
		// when
		final String output = run(IApplication.EXIT_OK, args);
		// then
		for (String segment : List.of(">> Collecting UML Elements DONE.", ">> Validating DONE.", ">> Writing report")) {
			assertTrue("Missing in console output: \"" + segment + "\"\n\n" + output, output.contains(segment));
		}
	}
}