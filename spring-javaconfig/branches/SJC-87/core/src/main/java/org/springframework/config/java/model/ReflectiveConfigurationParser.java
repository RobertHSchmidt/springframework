package org.springframework.config.java.model;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.config.java.annotation.Aspects;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;


/**
 * Parses configuration class literals using java reflection, writing the results
 * out to a {@link ConfigurationModel} object.
 * <p/>
 * TODO: rename to ReflectiveConfigurationParser (see precedent
 * at ReflectiveAspectJAdvisorFactory)
 *
 * @author Chris Beams
 */
public class ReflectiveConfigurationParser implements ConfigurationParser {

	private static final Log logger = LogFactory.getLog(ReflectiveConfigurationParser.class);

	private final ConfigurationModel model;

	public ReflectiveConfigurationParser(ConfigurationModel model) {
		this.model = model;
	}

	/**
	 * Parse <var>configurationSource</var>, populating <tt>model</tt>.
	 *
	 * @param configurationSource class literal to be parsed via reflection
	 */
	public void parse(Object configurationSource) {
		Class<?> classLiteral = (Class<?>) configurationSource;
		model.add(doParse(classLiteral, false));
	}

	/**
	 *
	 * @param literalClass
	 * @param isDeclaringClass
	 * @return
	 */
	private ConfigurationClass doParse(final Class<?> literalClass, boolean isDeclaringClass) {
		ConfigurationClass modelClass = createConfigurationClass(literalClass, isDeclaringClass);

		processAnyDeclaringClass(literalClass, modelClass);

		processAnyImportedConfigurations(literalClass, modelClass);

		processAnyImportedAspects(literalClass);

		processSelfAsAspectIfAnnotated(literalClass);

		processAllMethods(literalClass, modelClass);

		return modelClass;
	}

	private ConfigurationClass createConfigurationClass(Class<?> literal, boolean isDeclaringClass) {
		final ConfigurationClass modelClass;

		String className = literal.getName();
		Configuration metadata = AnnotationUtils.findAnnotation(literal, Configuration.class);
		int modifiers = literal.getModifiers();

		// treat declaring classes specially: they are only potentially configuration
		// classes -> the validation rules are different (see PCC class for details)
		if(isDeclaringClass)
			if(metadata == null)
				modelClass = new PotentialConfigurationClass(className, modifiers);
			else
				modelClass = new PotentialConfigurationClass(className, metadata, modifiers);
		// otherwise -> create a class as usual
		else
			if(metadata == null)
				modelClass = new ConfigurationClass(className, modifiers);
			else
				modelClass = new ConfigurationClass(className, metadata, modifiers);

		return modelClass;
	}

	private void processAnyDeclaringClass(final Class<?> literalClass, ConfigurationClass modelClass) {
		// detect and process this configuration class's declaring class (if any)
		Class<?> declaringLiteralClass = literalClass.getDeclaringClass();
		if(new DeclaringClassInclusionPolicy().isCandidateForInclusion(declaringLiteralClass))
			modelClass.setDeclaringClass(doParse(declaringLiteralClass, true));
	}

	private void processAnyImportedConfigurations(final Class<?> literalClass, ConfigurationClass modelClass) {
		// does this config class import any other config classes?
		Import importAnno = findAnnotation(literalClass, Import.class);
		if(importAnno != null)
			for(Class<?> classToImport : importAnno.value())
				modelClass.addImportedClass(doParse(classToImport, false));
	}

	private void processAnyImportedAspects(final Class<?> literalClass) {
		// does this configuration import any Aspect classes?
		Aspects importedAspects = findAnnotation(literalClass, Aspects.class);
		if(importedAspects == null)
			return;

		for(Class<?> aspectClass : importedAspects.value()) {
			Aspect aspectAnno = aspectClass.getAnnotation(Aspect.class);
			model.add(new AspectClass(aspectClass.getName(), aspectAnno));
		}
	}

	private void processSelfAsAspectIfAnnotated(final Class<?> literalClass) {
		// is this configuration also an @Aspect?
		Aspect aspectAnno = literalClass.getAnnotation(Aspect.class);
		if(aspectAnno != null)
			model.add(new AspectClass(literalClass.getName(), aspectAnno));
	}

	private void processAllMethods(final Class<?> literalClass, final ConfigurationClass modelClass) {
		// iterate through all the methods in the specified configuration class
		// looking for @Bean, @ExternalBean, etc.
		ReflectionUtils.doWithMethods(
			// for each method in this class
			literalClass,
			// execute this callback
			new MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        			processMethod(method, modelClass);
    			}

    		},
    		// but exclude all Object.* methods
    		new MethodFilter() {
    			public boolean matches(Method method) {
    				if(method.getDeclaringClass().equals(Object.class))
    					return false;
    				return true;
    			}
    		}
    	);
	}

	private void processMethod(Method method, ConfigurationClass modelClass) {
		processBeanMethod(method, modelClass);

		processExternalBeanMethod(method, modelClass);

		processAutoBeanMethod(method, modelClass);
	}

	private void processExternalBeanMethod(Method method, final ConfigurationClass modelClass) {
		ExternalBean metadata = findAnnotation(method, ExternalBean.class);
		if(metadata != null)
			modelClass.add(new ExternalBeanMethod(method.getName(), metadata, method.getModifiers()));
	}

	private void processBeanMethod(Method method, final ConfigurationClass modelClass) {
		Bean metadata = findAnnotation(method, Bean.class);
		if(metadata != null)
			modelClass.add(new BeanMethod(method.getName(), metadata, method.getModifiers()));
	}

	private void processAutoBeanMethod(Method method, final ConfigurationClass modelClass) {
		AutoBean metadata = findAnnotation(method, AutoBean.class);
		if(metadata != null)
			modelClass.add(new AutoBeanMethod(method.getName(),
		                                      metadata,
		                                      method.getReturnType().getName(),
		                                      method.getModifiers()));
	}

	private static class DeclaringClassInclusionPolicy {
		public boolean isCandidateForInclusion(Class<?> declaringClass) {
			if(declaringClass == null) return false;
			if(declaringClass.getName().endsWith("Test")) return false;
			if(declaringClass.getName().endsWith("Tests")) return false;
			return true;
		}
	}
}
