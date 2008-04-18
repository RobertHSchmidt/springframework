package org.springframework.config.java.model;

import static java.lang.String.format;

import java.util.ArrayList;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.process.MalformedJavaConfigurationException;

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
 * <p>Interface follows the builder pattern for method chaining.
 *
 * @author Chris Beams
 */
public class JavaConfigurationModel {

	/** list is used because order and collection equality matters. */
	private ArrayList<ConfigurationClass> configurationClasses = new ArrayList<ConfigurationClass>();

	/**
	 * Add a {@link Configuration @Configuration} class to the model.  Classes
	 * may be added at will and without any particular validation.  Malformed
	 * classes will be caught and errors processed during a later phase.
	 *
	 * @param configurationClass user-supplied Configuration class
	 */
	public JavaConfigurationModel addConfigurationClass(ConfigurationClass configurationClass) {
		configurationClasses.add(configurationClass);
		return this;
	}

	public ConfigurationClass[] getConfigurationClasses() {
		return configurationClasses.toArray(new ConfigurationClass[] {});
	}

	public ValidationErrors validate() {
		ValidationErrors errors = new ValidationErrors();

		if(configurationClasses.isEmpty())
			errors.add(ValidationError.MODEL_IS_EMPTY);

		for(ConfigurationClass configClass : configurationClasses)
			configClass.validate(errors);

		return errors;
	}

	public void assertIsValid() {
		ValidationErrors errors = validate();
		if(errors.size() > 0)
			throw new MalformedJavaConfigurationException(errors.toString());
	}

	@Override
	public String toString() {
		return format("{%s:configurationClasses=%s}", getClass().getSimpleName(), configurationClasses);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configurationClasses == null) ? 0 : configurationClasses.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaConfigurationModel other = (JavaConfigurationModel) obj;
		if (configurationClasses == null) {
			if (other.configurationClasses != null)
				return false;
		}
		else if (!configurationClasses.equals(other.configurationClasses))
			return false;
		return true;
	}


}