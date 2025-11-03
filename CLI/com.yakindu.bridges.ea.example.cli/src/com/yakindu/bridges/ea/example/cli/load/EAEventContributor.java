package com.yakindu.bridges.ea.example.cli.load;

import static org.eclipse.uml2.uml.UMLPackage.Literals.EVENT;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.TimeEvent;

import com.yakindu.bridges.ea.core.jdbc.IDatabaseAccess;
import com.yakindu.bridges.ea.uml.contributors.IContext;
import com.yakindu.bridges.ea.uml.contributors.IElementContributor;

public class EAEventContributor implements IElementContributor {

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
		return new ElementContribution(EVENT, 0);
	}

	@Override
	public List<EAElement> load(List<EObject> elements, IContext context) {
		for (EObject event : elements) {
			if (event instanceof TimeEvent timeEvent) {
				timeEvent.setIsRelative(true);
			} else if (event instanceof SignalEvent signalEvent && signalEvent.getSignal() != null) {
				signalEvent.setName(signalEvent.getSignal().getName());
			} else if (event instanceof CallEvent callEvent && callEvent.getOperation() != null) {
				callEvent.setName(callEvent.getOperation().getName());
			}
		}
		return null;
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
}
