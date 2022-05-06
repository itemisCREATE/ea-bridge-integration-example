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
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;

import com.yakindu.bridges.ea.example.cli.validation.custom.CustomValidation;
import com.yakindu.bridges.ea.example.cli.validation.report.ValidationReport;
import com.yakindu.bridges.ea.example.cli.validation.uml.UMLValidation;

public class ModelValidation {

	private final Resource resource;
	private final List<Element> elements;
	private final String reportFile;

	private ValidationResult result;

	public ModelValidation(Resource resource, List<Element> elements, String reportFile) throws FileNotFoundException {
		this.resource = resource;
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

	public List<String> validate() {

		// this already collects all load issues
		result = new ValidationResult(resource);

		// add uml issues
		UMLValidation.validate(resource, result);

		// add custom issues
		new CustomValidation(resource, elements).validate(result);

		return result.asList();
	}

	public void createReport() {
		if (result == null)
			throw new IllegalStateException("Please call 'validate()' first");

		final String content = new ValidationReport(result).create();

		try (FileOutputStream outputStream = new FileOutputStream(reportFile)) {
			try (final Writer bw = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
				bw.write(content);
			}
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
