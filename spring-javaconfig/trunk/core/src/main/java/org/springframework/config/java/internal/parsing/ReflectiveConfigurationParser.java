package org.springframework.config.java.internal.parsing;

import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.annotation.aop.Aspects;
import org.springframework.config.java.internal.model.AspectClass;
import org.springframework.config.java.internal.model.AutoBeanMethod;
import org.springframework.config.java.internal.model.BeanMethod;
import org.springframework.config.java.internal.model.ConfigurationClass;
import org.springframework.config.java.internal.model.ConfigurationModel;
import org.springframework.config.java.internal.model.ExternalBeanMethod;
import org.springframework.config.java.internal.model.ExternalValueMethod;
import org.springframework.config.java.internal.model.NonJavaConfigMethod;
import org.springframework.config.java.internal.model.PotentialConfigurationClass;
import org.springframework.config.java.internal.util.Constants;
import org.springframework.config.java.internal.util.AnnotationExtractionUtils.AnnotationFilter;
import org.springframework.config.java.model.ModelClass;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;


/**
 * Parses configuration class literals using java reflection, writing the results
 * out to a {@link ConfigurationModel} object.
 *
 * @author Chris Beams
 */
public class ReflectiveConfigurationParser implements ConfigurationParser {

	private static final Log logger = LogFactory.getLog(ReflectiveConfigurationParser.class);

	private final ConfigurationModel model;

	private final Stack<Class<?>> importStack = new Stack<Class<?>>();

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

		processAnyResourceBundles(literalClass, modelClass);

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
		if(importAnno == null)
			return;

		if(importStack.contains(literalClass))
			throw new CircularImportException(literalClass, importStack);

		importStack.push(literalClass);

		for(Class<?> classToImport : importAnno.value())
			modelClass.addImportedClass(doParse(classToImport, false));

		importStack.pop();
	}

	private void processAnyResourceBundles(Class<?> literalClass, ConfigurationClass modelClass) {
		ResourceBundles resourceBundles = literalClass.getAnnotation(ResourceBundles.class);
		if(resourceBundles != null)
			modelClass.add(resourceBundles);
	}

	private void processAnyImportedAspects(final Class<?> literalClass) {
		// does this configuration import any @Aspect classes?
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
		Annotation[] annotations = getJavaConfigAnnotations(method);
		ModelClass returnType = ModelClass.forClass(method.getReturnType());

		if(BeanMethod.identifyAsBeanMethod(annotations))
			modelClass.add(new BeanMethod(method.getName(), method.getModifiers(), annotations));

		else if(ExternalBeanMethod.identifyAsExternalBeanMethod(annotations))
			modelClass.add(new ExternalBeanMethod(method.getName(), method.getModifiers(), annotations));

		else if(ExternalValueMethod.identifyAsExternalValueMethod(annotations))
			modelClass.add(new ExternalValueMethod(method.getName(), method.getModifiers(), annotations));

		else if(AutoBeanMethod.identifyAsExternalBeanMethod(annotations))
			modelClass.add(new AutoBeanMethod(method.getName(), returnType, method.getModifiers(), annotations));

		else
			modelClass.add(new NonJavaConfigMethod(method.getName(), method.getModifiers(), annotations));
	}

	/**
	 * Returns an array of all framework-specific annotations on <var>method</var>.
	 * Considers only those annotations that are a) directly specified on <var>method</var>
	 * or, in the case of Inherited annotations, those that are annotated on a
	 * superclass/interface implementation of <var>method</var>.
	 */
	private Annotation[] getJavaConfigAnnotations(Method method) {
		return findAnnotations(method, new AnnotationFilter() {
			public boolean accept(Annotation candidate) {
				return candidate.annotationType().getName().startsWith(Constants.JAVA_CONFIG_PKG);
			}
		});
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
