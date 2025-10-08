package com.yakindu.bridges.ea.example.cli.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;

import com.google.common.collect.Multimap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import com.yakindu.base.types.adapter.OriginTraceAdapter;
import com.yakindu.bridges.ea.core.utils.EAResourceUtils;
import com.yakindu.bridges.ea.core.utils.MapUtils;
import com.yakindu.bridges.ea.example.cli.codegen.util.StatechartUtil;
import com.yakindu.sct.domain.c.runtime.CSTextRuntimeModule;
import com.yakindu.sct.model.sgraph.SpecificationElement;
import com.yakindu.sct.model.sgraph.Statechart;
import com.yakindu.sct.model.stext.resource.SCTResourceValidatorImpl;
import com.yakindu.sct.model.stext.resource.StextResource;
import com.yakindu.sct.uml2.transformation.IStatemachineTransformation;
import com.yakindu.sct.uml2.transformation.module.TransformationModule;

public class SCTValidation {
	
	private static final List<String> IGNORED_MESSAGES = List.of("Domain 'com.yakindu.domain.c' is not available.");	
	
	@Inject
	private IStatemachineTransformation trafo;

	// the resource field will be used in subsequent implementations to match sct elements causing issues to their
	// equivalent in the EA model
	private Collection<StateMachine> stms;
	@Inject
	private StatechartUtil statechartUtil;
	@Inject
	private SCTResourceValidatorImpl validator;

	public SCTValidation(Collection<StateMachine> stms) {
		this.stms = stms != null ? stms : new ArrayList<>();
	}

	public void initialize() {
		trafo = null;
		// Only for Generic and C Domain now
		Guice.createInjector(Modules.override(new TransformationModule()).with(new CSTextRuntimeModule()))
				.injectMembers(this);
	}

	public void validateStatechartsForResource(ValidationResult result, boolean verbose, boolean isTest) {
		stms.forEach(stm -> validateStatechart(result, stm, verbose, isTest));
	}

	private void validateStatechart(ValidationResult result, StateMachine stateMachine, boolean verbose,
			boolean isTest) {
		initialize();
		final StextResource res = statechartUtil.createResource(stateMachine.getName());
		final Statechart statechart = trafo.transformStatemachine(stateMachine, res);
		statechartUtil.makeCStatechart(statechart);
		validateStatechart(result, res, statechart, stateMachine, verbose, isTest);
	}

	private void validateStatechart(ValidationResult result, final StextResource res, final Statechart statechart,
			StateMachine stateMachine, boolean verbose, boolean isTest) {
		try {
			final Map<EObject, Map<String, Severity>> issuesMap = getTraceableIssues(
					(StextResource) statechart.eResource(), List.of(Severity.ERROR, Severity.WARNING));
			for (EObject object : issuesMap.keySet()) {
				for (Entry<String, Severity> issueEntry : issuesMap.get(object).entrySet()) {

					if (issueEntry.getValue() == Severity.WARNING) {
						result.addSctWarning(object, issueEntry.getKey(), res,
								this::getUMLElementFromSctElement);
					} else {
						result.addSctError(object, issueEntry.getKey(), res,
								this::getUMLElementFromSctElement);
					}
				}
			}

		} catch (Exception e) {
			new Exception("Failed to validate state machine: " + UMLUtil.getQualifiedText(stateMachine), e)
					.printStackTrace(System.out);
		}
	}
	
	public Map<EObject, Map<String, Severity>> getTraceableIssues(StextResource resource,
			List<Severity> requiredSeverityLevels) {
		final Map<EObject, Map<String, Severity>> result = new HashMap<>();

		final List<Issue> validatorResult = validator.validate(resource, CheckMode.NORMAL_AND_FAST,
				CancelIndicator.NullImpl);

		final Multimap<SpecificationElement, Diagnostic> linkingDiagnostics = resource.getLinkingDiagnostics();
		final Multimap<SpecificationElement, Diagnostic> syntaxDiagnostics = resource.getSyntaxDiagnostics();

		validatorResult.stream()
				.filter(issue -> requiredSeverityLevels.contains(issue.getSeverity())
						&& !skipValidationIssue(issue.getMessage()))
				.forEach(issue -> MapUtils.addToMapMap(result, getUMLElementForIssue(resource, issue),
						issue.getMessage(), issue.getSeverity()));

		if (requiredSeverityLevels.contains(Severity.ERROR)) {
			linkingDiagnostics.entries().stream().forEach(e -> MapUtils.addToMapMap(result,
					getUMLElementFromSctElement(e.getKey()), e.getValue().getMessage(), Severity.ERROR));
			syntaxDiagnostics.entries().stream().forEach(e -> MapUtils.addToMapMap(result,
					getUMLElementFromSctElement(e.getKey()), e.getValue().getMessage(), Severity.ERROR));
		}
		return result;
	}
	
	private boolean skipValidationIssue(String issue) {
		if (issue == null || issue.isBlank())
			return true; 
		for (String msg : IGNORED_MESSAGES) {
			if (issue.contains(msg))
				return true;
		}
		return false;
	}
	
	public EObject getUMLElementFromSctElement(EObject sctElement) {
		EObject result = null;
		if (sctElement == null) {
			return result;
		}
		final OriginTraceAdapter umlOriginAdapter = (OriginTraceAdapter) sctElement.eAdapters().stream()
				.filter(OriginTraceAdapter.class::isInstance).findFirst().orElse(null);
		if (umlOriginAdapter != null) {
			final EObject umlObject = (EObject) umlOriginAdapter.getOrigin();
			if (umlObject != null) {
				final Integer umlObjectId = EAResourceUtils.getIdForElement(umlObject);
				if (umlObjectId == null) {
					result = getUMLElementFromSctElement(sctElement.eContainer());
				} else {
					result = umlObject;
				}
			}
		} else {
			result = getUMLElementFromSctElement(sctElement.eContainer());
		}
		return result;
	}
	
	private EObject getUMLElementForIssue(StextResource resource, Issue issue) {
		EObject result = null;
		URI uri = issue.getUriToProblem();
		if (uri != null && uri.hasFragment()) {
			EObject foundSctElement = resource.getEObject(uri.fragment());
			if (foundSctElement != null) {
				result = getUMLElementFromSctElement(foundSctElement);
			}
		}
		if (result == null) {
			try {
				return getUMLElementFromResource(resource);
			} catch (Exception e) {
				e.printStackTrace(System.out);
				return null;
			}
		} else {
			return result;
		}
	}
	
	private EObject getUMLElementFromResource(StextResource resource) throws Exception {
		final EObject resourceContent = resource.getContents().stream().findFirst()
				.orElseThrow(() -> new Exception("resource has no UML origin"));
		final EObject umlOriginStateMachine = getUMLElementFromSctElement(resourceContent);
		return Objects.requireNonNullElse(umlOriginStateMachine, resourceContent);
	}

	public Resource getSCTStatechartResource(StateMachine stm) {
		initialize();
		final Resource res = statechartUtil.createResource(stm.getName());
		final Statechart statechart = trafo.transformStatemachine(stm, res);
		statechartUtil.makeCStatechart(statechart);
		return res;
	}
}
