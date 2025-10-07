package com.yakindu.bridges.ea.example.cli.codegen.builders;

import com.google.common.collect.Maps
import com.yakindu.sct.model.sgen.FeatureParameterValue
import com.yakindu.sct.model.sgen.SGenFactory
import com.yakindu.sct.model.sgraph.Statechart
import java.util.List
import java.util.Map

class GeneratorEntryBuilder {

	val extension SGenFactory factory = SGenFactory.eINSTANCE

	var Statechart statechart = null;
	val Map<String, List<Pair<String, Object>>> features = Maps.newHashMap

	def forStatechart(Statechart statechart) {
		this.statechart = statechart
		return this
	}

	def addFeatureType(String featureTypeName, List<Pair<String, Object>> parameterValues) {
		features.put(featureTypeName, parameterValues)
		return this
	}

	def build() {
		if (statechart === null) {
			throw new IllegalArgumentException("No statechart provided")
		}
		createGeneratorEntry => [ entry |
			entry.elementRef = statechart
			features.forEach [ typeName, configs |
				entry.features += createFeatureConfiguration => [
					type = createFeatureType => [name = typeName]
					parameterValues += configs.map [ config |
						createFeatureParameterValue => [
							parameter = createFeatureParameter => [name = config.key]
							value = config.value
						]
					]
				]
			]
		]
	}

	private def dispatch setValue(FeatureParameterValue param, String value) {
		param.value = value
	}

	private def dispatch setValue(FeatureParameterValue param, Integer value) {
		param.value = value
	}

	private def dispatch setValue(FeatureParameterValue param, Boolean value) {
		param.value = value
	}

}
