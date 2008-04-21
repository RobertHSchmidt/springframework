package org.springframework.config.java.model;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Import;


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

		if(isUserSpecified)
			modelClass = new ConfigurationClass(literalClass.getName(), literalClass.getModifiers());
		else
			modelClass = new PotentialConfigurationClass(literalClass.getName(), literalClass.getModifiers());

		Class<?> declaringLiteralClass = literalClass.getDeclaringClass();
		if(new DeclaringClassInclusionPolicy().isCandidateForInclusion(declaringLiteralClass))
			modelClass.setDeclaringClass(doParse(declaringLiteralClass, false));

		Import importAnno = findAnnotation(literalClass, Import.class);
		if(importAnno != null)
			for(Class<?> classToImport : importAnno.value())
				modelClass.addImportedClass(doParse(classToImport, true));

		for(Method method : literalClass.getDeclaredMethods()) {
			Bean bean = findAnnotation(method, Bean.class);
			if(bean != null)
				modelClass.add(new BeanMethod(method.getName(), bean, method.getModifiers()));

			ExternalBean extBean = findAnnotation(method, ExternalBean.class);
			if(extBean != null)
				modelClass.add(new ExternalBeanMethod(method.getName(), extBean, method.getModifiers()));
		}

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
