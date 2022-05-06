package com.yakindu.bridges.ea.example.cli.validation.uml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.uml2.uml.Element;

/**
 * Copy from inner class of UMLActionBarContributor.
 */
public class UMLDiagnostician extends Diagnostician {

	private static final String PLUGIN_ID = "com.yakindu.bridges.ea.example.validation";

	public final static UMLDiagnostician INSTANCE = new UMLDiagnostician();

	public Diagnostic validate(Resource resource) {
		final List<BasicDiagnostic> diagnostics = new ArrayList<>();

		final Map<Object, Object> defaultContext = createDefaultContext();
		for (EObject obj : resource.getContents()) {
			if (obj instanceof Element) {
				final BasicDiagnostic diagnostic = createDefaultDiagnostic(obj);
				validate(obj, diagnostic, defaultContext);
				if (diagnostic.getSeverity() >= Diagnostic.WARNING) {
					diagnostics.add(diagnostic);
				}
			}
		}

		if (diagnostics.isEmpty())
			return Diagnostic.OK_INSTANCE;
		if (diagnostics.size() == 1)
			return diagnostics.get(0);
		return new BasicDiagnostic(PLUGIN_ID, 0, diagnostics, EcorePlugin.INSTANCE
				.getString("_UI_DiagnosticRoot_diagnostic", new Object[] { "<Resource> " + resource.getURI() }),
				new Object[] { resource });
	}

	protected boolean doValidateStereotypeApplications(EObject eObject, DiagnosticChain diagnostics,
			Map<Object, Object> context) {
		List<EObject> stereotypeApplications = eObject instanceof Element
				? ((Element) eObject).getStereotypeApplications()
				: Collections.<EObject>emptyList();

		if (!stereotypeApplications.isEmpty()) {
			Iterator<EObject> i = stereotypeApplications.iterator();
			boolean result = validate(i.next(), diagnostics, context);

			while (i.hasNext() && (result || diagnostics != null)) {
				result &= validate(i.next(), diagnostics, context);
			}

			return result;
		} else {
			return true;
		}
	}

	@Override
	protected boolean doValidateContents(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = doValidateStereotypeApplications(eObject, diagnostics, context);

		if (result || diagnostics != null) {
			result &= super.doValidateContents(eObject, diagnostics, context);
		}

		return result;
	}

	@Override
	public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return super.validate(eClass, eObject, diagnostics, context);
	}
}