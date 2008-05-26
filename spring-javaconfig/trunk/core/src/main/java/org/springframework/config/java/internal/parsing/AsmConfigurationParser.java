package org.springframework.config.java.internal.parsing;

import org.springframework.config.java.internal.model.ConfigurationModel;

/**
 * TODO: implement
 *
 * @see ReflectiveConfigurationParser for a reference implementation
 * @see org.springframework.config.java.internal.model.ConfigurationParserTests.AsmConfigurationParserTests
 */
public class AsmConfigurationParser implements ConfigurationParser {

	private final ConfigurationModel model;

	public AsmConfigurationParser(ConfigurationModel model) {
		this.model = model;
	}

	public void parse(Object configurationSource) {
		throw new UnsupportedOperationException();
	}

}
