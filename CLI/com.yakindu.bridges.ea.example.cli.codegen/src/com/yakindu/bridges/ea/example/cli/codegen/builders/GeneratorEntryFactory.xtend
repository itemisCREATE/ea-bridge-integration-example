package com.yakindu.bridges.ea.example.cli.codegen.builders;

import com.yakindu.sct.model.sgen.SGenFactory
import com.yakindu.sct.model.sgraph.Statechart
import java.util.List
import java.util.Map

class GeneratorEntryFactory {

	val extension SGenFactory = SGenFactory.eINSTANCE

	def create(Map<String, List<Pair<String, Object>>> generatorOptions, Statechart statechart) {

		val genEntry = new GeneratorEntryBuilder()
		genEntry.forStatechart(statechart)

		if (generatorOptions !== null) {
			for (type : generatorOptions.keySet) {
				genEntry.addFeatureType(type, generatorOptions.get(type))
			}
		}

		val entry = genEntry.build()

		createGeneratorModel => [
			entries += entry
		]
		return entry
	}
}
