package org.springframework.config.java.model;


import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ResourceBundles;

/**
 * Abstract representation of a user-definied {@link Configuration @Configuration}
 * class.  Includes a set of Bean methods, AutoBean methods, ExternalBean methods,
 * ExternalValue methods, etc.  Includes all such methods defined in the ancestry of
 * the class, in a 'flattened-out' manner.  Note that each BeanMethod representation
 * does still contain source information about where it was originally detected (for
 * the purpose of tooling with Spring IDE).
 *
 * <p>Like the rest of the {@link org.springframework.config.java.model model} package,
 * this class follows the fluent interface / builder pattern such that a model can be
 * built up easily by method chaining.
 *
 * @author Chris Beams
 */
public class ConfigurationClass {
	private final String className;
	private ConfigurationClass importedBy;

	/** set is used because order does not matter. see {@link #addBeanMethod(BeanMethod)} */
	private HashSet<BeanMethod> beanMethods = new HashSet<BeanMethod>();

	/** list is used because order matters. see {@link #addResourceBundle(ResourceBundles)} */
	private ArrayList<ResourceBundles> resourceBundles = new ArrayList<ResourceBundles>();

	/**
	 * Creates a new ConfigurationClass named <var>className</var>
	 * @param className fully-qualified Configuration class being represented
	 * @see #setClassName(String)
	 */
	public ConfigurationClass(String className) {
		this.className = className;
	}

	/**
	 * bean methods may be locally declared within this class, or discovered
	 * in a superclass.  order is insignificant.
	 */
	public ConfigurationClass addBeanMethod(BeanMethod beanMethod) {
		beanMethods.add(beanMethod);
		return this;
	}

	public BeanMethod[] getBeanMethods() {
		return beanMethods.toArray(new BeanMethod[] { });
	}

	/**
	 * ResourceBundles may be locally declared on on this class, or discovered
	 * in a superclass.  The contract for processing multiple ResourceBundles
	 * annotations will be to combine them all into a single list of basenames
	 * with duplicates eliminated. This list will be ordered according to the
	 * order in which the ResourceBundles were added.  Therefore it is important
	 * that any configuration class processor is careful to add ResourceBundles
	 * in a superclass-first, top-down fashion.  In this way, the most concrete
	 * class will have precedence and thus be able to 'override' superclass behavior
	 */
	public ConfigurationClass addResourceBundle(ResourceBundles resourceBundle) {
		resourceBundles.add(resourceBundle);
		return this;
	}

	public String getClassName() {
		return className;
	}

	/** must declare at least one Bean method, etc */
	public ValidationErrors validate(ValidationErrors errors) {
		if(beanMethods.isEmpty())
			errors.add(ValidationError.CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN);
		return errors;
	}

	@Override
	public String toString() {
		return format("{%s:className=%s,beanMethods=%s}", getClass().getSimpleName(), className, beanMethods);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanMethods == null) ? 0 : beanMethods.hashCode());
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((importedBy == null) ? 0 : importedBy.hashCode());
		result = prime * result + ((resourceBundles == null) ? 0 : resourceBundles.hashCode());
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
		ConfigurationClass other = (ConfigurationClass) obj;
		if (beanMethods == null) {
			if (other.beanMethods != null)
				return false;
		}
		else if (!beanMethods.equals(other.beanMethods))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		}
		else if (!className.equals(other.className))
			return false;
		if (importedBy == null) {
			if (other.importedBy != null)
				return false;
		}
		else if (!importedBy.equals(other.importedBy))
			return false;
		if (resourceBundles == null) {
			if (other.resourceBundles != null)
				return false;
		}
		else if (!resourceBundles.equals(other.resourceBundles))
			return false;
		return true;
	}
}