package com.yakindu.bridges.ea.example.cli.validation.custom;

import java.util.function.Consumer;

import org.eclipse.uml2.uml.Element;

import com.yakindu.bridges.ea.example.cli.validation.ValidationResult;

public abstract class AbstractValidationRule<T extends Element> {

	public final Class<T> applicableTo;

	protected AbstractValidationRule(Class<T> applicableTo) {
		this.applicableTo = applicableTo;
	}

	protected void validate(Element obj, ValidationResult result) {
		if (obj == null || !applicableTo.isInstance(obj))
			throw new IllegalArgumentException("Parameter has unexpected type: " + obj);
		try {
			validate(applicableTo.cast(obj), //
					e -> result.addCustomError(obj, e), //
					w -> result.addCustomWarning(obj, w));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void validate(T obj, Consumer<String> addError, Consumer<String> addWarning);

}
