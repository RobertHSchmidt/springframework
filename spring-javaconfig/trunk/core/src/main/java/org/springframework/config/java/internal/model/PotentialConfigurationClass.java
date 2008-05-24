package org.springframework.config.java.internal.model;

import org.springframework.config.java.annotation.Configuration;

/**
 * Represents a class that, at parsing time, is undetermined whether
 * it should be a configuration class.  At present, this is used in
 * the case of declaring classes.
 * <p>
 * TODO: add in modified validation rules and isConfigurationClass()
 * logic.
 *
 * @author Chris Beams
 */
public class PotentialConfigurationClass extends ConfigurationClass {

	public PotentialConfigurationClass(String name, int modifiers) {
		super(name, modifiers);
	}

	public PotentialConfigurationClass(String name) {
		super(name);
	}

	public PotentialConfigurationClass(String name, Configuration metadata, int modifiers) {
		super(name, metadata, modifiers);
	}

}
