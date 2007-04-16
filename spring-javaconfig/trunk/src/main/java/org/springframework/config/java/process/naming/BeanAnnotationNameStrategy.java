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
package org.springframework.config.java.process.naming;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Simple strategy returning the
 * @Bean name property for the creating method.
 * 
 * @see Bean
 * @author Costin Leau
 * 
 */
public class BeanAnnotationNameStrategy implements BeanNamingStrategy {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.config.java.process.naming.BeanNamingStrategy#getBeanName(java.lang.reflect.Method,
	 * org.springframework.config.java.annotation.Configuration)
	 */
	public String getBeanName(Method beanCreationMethod, Configuration configuration) {
		Assert.notNull(beanCreationMethod);
		Bean bean = beanCreationMethod.getAnnotation(Bean.class);
		// return null if no annotation is found to let other strategies kick in
		return (bean != null && StringUtils.hasText(bean.name()) ? bean.name() : null);
	}
}
