package org.springframework.config.java.model;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;


public class ReflectiveJavaConfigurationModelPopulator {

	private final JavaConfigurationModel model;

	public ReflectiveJavaConfigurationModelPopulator(JavaConfigurationModel model) {
		this.model = model;
	}

	public void addToModel(Class<?> classLiteral) {
		ConfigurationClass configClass = new ConfigurationClass(classLiteral.getName());

		for(Method method : classLiteral.getDeclaredMethods()) {
			if(AnnotationUtils.findAnnotation(method, Bean.class) != null) {
				configClass.addBeanMethod(new BeanMethod(method.getName()));
			}
		}

		model.addConfigurationClass(configClass);
	}

}
