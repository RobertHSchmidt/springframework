package org.springframework.config.java.model;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Import;




public class ReflectiveJavaConfigurationModelPopulator implements JavaConfigurationModelPopulator {

	private final JavaConfigurationModel model;

	public ReflectiveJavaConfigurationModelPopulator(JavaConfigurationModel model) {
		this.model = model;
	}

	public void addToModel(Object obj) {
		Class<?> classLiteral = (Class<?>) obj;
		ConfigurationClass configClass = new ConfigurationClass(classLiteral.getName());

		Import importAnno = findAnnotation(classLiteral, Import.class);
		if(importAnno != null)
			for(Class<?> classToImport : importAnno.value())
				addToModel(classToImport);

		for(Method method : classLiteral.getDeclaredMethods()) {
			if(findAnnotation(method, Bean.class) != null) {
				configClass.addBeanMethod(new BeanMethod(method.getName(), method.getModifiers()));
			}
		}

		model.addConfigurationClass(configClass);
	}

}
