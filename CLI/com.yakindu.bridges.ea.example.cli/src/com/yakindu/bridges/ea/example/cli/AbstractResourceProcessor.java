package com.yakindu.bridges.ea.example.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.yakindu.bridges.ea.core.EAResource;
import com.yakindu.bridges.ea.example.cli.load.UMLModelUtils;

public abstract class AbstractResourceProcessor {

	private static final String VALID_PATH_SEGMENT = "[0-9a-zA-Z._$-]+";

	private static final Pattern PATTERN_VALID_PATH_WINDOWS = Pattern.compile(
			String.format("([a-zA-Z]:%s|%1$s%1$s?)?%s(%1$s%2$s)*%1$s?", "[\\\\/]", VALID_PATH_SEGMENT + "(~\\d)?"));

	private static final Pattern PATTERN_VALID_PATH_LINUX = Pattern
			.compile("/?" + VALID_PATH_SEGMENT + "(/" + VALID_PATH_SEGMENT + ")*/?");

	public void run(String[] args) {
		Resource resource = null;
		try {
			resource = report("Loading '" + args[0] + "'", () -> {
				try {
					return load(args[0]);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			});
			run(resource, skip(args, 1));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resource != null) {
				try {
					resource.unload();
				} catch (Exception e) {
					// silent fail
				}
			}
		}
	}

	protected abstract String run(Resource resource, String[] args);

	protected void report(String task, Runnable runnable) {
		report(task, () -> {
			runnable.run();
			return null;
		});
	}

	protected <T> T report(String task, Supplier<T> runnable) {
		System.out.println(">> " + task + " ...");
		final long start = System.currentTimeMillis();
		try {
			final T result = runnable.get();
			final long duration = System.currentTimeMillis() - start;
			System.out.println(">> " + task + " DONE.  [ " + getHumanReadableDuration(duration) + " ]");
			return result;
		} catch (Exception e) {
			final long duration = System.currentTimeMillis() - start;
			System.out.println(">> " + task + " FAILED.  [ " + getHumanReadableDuration(duration) + " ]");
			throw e;
		}
	}

	private static String getHumanReadableDuration(long durationInMiliseconds) {
		if (durationInMiliseconds < 2000)
			return durationInMiliseconds + "ms";
		if (durationInMiliseconds < 100 * 1000)
			return (durationInMiliseconds / 1000) + "sec";
		return (durationInMiliseconds / 1000 / 60) + "min";
	}

	private Resource load(String filePath) throws Exception {
		if (filePath == null || filePath.isEmpty())
			throw new IllegalArgumentException("File argument missing");
		final File file = new File(filePath);
		if (!file.exists())
			throw new FileNotFoundException("File not found: " + filePath);

		final URI uri = URI.createFileURI(file.getAbsolutePath());
		final ResourceSetImpl set = new ResourceSetImpl();

		if (EAResource.FILE_EXTENSIONS.stream().anyMatch(ext -> filePath.toLowerCase().endsWith(ext))) {
			final Map<Object, Object> loadOptions = set.getLoadOptions();
			loadOptions.put(EAResource.OPTION_READONLY, true);
			loadOptions.put(EAResource.OPTION_REPORT_TO_ERROR_LOG, false);
			loadOptions.put(EAResource.OPTION_REPORT_AS_RESOURCE_MARKERS, false);
			checkLInfo();
		}
		final Resource res = set.getResource(uri, true);
		UMLModelUtils.adjustTimeEventsInModel(res);
		UMLModelUtils.fixEffects(res);
		return res;
	}

	private void checkLInfo() throws Exception {
		@SuppressWarnings("restriction")
		final String msg = com.yakindu.bridges.ea.core.internal.utils.LInfo.isV();
		if (msg != null)
			throw new Exception(msg);
	}

	protected String getNameOrGuidFromArguments(String[] args) {
		for (String arg : args) {
			if (!ExampleCLI.VERBOSE_OUTPUT.equals(arg)) {
				return arg;
			}
		}
		return "";
	}

	protected boolean getVerboseFlagFromArguments(String[] args) {
		for (String arg : args) {
			if (ExampleCLI.VERBOSE_OUTPUT.equals(arg)) {
				return true;
			}
		}
		return false;
	}

	protected static String[] skip(String[] args, int index) {
		final String[] newArgs = new String[args.length - 1];
		System.arraycopy(args, index, newArgs, 0, args.length - 1);
		return newArgs;
	}

	public static boolean isValidFolderPath(final String path) {
		if (path == null || path.trim().isEmpty())
			return false;
		final Pattern pattern = isWindows() ? PATTERN_VALID_PATH_WINDOWS : PATTERN_VALID_PATH_LINUX;
		return pattern.matcher(path).matches();
	}

	private static boolean isWindows() {
		final String os = System.getProperty("os.name") == null ? "" : System.getProperty("os.name");
		return os.contains("Windows");
	}
}
