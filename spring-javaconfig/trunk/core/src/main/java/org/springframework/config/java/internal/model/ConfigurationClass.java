package org.springframework.config.java.internal.model;


import static java.lang.String.format;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.extractClassAnnotation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.model.ModelClass;
import org.springframework.util.Assert;

/**
 * Abstract representation of a user-defined {@link Configuration @Configuration}
 * class.  Includes a set of Bean methods, AutoBean methods, ExternalBean methods,
 * ExternalValue methods, etc.  Includes all such methods defined in the ancestry of
 * the class, in a 'flattened-out' manner.  Note that each BeanMethod representation
 * does still contain source information about where it was originally detected (for
 * the purpose of tooling with Spring IDE).
 *
 * <p>Like the rest of the {@link org.springframework.config.java.internal.model model} package,
 * this class follows the fluent interface / builder pattern such that a model can be
 * built up easily by method chaining.
 *
 * @author Chris Beams
 */
public class ConfigurationClass extends ModelClass {

	/**
	 * Used as metadata on {@link org.springframework.beans.factory.config.BeanDefinition}
	 * to indicate that a bean is a {@link Configuration @Configuration} class
	 * and therefore a candidate for enhancement.
	 *
	 * <p>TODO: find a better name
	 *
	 * @see org.springframework.beans.factory.config.BeanDefinition
	 * @see org.springframework.core.AttributeAccessor#getAttribute(String)
	 * @see org.springframework.config.java.internal.parsing.ConfigurationParser
	 * @see org.springframework.config.java.internal.model.ConfigurationEnhancingBeanFactoryPostProcessor
	 */
	public static final String BEAN_ATTR_NAME = "isJavaConfigurationClass";

	private @Configuration class Prototype { }
	private static final Configuration DEFAULT_METADATA = extractClassAnnotation(Configuration.class, Prototype.class);

	private final int modifiers;

	private final Configuration metadata;

	/** set is used because order does not matter. see {@link #add(BeanMethod)} */
	private HashSet<BeanMethod> beanMethods = new HashSet<BeanMethod>();

	/** set is used because order does not matter. see {@link #add(ExternalBeanMethod)} */
	private HashSet<ExternalBeanMethod> externalBeanMethods = new HashSet<ExternalBeanMethod>();

	private HashSet<ExternalValueMethod> externalValueMethods = new HashSet<ExternalValueMethod>();

	private HashSet<AutoBeanMethod> autoBeanMethods = new HashSet<AutoBeanMethod>();

	private HashSet<NonJavaConfigMethod> nonJavaConfigMethods = new HashSet<NonJavaConfigMethod>();

	/** list is used because order matters. see {@link #add(ResourceBundles)} */
	private ArrayList<ResourceBundles> resourceBundles = new ArrayList<ResourceBundles>();

	// TODO: may need to be a LinkedHashSet to avoid duplicates while preserving original insertion order
	// problem: LinkedHashSet#equals() does not respect insertion order.
	private ArrayList<ConfigurationClass> importedClasses = new ArrayList<ConfigurationClass>();

	private ArrayList<AspectClass> importedAspects = new ArrayList<AspectClass>();

	private ConfigurationClass declaringClass;

	private String pkg;

	/**
	 * Creates a new ConfigurationClass named <var>className</var>
	 * @param name fully-qualified Configuration class being represented
	 * @see #setClassName(String)
	 */
	public ConfigurationClass(String name) {
		this(name, DEFAULT_METADATA, 0);
	}

	public ConfigurationClass(String name, Configuration metadata) {
		this(name, metadata, 0);
	}

	public ConfigurationClass(String name, int modifiers) {
		this(name, DEFAULT_METADATA, modifiers);
	}

	public ConfigurationClass(String name, Configuration metadata, int modifiers) {
		super(name);
		Assert.hasText(name, "Configuration class name must have text");

		Assert.notNull(metadata, "@Configuration annotation must be non-null");
		this.metadata = metadata;

		Assert.isTrue(modifiers >= 0, "modifiers must be non-negative");
		this.modifiers = modifiers;
	}

