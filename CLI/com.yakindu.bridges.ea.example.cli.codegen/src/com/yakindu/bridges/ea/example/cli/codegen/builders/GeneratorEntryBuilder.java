package com.yakindu.bridges.ea.example.cli.codegen.builders;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.yakindu.sct.model.sgen.FeatureConfiguration;
import com.yakindu.sct.model.sgen.FeatureParameter;
import com.yakindu.sct.model.sgen.FeatureParameterValue;
import com.yakindu.sct.model.sgen.FeatureType;
import com.yakindu.sct.model.sgen.GeneratorEntry;
import com.yakindu.sct.model.sgen.SGenFactory;
import com.yakindu.sct.model.sgraph.Statechart;

public class GeneratorEntryBuilder {

	private static final SGenFactory FACTORY = SGenFactory.eINSTANCE;

	private Statechart statechart = null;
	private final Map<String, List<Entry<String, Object>>> features = Maps.newHashMap();

	public GeneratorEntryBuilder forStatechart(Statechart statechart) {
		this.statechart = statechart;
		return this;
	}

	public GeneratorEntryBuilder addFeatureType(String featureTypeName, List<Entry<String, Object>> parameterValues) {
		features.put(featureTypeName, parameterValues);
		return this;
	}

	public GeneratorEntry build() {
		if (statechart == null) {
			throw new IllegalArgumentException("No statechart provided");
		}
		final GeneratorEntry entry = FACTORY.createGeneratorEntry();
		entry.setElementRef(statechart);

		for (String typeName : features.keySet()) {
			final List<Entry<String, Object>> configs = features.get(typeName);
			
			final FeatureConfiguration featureConfig = FACTORY.createFeatureConfiguration();
			entry.getFeatures().add(featureConfig);
			
			final FeatureType type = FACTORY.createFeatureType();
			type.setName(typeName);
			featureConfig.setType(type);
			
			for (Entry<String, Object> config : configs) {
				final FeatureParameterValue paramValue = FACTORY.createFeatureParameterValue();
				final FeatureParameter param = FACTORY.createFeatureParameter();
				paramValue.setParameter(param);
				param.setName(config.getKey());
				setValue(paramValue, config.getValue());
				featureConfig.getParameterValues().add(paramValue);
			}
		}
		return entry;
		
//		createGeneratorEntry => [ entry |
//			entry.elementRef = statechart
//			features.forEach [ typeName, configs |
//				entry.features += createFeatureConfiguration => [
//					type = createFeatureType => [name = typeName]
//					parameterValues += configs.map [ config |
//						createFeatureParameterValue => [
//							parameter = createFeatureParameter => [name = config.key]
//							value = config.value
//						]
//					]
//				]
//			]
//		]
	}

	private void setValue(FeatureParameterValue paramValue, Object value) {
		if (value instanceof String) {
			paramValue.setValue((String) value);
		} else if (value instanceof Boolean) {
			paramValue.setValue((Boolean) value);
		} else if (value instanceof Integer) {
			paramValue.setValue((Integer) value);
		} else {
			// skip value
		}
	}
}
