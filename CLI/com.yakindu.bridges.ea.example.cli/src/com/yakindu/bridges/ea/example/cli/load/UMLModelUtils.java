package com.yakindu.bridges.ea.example.cli.load;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.TimeEvent;

public class UMLModelUtils {

	public static void fixEvents(Resource resource) {
		collect(resource.getAllContents(), Event.class).forEach(event -> {
			event.eSetDeliver(false);
			if (event instanceof TimeEvent timeEvent) {
				/*
				 * Needed for UML2SCT transformation
				 */
				timeEvent.setIsRelative(true);
			} else if (event instanceof SignalEvent signalEvent && signalEvent.getSignal() != null) {
				signalEvent.setName(signalEvent.getSignal().getName());
			} else if (event instanceof CallEvent callEvent && callEvent.getOperation() != null) {
				callEvent.setName(callEvent.getOperation().getName());
			}
		});
	}

	public static void fixEffects(Resource resource) {
		collect(resource.getAllContents(), OpaqueBehavior.class).forEach(behavior -> {
			if (behavior.getBodies().isEmpty() || behavior.getBodies().get(0).isBlank()) {
				behavior.eSetDeliver(false);
				behavior.getBodies().add(behavior.getName());
			}
		});
	}

	private static <T extends NamedElement> List<T> collect(TreeIterator<EObject> iter, Class<T> type) {
		final List<T> result = new ArrayList<>();
		while (iter.hasNext()) {
			final EObject obj = iter.next();
			if (type.isInstance(obj)) {
				result.add(type.cast(obj));
			}
		}
		return result;
	}
}