	/**
	 * bean methods may be locally declared within this class, or discovered
	 * in a superclass.  order is insignificant.
	 */
	public ConfigurationClass add(BeanMethod method) {
		beanMethods.add(method);
		method.setDeclaringClass(this);
		return this;
	}

	public ConfigurationClass add(ExternalBeanMethod method) {
		externalBeanMethods.add(method);
		return this;
	}

	public ConfigurationClass add(ExternalValueMethod method) {
		externalValueMethods.add(method);
		return this;
	}

	public ConfigurationClass add(AutoBeanMethod method) {
		autoBeanMethods.add(method);
		return this;
	}

	public ConfigurationClass add(NonJavaConfigMethod method) {
		nonJavaConfigMethods.add(method);
		return this;
	}

	public BeanMethod[] getBeanMethods() {
		return beanMethods.toArray(new BeanMethod[beanMethods.size()]);
	}

	public AutoBeanMethod[] getAutoBeanMethods() {
		return autoBeanMethods.toArray(new AutoBeanMethod[autoBeanMethods.size()]);
	}

	public ResourceBundles[] getResourceBundles() {
		return resourceBundles.toArray(new ResourceBundles[resourceBundles.size()]);
	}


	public BeanMethod[] getFinalBeanMethods() {
		ArrayList<BeanMethod> finalBeanMethods = new ArrayList<BeanMethod>();
		for(BeanMethod beanMethod : beanMethods)
			if(beanMethod.getMetadata().allowOverriding() == false)
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

	public ConfigurationClass addImportedClass(ConfigurationClass importedClass) {
		importedClasses.add(importedClass);
		return this;
	}

	/**
	 * Returns a properly-ordered collection of this configuration class and
	 * all classes it imports, recursively. Ordering is depth-first, then breadth
	 * such that in the case of a configuration class M that imports classes A and Y
	 * and A imports B and Y imports Z, the contents and order of the resulting
	 * collection will be [B, A, Z, Y, M].  This ordering is significant, because the
	 * most specific class (where M is most specific) should have precedence when it comes
	 * to bean overriding.  In the example above, if M implements a bean (method) named
	 * 'foo' and A implements the same, M's method should 'win' in terms of which bean
	 * will actually resolve upon a call to getBean().
	 */
	public Collection<ConfigurationClass> getSelfAndAllImports() {
		ArrayList<ConfigurationClass> selfAndAllImports = new ArrayList<ConfigurationClass>();

		for(ConfigurationClass importedClass : importedClasses)
			selfAndAllImports.addAll(importedClass.getSelfAndAllImports());

		selfAndAllImports.add(this);
		return selfAndAllImports;
	}

	public ConfigurationClass addImportedAspect(AspectClass aspectClass) {
		importedAspects.add(aspectClass);
		return this;
	}

	public ConfigurationClass setDeclaringClass(ConfigurationClass configurationClass) {
		this.declaringClass = configurationClass;
		return this;
	}

	public ConfigurationClass getDeclaringClass() {
		return declaringClass;
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

	public int getModifiers() {
		return modifiers;
	}

	public Configuration getMetadata() {
		return this.metadata;
	}


	/** must declare at least one Bean method, etc */
	public ValidationErrors validate(ValidationErrors errors) {
		// cascade through all imported classes
		for(ConfigurationClass importedClass : importedClasses)
			importedClass.validate(errors);

		// a configuration class may not be final (CGLIB limitation)
		if(Modifier.isFinal(modifiers))
			errors.add(ValidationError.CONFIGURATION_MUST_BE_NON_FINAL + ": " + getName());

		// a configuration class must declare at least one @Bean/@AutoBean OR import at least one other configuration
		if(importedClasses.isEmpty()
				&& beanMethods.isEmpty()
				&& autoBeanMethods.isEmpty())
			errors.add(ValidationError.CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN.toString() + ": " + getName());

		// if the class is abstract and declares no @ExternalBean or @AutoBean methods, it is malformed
		if(Modifier.isAbstract(modifiers)
				&& externalBeanMethods.isEmpty()
				&& externalValueMethods.isEmpty()
				&& autoBeanMethods.isEmpty()
				)
			errors.add(ValidationError.ABSTRACT_CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_EXTERNALBEAN_EXTERNALVALUE_OR_AUTOBEAN.toString() + ": " + getName());

		// cascade through all declared @Bean methods
		for(BeanMethod method : beanMethods)
			method.validate(errors);

		// cascade through all declared @ExternalBean methods
		for(ExternalBeanMethod method : externalBeanMethods)
			method.validate(errors);

		// cascade through all declared @ExternalValue methods
		for(ExternalValueMethod method : externalValueMethods)
			method.validate(errors);

		// cascade through all declared @AutoBean methods
		for(AutoBeanMethod method : autoBeanMethods)
			method.validate(errors);

		// cascade through all remaning (non-javaconfig) methods
		for(NonJavaConfigMethod method : nonJavaConfigMethods)
			method.validate(errors);

		// TODO: {model validation} could validate that if there are any @ExternalValue methods
		// present that at least one @ResourceBundles must be present. this would actually only
		// apply for abstract @ExternalValue methods, because those that have an implementation
		// can default to it. see ExternalValueMethodInterceptor#initializeValueSource()

		return errors;
	}

	@Override
	public String toString() {
		return format("%s; beanMethods=%s; externalBeanMethods=%s; autoBeanMethods=%s",
					   super.toString(), beanMethods, externalBeanMethods, autoBeanMethods);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((autoBeanMethods == null) ? 0 : autoBeanMethods.hashCode());
		result = prime * result + ((beanMethods == null) ? 0 : beanMethods.hashCode());
		result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
		result = prime * result + ((externalBeanMethods == null) ? 0 : externalBeanMethods.hashCode());
		result = prime * result + ((externalValueMethods == null) ? 0 : externalValueMethods.hashCode());
		result = prime * result + ((importedAspects == null) ? 0 : importedAspects.hashCode());
		result = prime * result + ((importedClasses == null) ? 0 : importedClasses.hashCode());
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + modifiers;
		result = prime * result + ((nonJavaConfigMethods == null) ? 0 : nonJavaConfigMethods.hashCode());
		result = prime * result + ((pkg == null) ? 0 : pkg.hashCode());
		result = prime * result + ((resourceBundles == null) ? 0 : resourceBundles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationClass other = (ConfigurationClass) obj;
		if (autoBeanMethods == null) {
			if (other.autoBeanMethods != null)
				return false;
		}
		else if (!autoBeanMethods.equals(other.autoBeanMethods))
			return false;
		if (beanMethods == null) {
			if (other.beanMethods != null)
				return false;
		}
		else if (!beanMethods.equals(other.beanMethods))
			return false;
		if (declaringClass == null) {
			if (other.declaringClass != null)
				return false;
		}
		else if (!declaringClass.equals(other.declaringClass))
			return false;
		if (externalBeanMethods == null) {
			if (other.externalBeanMethods != null)
				return false;
		}
		else if (!externalBeanMethods.equals(other.externalBeanMethods))
			return false;
		if (externalValueMethods == null) {
			if (other.externalValueMethods != null)
				return false;
		}
		else if (!externalValueMethods.equals(other.externalValueMethods))
			return false;
		if (importedAspects == null) {
			if (other.importedAspects != null)
				return false;
		}
		else if (!importedAspects.equals(other.importedAspects))
			return false;
		if (importedClasses == null) {
			if (other.importedClasses != null)
				return false;
		}
		else if (!importedClasses.equals(other.importedClasses))
			return false;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
			return false;
		if (modifiers != other.modifiers)
			return false;
		if (nonJavaConfigMethods == null) {
			if (other.nonJavaConfigMethods != null)
				return false;
		}
		else if (!nonJavaConfigMethods.equals(other.nonJavaConfigMethods))
			return false;
		if (pkg == null) {
			if (other.pkg != null)
				return false;
		}
		else if (!pkg.equals(other.pkg))
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