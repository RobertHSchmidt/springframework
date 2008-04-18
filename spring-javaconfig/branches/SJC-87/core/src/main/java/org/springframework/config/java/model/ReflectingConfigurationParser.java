package org.springframework.config.java.model;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Import;


/**
 * Parses configuration class literals using java reflection, writing the results
 * out to a {@link ConfigurationModel} object.
 *
 * @author Chris Beams
 */
public class ReflectingConfigurationParser implements ConfigurationParser {

	private final ConfigurationModel model;

	public ReflectingConfigurationParser(ConfigurationModel model) {
		this.model = model;
	}

	public void parse(Object configurationSource) {
		Class<?> classLiteral = (Class<?>) configurationSource;
		ConfigurationClass configClass = new ConfigurationClass(classLiteral.getName());

		Import importAnno = findAnnotation(classLiteral, Import.class);
		if(importAnno != null)
			for(Class<?> classToImport : importAnno.value())
				parse(classToImport);

		for(Method method : classLiteral.getDeclaredMethods()) {
			if(findAnnotation(method, Bean.class) != null) {
				configClass.addBeanMethod(new BeanMethod(method.getName(), method.getModifiers()));
			}
		}

		model.addConfigurationClass(configClass);
	}

}
