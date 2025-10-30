package com.yakindu.bridges.ea.example.cli.load;

import static org.eclipse.uml2.uml.UMLPackage.Literals.CALL_OPERATION_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.Literals.SEND_SIGNAL_ACTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.TimeEvent;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.yakindu.bridges.ea.example.cli.validation.ModelError;

public class UMLModelUtils {

	public static void fixModelForStateMachines(Resource resource) {
		fixEvents(resource);
		convertEffects(resource);
	}

	private static void fixEvents(Resource resource) {
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

//	private static void fixEffects(Resource resource) {
//		collect(resource.getAllContents(), OpaqueBehavior.class).forEach(behavior -> {
//			if (behavior.getBodies().isEmpty() || behavior.getBodies().get(0).isBlank()) {
//				behavior.eSetDeliver(false);
//				behavior.getBodies().add(behavior.getName());
//			}
//		});
//	}

	private static void convertEffects(Resource resource) {
		/*-
		 * when simply typing in signal names and operation names as effects, we should easily be able to find those 
		 * in the model and replace them with activities containing send-signal-actions and call-operation-actions.
		 * 
		 * prerequisites:
		 *  - signals must be uniquely named within the entire model
		 *  - operation names must be uniquely named within a state machine (UML should already check this!)
		 *  - operation names must not collide with signal names
		 */
		final Map<String, Signal> allSignals = collectSignals(resource);
		final List<StateMachine> stms = collect(resource.getAllContents(), StateMachine.class);

		stms.forEach(stm -> convertEffects(stm, allSignals));
	}

	private static void convertEffects(StateMachine stm, Map<String, Signal> allSignals) {
		stm.getOwnedOperations().stream().filter(op -> allSignals.containsKey(op.getName())).forEach(op -> {
			final String msg = "Name clash with signal: " + UMLUtil.getQualifiedText(allSignals.get(op.getName()));
			stm.eResource().getErrors().add(ModelError.report(op, msg));
		});

		final List<OpaqueBehavior> behaviors = collect(stm.eAllContents(), OpaqueBehavior.class);
		behaviors.forEach(beh -> convertEffect(beh, stm.getOwnedOperations(), allSignals));
	}

	private static void convertEffect(OpaqueBehavior behavior, List<Operation> operations,
			Map<String, Signal> allSignals) {
		final List<String> effects = behavior.getBodies().stream().map(s -> s.split("\\R")).flatMap(Arrays::stream)
				.filter(s -> !s.isBlank()).map(String::trim).collect(Collectors.toList());
		if (effects.isEmpty())
			return;

		final Activity activity = UMLFactory.eINSTANCE.createActivity();
		for (String effect : effects) {
			final Signal signal = allSignals.get(effect);
			if (signal != null) {
				((SendSignalAction) activity.createOwnedNode(signal.getName(), SEND_SIGNAL_ACTION)).setSignal(signal);
			} else {
				final Operation operation = operations.stream()
						.filter(op -> effect.equals(op.getName()) || effect.equals(op.getName() + "()")).findFirst()
						.orElse(null);
				if (operation != null) {
					((CallOperationAction) activity.createOwnedNode(operation.getName(), CALL_OPERATION_ACTION))
							.setOperation(operation);
				}
			}
		}
		activity.getNodes().forEach(node -> node.setActivity(activity));
//		activity.getNodes().forEach(node -> createTargetInputPin(node)); // hopefully not needed

		if (!activity.getNodes().isEmpty()) {
			behavior.eContainer().eSetDeliver(false);
			EcoreUtil.replace(behavior, activity);
		}
	}

	public static Map<String, Signal> collectSignals(Resource resource) {
		final List<Signal> signals = collect(resource.getAllContents(), Signal.class);

		final Map<String, Signal> signalsByName = new HashMap<>();
		for (Signal signal : signals) {
			final Signal duplicate = signalsByName.put(signal.getName(), signal);
			if (duplicate != null) {
				final String msg = "Name clash with other signal: " + UMLUtil.getQualifiedText(duplicate);
				resource.getErrors().add(ModelError.report(duplicate, msg));
			}
		}
		return signalsByName;
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
