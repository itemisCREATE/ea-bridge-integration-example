package com.yakindu.bridges.ea.example.cli.validation.uml;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;

public class UMLNameBeautifier {

	private static final String ELEMENT_BASE = "org\\.eclipse\\.uml2\\.uml\\.internal\\.impl\\.(\\w+)Impl@[0-9a-f]+";
	private static final String ATTRIBUTES = "( \\([^:]+: [^\\)]*\\))*";
	private static final String ID = "\\{[^\\{\\}]+#([^\\}]+)\\}";
	private static final Pattern UML_ELEMENT_ATTR_PATTERN = Pattern.compile(ELEMENT_BASE + ATTRIBUTES);
	private static final Pattern UML_ELEMENT_ID_PATTERN = Pattern.compile(ELEMENT_BASE + ID);

	private final Resource resource;

	private Map<String, Map<String, Element>> elementToStringCacheByMetaClass = new HashMap<>();

	public UMLNameBeautifier(Resource resource) {
		this.resource = resource;
	}

	public String format(String message) {
		if (message == null || message.isBlank())
			return message;

		final Map<String, String> replacements = getReplacements(message);
		return formatMessage(message, replacements);
	}

	private String formatMessage(String message, final Map<String, String> replacements) {
		if (replacements.isEmpty())
			return message;

		String newMessage = message;
		for (Entry<String, String> replacement : replacements.entrySet()) {
			newMessage = newMessage.replace(replacement.getKey(), replacement.getValue());
		}
		return newMessage;
	}

	private Map<String, String> getReplacements(String message) {
		final Map<String, String> replacements = getAttributeReplacements(message);
		replacements.putAll(getIdReplacements(message));
		return replacements;
	}

	private Map<String, String> getIdReplacements(String message) {
		final Map<String, String> replacements = new HashMap<>();
		final Matcher matcher = UML_ELEMENT_ID_PATTERN.matcher(message);
		while (matcher.find()) {
			final String stringRepr = matcher.group();
			final String type = matcher.group(1);
			final String fragment = matcher.group(2);

			final EObject element = resource.getEObject(fragment);
			if (element != null) {
				replacements.put(stringRepr, String.format("<%s> %s", type, getPackageQualifiedName(element)));
			}
		}
		return replacements;
	}

	private Map<String, String> getAttributeReplacements(String message) {
		final Map<String, String> replacements = new HashMap<>();
		final Matcher matcher = UML_ELEMENT_ATTR_PATTERN.matcher(message);
		while (matcher.find()) {
			final String stringRepr = matcher.group();
			final String type = matcher.group(1);

			final Map<String, Element> elementsByStringRepr = getUmlElementsByStringRepr(type);
			final Element element = elementsByStringRepr.get(stringRepr);
			if (element != null) {
				replacements.put(stringRepr, String.format("<%s> %s", type, getPackageQualifiedName(element)));
			}
		}
		return replacements;
	}

	private Map<String, Element> getUmlElementsByStringRepr(String type) {
		Map<String, Element> allElementsOfType = elementToStringCacheByMetaClass.get(type);
		if (allElementsOfType == null) {
			allElementsOfType = new HashMap<>();
			for (TreeIterator<EObject> iter = resource.getAllContents(); iter.hasNext();) {
				final EObject obj = iter.next();
				if (obj instanceof Element && type.equals(obj.eClass().getName())) {
					allElementsOfType.put(obj.toString(), (Element) obj);
				}
			}
			elementToStringCacheByMetaClass.put(type, allElementsOfType);
		}
		return allElementsOfType;
	}

	private static String getPackageQualifiedName(EObject object) {
		if (object == null)
			return "[no parent]";

		final String prefix = object instanceof Package ? ""
				: getPackageQualifiedName(object.eContainer()) + Namespace.SEPARATOR;
		final String name = object instanceof NamedElement ? ((NamedElement) object).getName()
				: object instanceof ENamedElement ? ((ENamedElement) object).getName() : null;
		return prefix + (name != null && !name.isEmpty() ? name : "[unnamed " + object.eClass().getName() + "]");
	}
}
