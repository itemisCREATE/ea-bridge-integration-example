package com.yakindu.bridges.ea.example.cli;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.yakindu.bridges.ea.example.cli.codegen.CodeGeneratorFromUml;
import com.yakindu.bridges.ea.example.cli.codegen.StmCodeGenerator.LANG;
import com.yakindu.bridges.ea.example.cli.validation.ModelValidator;

public class ExampleCLI implements IApplication {

	public static final Integer EXIT_ERROR = 1;

	public static final String APP_VALIDATE = "validate";

	public static final String APP_GEN = "codegen";

	public static final String VERBOSE_OUTPUT = "-v";

	public static final String HELP = "YAKINDU EA-Bridge Example Command Line Interface\n" //
			+ "Possible options:\n" //
			+ "  " + APP_VALIDATE + " <eap-file> <output-report-file> [<classifier-name/package-name/guid>] [-v]\n" //
			+ "  " + APP_GEN + " <language> <eap-file> <output-folder> <state-machine-name/-guid> [-v]\n" //
			+ "Language options are: \"C\", \"C++\", \"C#\", \"Java\", \"Python\"\n" + VERBOSE_OUTPUT
			+ " enables 'verbose' output" //

	;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		// set up the exit data property in the case of errors
		// c.f. javadoc of IApplicationContext
		System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, "");

		final String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (args != null && args.length >= 3) {
			final String[] appArgs = AbstractResourceProcessor.skip(args, 1);

			final AbstractResourceProcessor processor = getProcessor(args[0], appArgs);
			if (processor != null) {
				try {
					processor.run(processor instanceof CodeGeneratorFromUml ? AbstractResourceProcessor.skip(appArgs, 1)
							: appArgs);
					System.out.println("Validation successfully finished.");
					return IApplication.EXIT_OK;

				} catch (Exception e) {

					// unexpected error - print stack trace!
					System.out.println("Validation failed unexpectedly:");
					e.printStackTrace();
				}
				return EXIT_ERROR;
			}
		}
		printHelp();
		return IApplication.EXIT_OK;
	}

	private AbstractResourceProcessor getProcessor(String cmd, String[] appArgs) throws Exception {
		if (APP_VALIDATE.equalsIgnoreCase(cmd))
			return new ModelValidator();
		if (APP_GEN.equalsIgnoreCase(cmd))
			return new CodeGeneratorFromUml(getTargetLanguage(appArgs[0]));
		return null;
	}

	private LANG getTargetLanguage(String languageInput) throws Exception {
		switch (languageInput) {
		case "C":
			return LANG.C;
		case "C++":
			return LANG.CPP;
		case "C#":
			return LANG.CSHARP;
		case "Java":
			return LANG.JAVA;
		case "Python":
			return LANG.PYTHON;
		default:
			throw new Exception("The provided target language is invalid or not supported!");
		}
	}

	private void printHelp() {
		System.out.println(HELP);
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
