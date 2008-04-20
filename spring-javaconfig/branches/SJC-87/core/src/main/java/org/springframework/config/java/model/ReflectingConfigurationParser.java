package org.springframework.config.java.model;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;

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
		model.add(doParse(classLiteral));
	}

	private ConfigurationClass doParse(Class<?> classLiteral) {
		ConfigurationClass configClass =
			new ConfigurationClass(classLiteral.getName(), classLiteral.getModifiers());

		Import importAnno = findAnnotation(classLiteral, Import.class);
		if(importAnno != null)
			for(Class<?> classToImport : importAnno.value())
				configClass.addImportedClass(doParse(classToImport));

		for(Method method : classLiteral.getDeclaredMethods()) {
			Bean bean = findAnnotation(method, Bean.class);
			if(bean != null)
				configClass.add(new BeanMethod(method.getName(), bean, method.getModifiers()));

			ExternalBean extBean = findAnnotation(method, ExternalBean.class);
			if(extBean != null)
				configClass.add(new ExternalBeanMethod(method.getName(), extBean, method.getModifiers()));
		}

		return configClass;
	}

}
