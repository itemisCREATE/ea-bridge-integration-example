package com.yakindu.bridges.ea.example.cli.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.yakindu.bridges.ea.core.EAResource;
import com.yakindu.bridges.ea.core.utils.EAResourceUtils;

public class UMLElementCollector {

	/**
	 * From the given <code>resource</code>, load all elements that are given by the comma-separated list of names
	 * and/or guids.
	 * 
	 * @param resource   A UML/EA resource
	 * @param nameOrGuid a comma-separated list of names and/or guids
	 * @param verbose    whether or not console output should be printed about the found elements
	 * @return a list of UML elements matching the given names/guids
	 * @throws IllegalArgumentException in case the given names/guids cannot be found
	 */
	public static List<Element> loadElements(Resource resource, String nameOrGuid, boolean verbose) {
		final List<Element> elements = loadElements(resource, nameOrGuid);

		if (verbose) {
			if (elements.isEmpty()) {
				System.out.println("No UML elements loaded");
			} else {
				System.out.println(String.format("%d element%s found: %s", //
						elements.size(), elements.size() == 1 ? "" : "s",
						elements.stream().filter(element -> element instanceof NamedElement)
								.map(e -> ((NamedElement) e).getName()).filter(name -> name != null).sorted()
								.collect(Collectors.joining(", "))));
			}
		}
		return elements;
	}

	private static List<Element> loadElements(Resource resource, String nameOrGuid) {
		if (resource == null)
			throw new NullPointerException("Resource is null");

		final List<Element> elements = new ArrayList<>();
		if (nameOrGuid != null && !nameOrGuid.isEmpty()) {
			String[] namesOrGuids = nameOrGuid.split(",");
			if (EAResource.PATTERN_GUID.matcher(namesOrGuids[0]).matches()) {
				if (!(resource instanceof EAResource))
					throw new IllegalArgumentException("GUIDs are only valid for eap-files");
				elements.addAll(getElementsByGuids(resource, Arrays.asList(namesOrGuids)));
			} else {
				elements.addAll(getElementsByNames(resource, Arrays.asList(namesOrGuids)));
			}
		} else {
			for (EObject obj : resource.getContents()) {
				if (obj instanceof Package pack)
					elements.add(pack);
			}
		}

		return elements;
	}

	private static Collection<PackageableElement> getElementsByNames(Resource resource, List<String> names) {
		final Map<String, PackageableElement> objectsByName = new HashMap<>();
		final TreeIterator<EObject> contents = resource.getAllContents();
		while (contents.hasNext()) {
			final EObject obj = contents.next();
			if (obj instanceof PackageableElement element) {
				final String objName = element.getName();
				if (names.contains(objName)) {
					if (objectsByName.containsKey(objName)) {
						throw new IllegalArgumentException(
								String.format("Name '%s' is not unique; it resolves at least to:%n- %s%n- %s", objName,
										getText(obj), getText(objectsByName.get(objName))));
					}
					objectsByName.put(objName, element);
				} else {
					// check for fully qualified names case-insensitive
					final String fqn = UMLUtil.getQualifiedText(obj);
					if (names.stream().anyMatch(fqn::equalsIgnoreCase)) {
						// fqn should be unique, no need to check for collisions
						objectsByName.put(fqn, element);
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
			throw new IllegalArgumentException("Could not find names " + unresolvableNames + " in the model");
		}
		return objectsByName.values();
	}

	private static Object getText(EObject obj) {
		if (obj == null)
			return "<null>";
		final String clazz = obj.eClass().getName();
		final String fqn = UMLUtil.getQualifiedText(obj);
		final String guid = EAResourceUtils.getGuidForElement(obj);
		return String.format("<%s> %s%s", clazz, fqn, guid == null ? "" : "  GUID = " + guid);
	}

	private static Set<Element> getElementsByGuids(Resource resource, List<String> guids) {
		final Set<Element> elements = new HashSet<>();
		for (String guid : guids) {
			EObject obj = EAResourceUtils.getElementForGuid(resource, guid);
			if (obj instanceof PackageableElement element) {
				elements.add(element);
			} else if (obj != null) {
				throw new IllegalArgumentException(
						"Unsupported model element class for guid '" + guid + "': " + obj.eClass().getName());
			} else {
				throw new IllegalArgumentException("Could not find model element for guid '" + guid + "'");
			}
		}
		return elements;
	}

	public static Collection<StateMachine> collectStatemachines(Collection<Element> elements) {
		final Set<StateMachine> stms = new HashSet<>();
		final List<Element> nonStms = new ArrayList<>();
		for (Element element : elements) {
			if (element instanceof StateMachine stm) {
				stms.add(stm);
			} else if (element instanceof PackageableElement) {
				nonStms.add(element);
			}
		}

		for (Element element : nonStms) {
			final TreeIterator<EObject> iter = element.eAllContents();
			while (iter.hasNext()) {
				final EObject obj = iter.next();
				if (obj instanceof StateMachine stm) {
					stms.add(stm);
				} else if (!(obj instanceof PackageableElement)) {
					iter.prune(); // skip sub-trees of non-package elements
				}
			}
		}
		return stms;
	}
}
