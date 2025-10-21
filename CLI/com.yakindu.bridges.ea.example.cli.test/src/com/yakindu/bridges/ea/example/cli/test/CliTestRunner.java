package com.yakindu.bridges.ea.example.cli.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.yakindu.bridges.ea.example.cli.ExampleCLI;

public class CliTestRunner {

	public static String run(Integer expectedExitCode, String... args) {
		return run(() -> {
			final ExampleCLI cli = new ExampleCLI();
			final Object code = cli.start(new ApplicationTestContext(args));
			assertEquals("Unexpected exit code", expectedExitCode, code);
		});
	}

	public static String run(Runnable runnable) {
		// redirect System output (redirect error output if needed, too)
		final PrintStream defaultOutStream = System.out;
		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outputStream));

			runnable.run();
			return outputStream.toString().trim();
		} finally {
			System.setOut(defaultOutStream);
		}
	}
}
