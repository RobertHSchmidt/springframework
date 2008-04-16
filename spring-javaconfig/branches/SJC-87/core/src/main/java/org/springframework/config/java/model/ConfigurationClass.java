package org.springframework.config.java.model;


import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ResourceBundles;

/**
 * Abstract representation of a user-definied {@link Configuration @Configuration}
 * class.  Includes a set of Bean methods, AutoBean methods, ExternalBean methods,
 * ExternalValue methods, etc.  Includes all such methods defined in the ancestry of
 * the class, in a 'flattened-out' manner.  Note that each BeanMethod representation
 * does still contain source information about where it was originally detected (for
 * the purpose of tooling with Spring IDE).
 * @author cbeams
 *
 */
public class ConfigurationClass {
	private String className;
	private Set<BeanMethod> beanMethods = new LinkedHashSet<BeanMethod>();
	private ConfigurationClass importedBy;
	private Set<ResourceBundles> resourceBundles = new LinkedHashSet<ResourceBundles>();

	/**
	 * bean methods may be locally declared within this class, or discovered
	 * in a superclass.  The contract for processing overlapping bean methods
	 * is a last-in-wins model, so it is important that any configuration
	 * class processor is careful to add bean methods in a superclass-first,
	 * top-down fashion.
	 */
	public void addBeanMethod(BeanMethod beanMethod) {
		beanMethods.add(beanMethod);
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
	public void addResourceBundle(ResourceBundles resourceBundle) {
		resourceBundles.add(resourceBundle);
	}

	/** fully-qualified classname for this Configuration class */
	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	/** must declare at least one Bean method, etc */
	public boolean isWellFormed() {
		return true;
	}
}