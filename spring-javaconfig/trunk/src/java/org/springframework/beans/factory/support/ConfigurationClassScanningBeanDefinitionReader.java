/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * ASM {@link http://asm.objectweb.org} based annotationName bean definition
 * reader. This implementation will read the bytecode directly to find more
 * information about each class read. ASM will be used as much as possible in
 * order to unnecessary class loading.
 * 
 * @author Costin Leau
 * @author Rod Johnson
 */
public class ConfigurationClassScanningBeanDefinitionReader extends AbstractAsmClassScanningBeanDefinitionReader {
	
	public ConfigurationClassScanningBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		super(beanFactory);
		addTypeFilter(new AnnotationTypeFilter(Configuration.class));
	}

	protected void processComponentOrFactoryClass(Class clazz) {
		System.out.println(clazz);
		BeanDefinition definition = new RootBeanDefinition(clazz);
		getBeanFactory().registerBeanDefinition(clazz.getName(), definition);
	}
}
