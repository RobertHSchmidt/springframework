package org.springframework.config.java.model;


import static java.lang.String.format;
import static org.springframework.util.ClassUtils.getShortName;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.util.Assert;

/**
 * Abstract representation of a user-defined {@link Configuration @Configuration}
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

	/**
	 * Used as metadata on {@link org.springframework.beans.factory.config.BeanDefinition}
	 * to indicate that a bean is a {@link Configuration @Configuration} class
	 * and therefore a candidate for enhancement.
	 *
	 * <p>TODO: find a better name
	 *
	 * @see org.springframework.beans.factory.config.BeanDefinition
	 * @see org.springframework.core.AttributeAccessor#getAttribute(String)
	 * @see org.springframework.config.java.model.ConfigurationParser
	 * @see org.springframework.config.java.model.ConfigurationEnhancingBeanFactoryPostProcessor
	 */
	public static final String BEAN_ATTR_NAME = "isJavaConfigurationClass";

	private final String name;
	private ConfigurationClass importedBy;
	private final int modifiers;

	/** set is used because order does not matter. see {@link #add(BeanMethod)} */
	private HashSet<BeanMethod> beanMethods = new HashSet<BeanMethod>();

	/** set is used because order does not matter. see {@link #add(ExternalBeanMethod)} */
	private HashSet<ExternalBeanMethod> externalBeanMethods = new HashSet<ExternalBeanMethod>();

	/** list is used because order matters. see {@link #add(ResourceBundles)} */
	private ArrayList<ResourceBundles> resourceBundles = new ArrayList<ResourceBundles>();

	/**
	 * Creates a new ConfigurationClass named <var>className</var>
	 * @param name fully-qualified Configuration class being represented
	 * @see #setClassName(String)
	 */
	public ConfigurationClass(String name) {
		this(name, 0);
	}

	public ConfigurationClass(String name, int modifiers) {
		Assert.hasText(name);
		this.name = name;

		Assert.isTrue(modifiers >= 0, "modifiers must be non-negative");
		this.modifiers = modifiers;
	}

	/**
	 * bean methods may be locally declared within this class, or discovered
	 * in a superclass.  order is insignificant.
	 */
	public ConfigurationClass add(BeanMethod method) {
		beanMethods.add(method);
		return this;
	}

	public ConfigurationClass add(ExternalBeanMethod method) {
		externalBeanMethods.add(method);
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
	public ConfigurationClass add(ResourceBundles resourceBundle) {
		resourceBundles.add(resourceBundle);
		return this;
	}

	public String getName() {
		return name;
	}

	public int getModifiers() {
		return modifiers;
	}


	public BeanMethod[] getFinalBeanMethods() {
		ArrayList<BeanMethod> finalBeanMethods = new ArrayList<BeanMethod>();
		for(BeanMethod beanMethod : beanMethods)
			if(beanMethod.getBeanAnnotation().allowOverriding() == false)
				finalBeanMethods.add(beanMethod);

		return finalBeanMethods.toArray(new BeanMethod[finalBeanMethods.size()]);
	}

	public boolean containsBeanMethod(String beanMethodName) {
		Assert.hasText(beanMethodName, "beanMethodName must be non-empty");
		for(BeanMethod beanMethod : beanMethods)
			if(beanMethod.getName().equals(beanMethodName))
				return true;
		return false;
	}


	/** must declare at least one Bean method, etc */
	public ValidationErrors validate(ValidationErrors errors) {
		if(beanMethods.isEmpty())
			errors.add(ValidationError.CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN.toString() + ": " + name);

		// if the class is abstract and declares no @ExternalBean methods, it is malformed
		if(Modifier.isAbstract(modifiers)
				&& externalBeanMethods.size() == 0)
			errors.add(ValidationError.ABSTRACT_CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_EXTERNALBEAN.toString() + ": " + name);

		return errors;
	}

	@Override
	public String toString() {
		return format("%s: name=%s; beanMethods=%s; externalBeanMethods=%s",
					   getClass().getSimpleName(), getShortName(name), beanMethods, externalBeanMethods);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanMethods == null) ? 0 : beanMethods.hashCode());
		result = prime * result + ((externalBeanMethods == null) ? 0 : externalBeanMethods.hashCode());
		result = prime * result + ((importedBy == null) ? 0 : importedBy.hashCode());
		result = prime * result + modifiers;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (externalBeanMethods == null) {
			if (other.externalBeanMethods != null)
				return false;
		}
		else if (!externalBeanMethods.equals(other.externalBeanMethods))
			return false;
		if (importedBy == null) {
			if (other.importedBy != null)
				return false;
		}
		else if (!importedBy.equals(other.importedBy))
			return false;
		if (modifiers != other.modifiers)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
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