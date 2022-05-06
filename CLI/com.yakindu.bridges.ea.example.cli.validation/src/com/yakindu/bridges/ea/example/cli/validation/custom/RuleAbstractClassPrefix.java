package com.yakindu.bridges.ea.example.cli.validation.custom;

import java.util.function.Consumer;

import org.eclipse.uml2.uml.Class;

public class RuleAbstractClassPrefix extends AbstractValidationRule<Class> {

	private static final String AbstractPrefix = "Abstract";

	public RuleAbstractClassPrefix() {
		super(Class.class);
	}

	@Override
	public void validate(Class obj, Consumer<String> addError, Consumer<String> addWarning) {
		if (isAbstractClassWithoutPrefix(obj)) {
			addWarning.accept(
					String.format("Abstract class '%s' should start with '%s'.", obj.getName(), AbstractPrefix));
		}
	}

	private boolean isAbstractClassWithoutPrefix(Class element) {
		return element instanceof Class && element.isAbstract() && !element.getName().startsWith(AbstractPrefix);
	}

}
