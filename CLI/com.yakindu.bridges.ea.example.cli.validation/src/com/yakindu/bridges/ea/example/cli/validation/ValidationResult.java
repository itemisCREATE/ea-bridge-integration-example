package com.yakindu.bridges.ea.example.cli.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.yakindu.bridges.ea.core.EAModelError;
import com.yakindu.bridges.ea.core.utils.MapUtils;
import com.yakindu.sct.model.stext.resource.StextResource;

public class ValidationResult {

	private static final int DEFAULT_LIST_SIZE = 4;

	private final List<String> resourceWarnings = new ArrayList<>();
	private final List<String> resourceErrors = new ArrayList<>();

	private final Map<EObject, List<String>> loadWarnings = new HashMap<>();
	private final Map<EObject, List<String>> loadErrors = new HashMap<>();

	private final Map<EObject, List<String>> umlWarnings = new HashMap<>();
	private final Map<EObject, List<String>> umlErrors = new HashMap<>();

	private final Map<EObject, List<String>> customWarnings = new HashMap<>();
	private final Map<EObject, List<String>> customErrors = new HashMap<>();
	
	private final Map<EObject, List<String>> sctWarnings = new HashMap<>();
	private final Map<EObject, List<String>> sctErrors = new HashMap<>();

	public ValidationResult(Resource resource) {
		resource.getErrors().forEach(error -> addIssue(loadErrors, error));
		resource.getWarnings().forEach(warning -> addIssue(loadWarnings, warning));
	}

	private void addIssue(Map<EObject, List<String>> issues, Diagnostic issue) {
		if (issue instanceof EAModelError) {
			final EObject element = ((EAModelError) issue).getContextElement();
			if (element == null) {
				if (((EAModelError) issue).getSeverity() == IStatus.ERROR) {
					resourceErrors.add(issue.getMessage());
				} else if (((EAModelError) issue).getSeverity() == IStatus.WARNING) {
					resourceWarnings.add(issue.getMessage());
				}
			} else {
				if (((EAModelError) issue).getSeverity() == IStatus.ERROR) {
					MapUtils.addToListMap(loadErrors, element, DEFAULT_LIST_SIZE, issue.getMessage());
				} else if (((EAModelError) issue).getSeverity() == IStatus.WARNING) {
					MapUtils.addToListMap(loadWarnings, element, DEFAULT_LIST_SIZE, issue.getMessage());
				}
			}
		} else {
			final String msg = String.format("%s while loading '%s' (line %d, column %d): %s", //
					issue.getClass().getSimpleName(), issue.getLocation(), issue.getLine(), issue.getColumn(),
					issue.getMessage());
			resourceErrors.add(msg);
		}
	}

	public void addUmlError(EObject object, String message) {
		MapUtils.addToListMap(umlErrors, object, DEFAULT_LIST_SIZE, message);
	}

	public void addUmlWarning(EObject object, String message) {
		MapUtils.addToListMap(umlWarnings, object, DEFAULT_LIST_SIZE, message);
	}

	public void addCustomError(EObject object, String message) {
		MapUtils.addToListMap(customErrors, object, DEFAULT_LIST_SIZE, message);
	}

	public void addCustomWarning(EObject object, String message) {
		MapUtils.addToListMap(customWarnings, object, DEFAULT_LIST_SIZE, message);
	}
	
	public void addSctError(EObject object, String message, StextResource resource, Function<EObject, EObject> sctToUml) {
		MapUtils.addToListMap(sctErrors, object, DEFAULT_LIST_SIZE, message);
	}
	
	public void addSctWarning(EObject object, String message, StextResource resource,
			Function<EObject, EObject> sctToUml) {
			MapUtils.addToListMap(sctWarnings, object, DEFAULT_LIST_SIZE, message);
	}

	public int count() {
		return resourceWarnings.size() + resourceErrors.size() //
				+ loadWarnings.size() + loadErrors.size() //
				+ umlWarnings.size() + umlErrors.size() //
				+ customWarnings.size() + customErrors.size();
	}

	public List<String> asList() {
		final List<String> list = new ArrayList<>(count());
		resourceWarnings.forEach(issue -> list.add("[W:Resource] " + issue));
		resourceErrors.forEach(issue -> list.add("[E:Resource] " + issue));
		loadWarnings.entrySet().forEach(issues -> list.addAll(asString("[W:Load]", issues)));
		loadErrors.entrySet().forEach(issues -> list.addAll(asString("[E:Load]", issues)));
		umlWarnings.entrySet().forEach(issues -> list.addAll(asString("[W:UML]", issues)));
		umlErrors.entrySet().forEach(issues -> list.addAll(asString("[E:UML]", issues)));
		customWarnings.entrySet().forEach(issues -> list.addAll(asString("[W:Custom]", issues)));
		customErrors.entrySet().forEach(issues -> list.addAll(asString("[E:Custom]", issues)));
		sctWarnings.entrySet().forEach(issues -> list.addAll(asString("[W:SCT]", issues)));
		sctErrors.entrySet().forEach(issues -> list.addAll(asString("[E:SCT]", issues)));
		Collections.sort(list);
		return list;
	}

	private List<String> asString(String prefix, Entry<EObject, List<String>> issues) {
		if (issues == null || issues.getValue().isEmpty())
			return Collections.emptyList();
		final List<String> result = new ArrayList<>(issues.getValue().size());
		final String objName = UMLUtil.getQualifiedText(issues.getKey());
		issues.getValue().forEach(issue -> result.add(String.format("%s '%s': %s", prefix, objName, issue)));
		return result;
	}

	public List<String> getResourceWarnings() {
		return Collections.unmodifiableList(resourceWarnings);
	}

	public List<String> getResourceErrors() {
		return Collections.unmodifiableList(resourceErrors);
	}

	public Map<EObject, List<String>> getLoadWarnings() {
		return Collections.unmodifiableMap(loadWarnings);
	}

	public Map<EObject, List<String>> getLoadErrors() {
		return Collections.unmodifiableMap(loadErrors);
	}

	public Map<EObject, List<String>> getUmlWarnings() {
		return Collections.unmodifiableMap(umlWarnings);
	}

	public Map<EObject, List<String>> getUmlErrors() {
		return Collections.unmodifiableMap(umlErrors);
	}

	public Map<EObject, List<String>> getCustomWarnings() {
		return Collections.unmodifiableMap(customWarnings);
	}

	public Map<EObject, List<String>> getCustomErrors() {
		return Collections.unmodifiableMap(customErrors);
	}
	
	public Map<EObject, List<String>> getSctWarnings() {
		return Collections.unmodifiableMap(sctWarnings);
	}

	public Map<EObject, List<String>> getSctErrors() {
		return Collections.unmodifiableMap(sctErrors);
	}

}
