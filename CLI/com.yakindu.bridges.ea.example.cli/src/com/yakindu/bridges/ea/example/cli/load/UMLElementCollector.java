package com.yakindu.bridges.ea.example.cli.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.yakindu.bridges.ea.core.EAResource;
import com.yakindu.bridges.ea.core.utils.EAResourceUtils;

public class UMLElementCollector {

	public List<Element> loadElements(Resource resource, String nameOrGuid) throws Exception {
		if (resource == null)
			throw new Exception("Resource is empty");

		final List<Element> elements = new ArrayList<>();
		if (nameOrGuid != null && !nameOrGuid.isEmpty()) {
			String[] namesOrGuids = nameOrGuid.split(",");
			if (EAResource.PATTERN_GUID.matcher(namesOrGuids[0]).matches()) {
				if (!(resource instanceof EAResource))
					throw new Exception("GUIDs are only valid for eap-files");
				elements.addAll(getElementsByGuids(resource, Arrays.asList(namesOrGuids)));
			} else {
				elements.addAll(getElementsByNames(resource, Arrays.asList(namesOrGuids)));
			}
		} else {
			for (EObject obj : resource.getContents()) {
				if (obj instanceof Package)
					elements.addAll(collectElements((Package) obj));
			}
		}

		return elements;
	}

	private Set<Element> getElementsByNames(Resource resource, List<String> names) throws Exception {
		final Map<String, NamedElement> objectsByName = new HashMap<>();
		final TreeIterator<EObject> contents = resource.getAllContents();
		while (contents.hasNext()) {
			final EObject obj = contents.next();
			if ((obj instanceof NamedElement || obj instanceof Package) && !(obj instanceof Comment)) {
				final String objName = ((NamedElement) obj).getName();
				if (names.contains(objName)) {
					if (objectsByName.containsKey(objName)) {
						throw new Exception(
								String.format("Name '%s' is not unique; it resolves at least to:%n- %s%n- %s", objName,
										getText(obj), getText(objectsByName.get(objName))));
					}
					objectsByName.put(objName, (NamedElement) obj);
				} else {
					// check for fully qualified names case-insensitive (issue #40)
					final String fqn = UMLUtil.getQualifiedText(obj);
					if (names.stream().anyMatch(fqn::equalsIgnoreCase)) {
						// fqn should be unique, no need to check for collisions
						objectsByName.put(fqn, (NamedElement) obj);
					}
				}
			}
		}
		if (names.size() > objectsByName.size()) {
			final List<String> unresolvableNames = new ArrayList<>();
			for (String unresolvableName : names) {
				if (!objectsByName.containsKey(unresolvableName))
					unresolvableNames.add(unresolvableName);
			}
			throw new Exception("Could not find names " + unresolvableNames + " in the model");
		}
		final Set<Element> foundElements = new HashSet<>();
		for (NamedElement obj : objectsByName.values()) {
			if (obj instanceof Element) {
				foundElements.add(obj);
			} else if (obj instanceof Package) {
				foundElements.addAll(collectElements((Package) obj));
			} else
				throw new Exception("Gathered objects contains invalid model element");
		}
		return foundElements;
	}

	private Object getText(EObject obj) {
		if (obj == null)
			return "<null>";
		final String clazz = obj.eClass().getName();
		final String fqn = UMLUtil.getQualifiedText(obj);
		final String guid = EAResourceUtils.getGuidForElement(obj);
		return String.format("<%s> %s%s", clazz, fqn, guid == null ? "" : "  GUID = " + guid);
	}

	private Set<Element> collectElements(Package pack) {
		Set<Element> collectedElements = new HashSet<>();
		TreeIterator<EObject> contents = ((EObject) pack).eAllContents();
		while (contents.hasNext()) {
			EObject eObject = contents.next();
			if (eObject instanceof Element) {
				collectedElements.add((Element) eObject);
			}
		}
		return collectedElements;
	}

	private Set<Element> getElementsByGuids(Resource resource, List<String> guids) throws Exception {
		Set<Element> elements = new HashSet<>();
		for (String guid : guids) {
			EObject obj = EAResourceUtils.getElementForGuid(resource, guid);
			if (obj instanceof Package) {
				elements.addAll(collectElements((Package) obj));
			} else if (obj instanceof Element) {
				elements.add((Element) obj);
			} else {
				throw new Exception("GUID " + guid + " cannot be resolved to a package or element but: " + obj);
			}
		}
		return elements;
	}

}
