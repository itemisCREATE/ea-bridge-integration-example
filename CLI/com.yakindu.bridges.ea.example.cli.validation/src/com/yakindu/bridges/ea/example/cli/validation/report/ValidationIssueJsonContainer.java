package com.yakindu.bridges.ea.example.cli.validation.report;

public class ValidationIssueJsonContainer {

	private final String severity;
	private final String type;
	private final String id;
	private final String msg;

	public ValidationIssueJsonContainer(String severity, String type, String id, String msg) {
		super();
		this.severity = severity;
		this.type = type;
		this.id = id;
		this.msg = msg;
	}

	public String getSeverity() {
		return severity;
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public String getMsg() {
		return msg;
	}

}
