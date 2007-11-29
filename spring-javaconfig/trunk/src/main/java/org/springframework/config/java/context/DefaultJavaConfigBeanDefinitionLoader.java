/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.config.java.context;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.web.JavaConfigWebApplicationContext;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Helper class designed to abstract out the common bean definition loading
 * logic shared by {@link JavaConfigApplicationContext} and
 * {@link JavaConfigWebApplicationContext}
 * 
 * @see JavaConfigApplicationContext
 * @see JavaConfigWebApplicationContext
 * 
 * @author Chris Beams
 */
public class DefaultJavaConfigBeanDefinitionLoader implements JavaConfigBeanDefinitionLoader {

	/** We delegate to Spring 2.5 and above class scanning support. */
	private final ClassPathScanningCandidateComponentProvider scanner;

	private final Set<Class<?>> configClasses;

	private final Set<String> basePackages;

	/**
	 * Create a new instance capable of loading bean definitions from both
	 * configuration classes in <var>configClasses</var> and by scanning the
	 * classpath for any Configuration classes in <var>basePackges</var>
	 * 
	 * @param loader - usually a JavaConfig[Web]ApplicationContext
	 * @param configClasses - the JavaConfigApplicationContext's set of
	 * configuration classes
	 * @param basePackages - the JavaConfigApplicationContext's set of base
	 * packages to scan
	 * 
	 * @see JavaConfigApplicationContext#initBeanDefinitionLoader()
	 */
	public DefaultJavaConfigBeanDefinitionLoader(ResourceLoader loader, Set<Class<?>> configClasses,
			Set<String> basePackages) {
		this.configClasses = configClasses;
		this.basePackages = basePackages;
		scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		scanner.setResourceLoader(loader);
	}

	/**
	 * Find all bean definitions within {@link #configClasses} and
	 * {@link #basePackages} and register them with <var>beanFactory</var>.
	 * 
	 * @see JavaConfigApplicationContext#loadBeanDefinitions(DefaultListableBeanFactory)
	 */
	public void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		for (Class<?> cz : configClasses) {
			if (ClassUtils.isConfigurationClass(cz)) {
				beanFactory.registerBeanDefinition(cz.getName(), new RootBeanDefinition(cz, true));
			}
		}

		for (String location : basePackages) {
			Set<BeanDefinition> beandefs = scanner.findCandidateComponents(location);
			for (BeanDefinition bd : beandefs) {
				beanFactory.registerBeanDefinition(bd.getBeanClassName(), bd);
			}
		}
	}

}
