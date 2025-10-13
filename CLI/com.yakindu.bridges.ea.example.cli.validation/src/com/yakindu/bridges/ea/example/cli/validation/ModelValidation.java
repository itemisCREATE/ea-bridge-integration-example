package com.yakindu.bridges.ea.example.cli.validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.StateMachine;

import com.yakindu.bridges.ea.example.cli.validation.custom.CustomValidation;
import com.yakindu.bridges.ea.example.cli.validation.report.ValidationReport;
import com.yakindu.bridges.ea.example.cli.validation.uml.UMLValidation;

public class ModelValidation {

	private final Resource resource;
	private final List<Element> elements;
	private final Collection<StateMachine> stms;
	private final String reportFile;

	private ValidationResult result;

	public ModelValidation(Resource resource, List<Element> elements, Collection<StateMachine> stms, String reportFile) throws FileNotFoundException {
		this.resource = resource;
		this.stms = stms == null ? Collections.emptyList() : stms;
		if (elements == null) {
			this.elements = new ArrayList<>();
		} else {
			this.elements = elements;
		}
		this.reportFile = reportFile;

		if (resource == null)
			throw new IllegalArgumentException("Resource argument must not be null");

		if (reportFile == null || reportFile.isBlank())
			throw new IllegalArgumentException("Report file argument missing");

		final File file = new File(reportFile);
		if (file.exists() && !file.delete())
			throw new FileNotFoundException("Failed to delete existing report file: " + file.getAbsolutePath());
	}

	public List<String> validate(boolean verbose) {

		// this already collects all load issues
		result = new ValidationResult(resource);

		// add uml issues
		new UMLValidation(resource).validate(result);
		
		// add sct validation issues
		new SCTValidation(stms).validateStatechartsForResource(result, verbose, false);

		// add custom issues
		new CustomValidation(resource, elements).validate(result);

		return result.asList();
	}

	public void createReport(final boolean verbose) {
		if (result == null)
			throw new IllegalStateException("Please call 'validate()' first");

		final String content = new ValidationReport(result).create();

		try (FileOutputStream outputStream = new FileOutputStream(reportFile)) {
			try (final Writer bw = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
				bw.write(content);
			}
		System.out.println("Saved to file: " + reportFile);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to create report", e);
		}
	}

	public ValidationResult getValidationResult() {
		if (result == null)
			throw new IllegalStateException("Please call 'validate()' first");
		return this.result;
	}

}
