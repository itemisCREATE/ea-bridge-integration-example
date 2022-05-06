package com.yakindu.bridges.ea.example.cli.test;

import java.util.Collections;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;

public class ApplicationTestContext implements IApplicationContext {

	private String[] args;

	public ApplicationTestContext(String... args) {
		this.args = args;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map getArguments() {
		return Collections.singletonMap(IApplicationContext.APPLICATION_ARGS, args);
	}

	@Override
	public void applicationRunning() {

	}

	@Override
	public String getBrandingApplication() {
		return null;
	}

	@Override
	public String getBrandingName() {
		return null;
	}

	@Override
	public String getBrandingDescription() {
		return null;
	}

	@Override
	public String getBrandingId() {
		return null;
	}

	@Override
	public String getBrandingProperty(String key) {
		return null;
	}

	@Override
	public Bundle getBrandingBundle() {
		return null;
	}

	@Override
	public void setResult(Object result, IApplication application) {

	}

}
