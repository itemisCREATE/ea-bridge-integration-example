package com.yakindu.bridges.ea.example.cli.load;

import static org.eclipse.uml2.uml.UMLPackage.Literals.CALL_OPERATION_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.Literals.SEND_SIGNAL_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.Literals.STATE_MACHINE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.yakindu.bridges.ea.core.jdbc.IDatabaseAccess;
import com.yakindu.bridges.ea.example.cli.validation.ModelError;
import com.yakindu.bridges.ea.uml.contributors.IContext;
import com.yakindu.bridges.ea.uml.contributors.IElementContributor;

public class EAEffectContributor implements IElementContributor {

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public String getCategoryLabel() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isDefaultEnabled() {
		return true;
	}

	@Override
	public ElementContribution registerForType() {
		return new ElementContribution(STATE_MACHINE, 0);
	}

	@Override
	public List<EAElement> load(List<EObject> elements, IContext context) {
		/*-
		 * when simply typing in signal names and operation names as effects, we should easily be able to find those 
		 * in the model and replace them with activities containing send-signal-actions and call-operation-actions.
		 * 
		 * prerequisites:
		 *  - signals must be uniquely named within the entire model
		 *  - operation names must be uniquely named within a state machine (UML should already check this!)
		 *  - operation names must not collide with signal names
		 */
		final Map<String, Signal> allSignals = collectSignals(context.getEAResource());
		elements.forEach(stm -> convertEffects((StateMachine) stm, allSignals));
		fixEffects(context.getEAResource());

		return null;
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
		if (operations.isEmpty() && allSignals.isEmpty())
			return;

		final List<String> effects = getEffectsFromOpaqueBehavior(behavior);
		if (effects.isEmpty())
			return;

		final Map<String, Operation> operationsByName = operations.stream()
				.collect(Collectors.toMap(NamedElement::getName, Function.identity(), (o1, o2) -> o1));
		final Pattern operationPattern = operations.isEmpty() ? null
				: Pattern.compile(
						operationsByName.keySet().stream().sorted()
								.collect(Collectors.joining("\\E|\\Q", "(?:call\\w+)(\\Q", "\\E)(?:\\(.+\\))?")),
						Pattern.CASE_INSENSITIVE);

		final Activity activity = UMLFactory.eINSTANCE.createActivity();
		for (String effect : effects) {

			createAction(activity, effect, allSignals, operationsByName, operationPattern);
		}
		activity.getNodes().forEach(node -> node.setActivity(activity));

		if (!activity.getNodes().isEmpty()) {
			behavior.eContainer().eSetDeliver(false);
			EcoreUtil.replace(behavior, activity);
		}
	}

	private static List<String> getEffectsFromOpaqueBehavior(OpaqueBehavior behavior) {
		if (behavior.getBodies().isEmpty())
			return List.of(behavior.getName());
		return behavior.getBodies().stream().map(s -> s.split("\\R")).flatMap(Arrays::stream).filter(s -> !s.isBlank())
				.collect(Collectors.toList());
	}

	private static void createAction(final Activity activity, String effect, Map<String, Signal> allSignals,
			final Map<String, Operation> operationsByName, final Pattern operationPattern) {
		final String signalName = extractSignalNameFromEffect(effect);
		final Signal signal = allSignals.get(signalName);
		if (signal != null) {

			final SendSignalAction action = (SendSignalAction) activity.createOwnedNode(signal.getName(),
					SEND_SIGNAL_ACTION);
			action.setSignal(signal);
			action.setTarget(UMLFactory.eINSTANCE.createInputPin());

		} else if (operationPattern != null) {

			final Matcher matcher = operationPattern.matcher(effect);
			if (matcher.matches()) {

				final Operation operation = operationsByName.get(matcher.group(1));
				((CallOperationAction) activity.createOwnedNode(operation.getName(), CALL_OPERATION_ACTION))
						.setOperation(operation);
			}
		}
	}

	private static String extractSignalNameFromEffect(String effect) {
		if (effect == null || effect.isBlank())
			return null;
		if (effect.toLowerCase().trim().startsWith("raise "))
			return effect.substring("raise ".length()).trim();
		if (effect.toLowerCase().trim().startsWith("send "))
			return effect.substring("send ".length()).trim();
		return effect.trim();
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

	@Override
	public Set<EObject> preSave(EObject element, IContext context) {
		return null;
	}

	@Override
	public EAElement postSave(EObject element, IContext context) {
		return null;
	}

	@Override
	public IPostProcessor getPostProcessor() {
		return null;
	}

	@Override
	public int computeDataHash(Set<Integer> excludedPackageIds, Set<String> guids, List<Integer> packageIds,
			List<Integer> elementIds, List<Integer> attributeIds, List<Integer> operationIds,
			List<Integer> connectorIds, IDatabaseAccess db) {
		return 0;
	}
	
	private static void fixEffects(Resource resource) {
		collect(resource.getAllContents(), OpaqueBehavior.class).forEach(behavior -> {
			if (behavior.getBodies().isEmpty() || behavior.getBodies().get(0).isBlank()) {
				behavior.getBodies().add(behavior.getName());
			}
		});
	} 
}
