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

package org.springframework.config.java.listener.aop.targetsource;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.targetsource.HotSwappable;
import org.springframework.config.java.listener.ConfigurationListenerSupport;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * ConfigurationListener implementations that understands classes for hot
 * swapping.
 * 
 * @author Rod Johnson
 */
public class HotSwapConfigurationListener extends ConfigurationListenerSupport {

	private List<Method> hotswapMethods = new LinkedList<Method>();

	@Override
	public int beanCreationMethod(BeanDefinitionRegistration beanDefinitionRegistration, ConfigurationProcessor cp,
			String configurerBeanName, Class<?> configurerClass, Method m, Bean beanAnnotation) {
		if (AnnotationUtils.findAnnotation(m, HotSwappable.class) != null) {
			hotswapMethods.add(m);
		}

		return 0;
	}

	@Override
	public boolean processBeanMethodReturnValue(ConfigurationProcessor cp, Object originallyCreatedBean, Method method,
			ProxyFactory pf) {
		if (hotswapMethods.contains(method)) {
			HotSwappableTargetSource hsts = new HotSwappableTargetSource(originallyCreatedBean);
			pf.setTargetSource(hsts);
			return true;
		}
		return false;
	}

}
