package com.yakindu.bridges.ea.example.cli.load;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.TimeEvent;

public class UMLModelUtils {

	public static void adjustTimeEventsInModel(Resource resource) {
		final TreeIterator<EObject> iter = resource.getAllContents();
		while (iter.hasNext()) {
			final EObject obj = iter.next();
			if (obj instanceof TimeEvent) {
				/*
				 * Needed for UML2SCT transformation
				 */
				((EObject) obj).eSetDeliver(false);
				((TimeEvent) obj).setIsRelative(true);
			}
		}
	}
}
