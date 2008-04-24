package org.springframework.config.java.model;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 *
 * @author Chris Beams
 */
public class ReflectingConfigurationParser implements ConfigurationParser {

	private static final Log log = LogFactory.getLog(ReflectingConfigurationParser.class);

	private final ConfigurationModel model;

	public ReflectingConfigurationParser(ConfigurationModel model) {
		this.model = model;
	}

	/**
	 * Parse <var>configurationSource</var>, populating <tt>model</tt>.
	 *
	 * @param configurationSource class literal to be parsed via reflection
	 */
	public void parse(Object configurationSource) {
		Class<?> classLiteral = (Class<?>) configurationSource;
		model.add(doParse(classLiteral, true));
	}

	/**
	 *
	 * @param literalClass
	 * @param isUserSpecified
	 * @return
	 */
	private ConfigurationClass doParse(Class<?> literalClass, boolean isUserSpecified) {
		final ConfigurationClass modelClass;
		final String className = literalClass.getName();
		final Configuration metadata = AnnotationUtils.findAnnotation(literalClass, Configuration.class);
		final int modifiers = literalClass.getModifiers();

		if(isUserSpecified)
			if(metadata == null)
				modelClass = new ConfigurationClass(className, modifiers);
			else
				modelClass = new ConfigurationClass(className, metadata, modifiers);
		else
			if(metadata == null)
				modelClass = new PotentialConfigurationClass(className, modifiers);
			else
				modelClass = new PotentialConfigurationClass(className, metadata, modifiers);

		Class<?> declaringLiteralClass = literalClass.getDeclaringClass();
		if(new DeclaringClassInclusionPolicy().isCandidateForInclusion(declaringLiteralClass))
			modelClass.setDeclaringClass(doParse(declaringLiteralClass, false));

		Import importAnno = findAnnotation(literalClass, Import.class);
		if(importAnno != null)
			for(Class<?> classToImport : importAnno.value())
				modelClass.addImportedClass(doParse(classToImport, true));

		ReflectionUtils.doWithMethods(
			// for each method in this class
			literalClass,
			// execute this callback
			new MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        			Bean bean = findAnnotation(method, Bean.class);
        			if(bean != null)
        				modelClass.add(new BeanMethod(method.getName(), bean, method.getModifiers()));

        			ExternalBean extBean = findAnnotation(method, ExternalBean.class);
        			if(extBean != null)
        				modelClass.add(new ExternalBeanMethod(method.getName(), extBean, method.getModifiers()));
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

		return modelClass;
	}

}

class DeclaringClassInclusionPolicy {
	public boolean isCandidateForInclusion(Class<?> declaringClass) {
		if(declaringClass == null) return false;
		if(declaringClass.getName().endsWith("Test")) return false;
		if(declaringClass.getName().endsWith("Tests")) return false;
		return true;
	}
}
