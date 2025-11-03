package com.yakindu.bridges.ea.example.cli.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.yakindu.bridges.ea.core.utils.EAResourceUtils;

public class ModelError implements Diagnostic {

	private final EObject element;
	private final String message;
	private final String guid;
	private final String eaObjectType;

	public static ModelError report(Element element, String issue) {
		final String eaObjectType = EAResourceUtils.getObjectTypeForElement(element);
		final String guid = EAResourceUtils.getGuidForElement(element);
		final ModelError error = new ModelError(element, issue, guid, eaObjectType);
		element.eResource().getErrors().add(error);
		return error;
	}

	public ModelError(EObject element, String message, String guid, String eaObjectType) {
		this.element = element;
		this.message = message;
		this.guid = guid;
		this.eaObjectType = eaObjectType;
	}

	public EObject getElement() {
		return element;
	}

	public String getEAObjectType() {
		return eaObjectType;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getLocation() {
		return guid;
	}

	@Override
	public int getLine() {
		return 0;
	}

	@Override
	public int getColumn() {
		return 0;
	}

	@Override
	public String toString() {
		return "Model Error [element='" + (element == null ? null : UMLUtil.getQualifiedText(element))
				+ "', message='" + message + "', guid=" + guid + "]";
	}
}
