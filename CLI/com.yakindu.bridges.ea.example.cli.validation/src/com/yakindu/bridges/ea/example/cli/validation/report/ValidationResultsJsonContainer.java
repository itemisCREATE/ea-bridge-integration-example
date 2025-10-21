package com.yakindu.bridges.ea.example.cli.validation.report;

import java.util.List;

public class ValidationResultsJsonContainer {

	private final List<ValidationIssueJsonContainer> resourceIssues;
	private final List<ValidationIssueJsonContainer> loadIssues;
	private final List<ValidationIssueJsonContainer> umlIssues;
	private final List<ValidationIssueJsonContainer> sctIssues;
	private final List<ValidationIssueJsonContainer> customIssues;

	public ValidationResultsJsonContainer(List<ValidationIssueJsonContainer> resourceIssues,
			List<ValidationIssueJsonContainer> loadIssues, List<ValidationIssueJsonContainer> umlIssues,
			 List<ValidationIssueJsonContainer> sctIssues, List<ValidationIssueJsonContainer> customIssues) {
		super();
		this.resourceIssues = resourceIssues;
		this.loadIssues = loadIssues;
		this.umlIssues = umlIssues;
		this.sctIssues = sctIssues;
		this.customIssues = customIssues;
	}

	public List<ValidationIssueJsonContainer> getResourceIssues() {
		return resourceIssues;
	}

	public List<ValidationIssueJsonContainer> getLoadIssues() {
		return loadIssues;
	}

	public List<ValidationIssueJsonContainer> getUmlIssues() {
		return umlIssues;
	}
	
	public List<ValidationIssueJsonContainer> getSctIssues() {
		return sctIssues;
	}

	public List<ValidationIssueJsonContainer> getCustomIssues() {
		return customIssues;
	}

	public int count() {
		return resourceIssues.size() + loadIssues.size() + umlIssues.size() + sctIssues.size() + customIssues.size();
	}

}