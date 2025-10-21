package com.yakindu.bridges.ea.example.cli.codegen.builders;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.yakindu.sct.model.sgen.GeneratorEntry;
import com.yakindu.sct.model.sgen.GeneratorModel;
import com.yakindu.sct.model.sgen.SGenFactory;
import com.yakindu.sct.model.sgraph.Statechart;

public class GeneratorEntryFactory {

	private static final SGenFactory FACTORY = SGenFactory.eINSTANCE;

	public GeneratorEntry create(Map<String, List<Entry<String, Object>>> generatorOptions, Statechart statechart) {
		final GeneratorEntryBuilder genEntry = new GeneratorEntryBuilder();
		genEntry.forStatechart(statechart);

		if (generatorOptions != null) {
			for (String type : generatorOptions.keySet()) {
				genEntry.addFeatureType(type, generatorOptions.get(type));
			}
		}
		final GeneratorEntry entry = genEntry.build();

		final GeneratorModel generatorModel = FACTORY.createGeneratorModel();
		generatorModel.getEntries().add(entry);

		return entry;
	}
}
