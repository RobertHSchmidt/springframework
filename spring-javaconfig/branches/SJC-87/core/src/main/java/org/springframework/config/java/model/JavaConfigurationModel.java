package org.springframework.config.java.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.config.java.annotation.Configuration;

/**
 * An abstract representation of a set of user-provided "Configuration classes",
 * usually but not necessarily annotated with {@link Configuration @Configuration}.
 * The model is populated with a
 * {@link org.springframework.config.java.process.ConfigurationProcessor} implementation,
 * which may be reflection-based or ASM-based.  Once a model has been populated, it
 * can then be rendered out to a set of BeanDefinitions.  The model provides an important
 * layer of indirection between the complexity of parsing a set of classes and the complexity
 * of representing the contents of those classes as BeanDefinitions.
 *
 * @author Chris Beams
 */
public class JavaConfigurationModel {
	private Set<ConfigurationClass> configurationClasses = new LinkedHashSet<ConfigurationClass>();

	/**
	 * Add a {@link Configuration @Configuration} class to the model.  Classes
	 * may be added at will and without any particular validation.  Malformed
	 * classes will be caught and errors processed during a later phase.
	 * 
	 * @param configurationClass user-supplied Configuration class
	 */
	public void addConfigurationClass(ConfigurationClass configurationClass) {
		configurationClasses.add(configurationClass);
	}

	public ConfigurationClass[] getConfigurationClasses() {
		return configurationClasses.toArray(new ConfigurationClass[] {});
	}
}