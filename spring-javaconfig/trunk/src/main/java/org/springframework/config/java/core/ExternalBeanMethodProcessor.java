/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.config.java.core;

import java.lang.reflect.Method;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.util.Assert;

public class ExternalBeanMethodProcessor implements BeanMethodProcessor {
	private final BeanFactory owningBeanFactory;

	private final BeanNamingStrategy namingStrategy;

	public ExternalBeanMethodProcessor(BeanFactory owningBeanFactory, BeanNamingStrategy namingStrategy) {
		Assert.notNull(owningBeanFactory, "owningBeanFactory is required");
		Assert.notNull(namingStrategy, "namingStrategy is required");

		this.owningBeanFactory = owningBeanFactory;
		this.namingStrategy = namingStrategy;
	}

	public Object processMethod(Method targetMethod) {
		ExternalBean externalBean = targetMethod.getAnnotation(ExternalBean.class);
		String beanName;

		if (externalBean != null && !"".equals(externalBean.value()))
			beanName = externalBean.value();
		else
			beanName = namingStrategy.getBeanName(targetMethod);

		return owningBeanFactory.getBean(beanName);
	}

	public static boolean isCandidate(Method candidateMethod) {
		return ClassUtils.hasAnnotation(candidateMethod, ExternalBean.class);
	}
}