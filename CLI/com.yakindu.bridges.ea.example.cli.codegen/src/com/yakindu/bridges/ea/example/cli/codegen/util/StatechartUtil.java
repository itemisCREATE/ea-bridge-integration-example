package com.yakindu.bridges.ea.example.cli.codegen.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.yakindu.sct.ui.editor.editor.StatechartDiagramEditor;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.yakindu.sct.model.sgraph.Statechart;
import com.yakindu.sct.model.stext.resource.StextResource;
import com.yakindu.sct.ui.editor.DiagramActivator;
import com.yakindu.sct.ui.editor.providers.SemanticHints;
import com.yakindu.sct.ui.editor.providers.StatechartDiagramViewProvider;

public class StatechartUtil {
	
	private static final int INITIAL_TEXT_COMPARTMENT_X = 10;
	private static final int INITIAL_TEXT_COMPARTMENT_Y = 10;
	private static final int INITIAL_TEXT_COMPARTMENT_HEIGHT = 400;
	private static final int INITIAL_TEXT_COMPARTMENT_WIDTH = 200;
	
	private static final String YAKINDU_SCT_DOMAIN_C = "com.yakindu.domain.c";
	
	private static final String YAKINDU_SCT_DOMAIN_JAVA = "com.yakindu.domain.java";
	
	@Inject
	private Provider<StextResource> resourceProvider;
	
	public StextResource createResource(String sctName) {
		final StextResource stextResource = resourceProvider.get();

		final ResourceSet set = new ResourceSetImpl();
		set.getResources().add(stextResource);

		final URI uri = URI.createURI("mem:/" + sctName);
		stextResource.setURI(uri);
		return stextResource;
	}
	
	public void makeCStatechart(Statechart sct) {
		//TODO: Currently we do not use nor benefit from setting the C domain,
		//thus until the UML2SCT transformation is not prepared for the target specific type transformation this should be disabled
		//For more information see: https://github.com/itemisCREATE/ea-bridge-integration-example/issues/11
		//sct.setDomainID(YAKINDU_SCT_DOMAIN_C);
	}
	
	public void makeJavaStatechart(Statechart sct) {
		//TODO: Same as above
		//sct.setDomainID(YAKINDU_SCT_DOMAIN_JAVA);
	}
	
	private static void setTextCompartmentLayoutConstraint(Node textCompartment) {
		final Bounds bounds = NotationFactory.eINSTANCE.createBounds();
		bounds.setX(INITIAL_TEXT_COMPARTMENT_X);
		bounds.setY(INITIAL_TEXT_COMPARTMENT_Y);
		bounds.setHeight(INITIAL_TEXT_COMPARTMENT_HEIGHT);
		bounds.setWidth(INITIAL_TEXT_COMPARTMENT_WIDTH);
		textCompartment.setLayoutConstraint(bounds);
	}
	
	public String saveStatechart(Statechart sct, final String outputFolder, boolean verbose) {

		final File outFolderFile = new File(outputFolder);
		final String sctFilePath = outFolderFile.getAbsolutePath().toString() + File.separator + sct.getName()
				+ File.separator + sct.getName() + ".ysc";
		final Resource stextResource = sct.eResource();

		final StatechartDiagramViewProvider provider = new StatechartDiagramViewProvider();
		final Diagram diagram = provider.createDiagram(new EObjectAdapter(sct), StatechartDiagramEditor.ID,
				DiagramActivator.DIAGRAM_PREFERENCES_HINT);
		diagram.setElement(sct);
		stextResource.getContents().add(diagram);

		final Node statechartText = ViewService.createNode(diagram, sct, SemanticHints.STATECHART_TEXT,
				DiagramActivator.DIAGRAM_PREFERENCES_HINT);
		ViewService.createNode(statechartText, sct, SemanticHints.STATECHART_NAME,
				DiagramActivator.DIAGRAM_PREFERENCES_HINT);
		ViewService.createNode(statechartText, sct, SemanticHints.STATECHART_TEXT_EXPRESSION,
				DiagramActivator.DIAGRAM_PREFERENCES_HINT);
		setTextCompartmentLayoutConstraint(statechartText);

		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			stextResource.save(outputStream,Map.of(XMLResource.OPTION_XML_VERSION, "1.1"));
			final File file = new File(sctFilePath);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			Files.asCharSink(file, StandardCharsets.UTF_8).write(outputStream.toString(StandardCharsets.UTF_8));
			
			if (verbose)
				System.out.println("Saved to: " + sctFilePath);
		} catch (Exception e) {
			System.out.println("FAILED to save statechart '" + sct.getName() + "' (" + e.getClass().getSimpleName()
					+ ") to '" + sctFilePath + "': " + e.getMessage());
			if (verbose)
				e.printStackTrace(System.out);
		}

		return sctFilePath;
	}
	
	public Resource loadResource(URI uri) {
		final Resource stextResource = resourceProvider.get();
		final ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResources().add(stextResource);
		stextResource.setURI(uri);
		try {
			stextResource.load(Collections.EMPTY_MAP);
			return stextResource;
		} catch (IOException e) {
			throw new IllegalStateException("Error loading resource", e);
		}
	}

}
