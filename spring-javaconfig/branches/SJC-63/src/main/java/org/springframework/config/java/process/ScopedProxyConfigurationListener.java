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
package org.springframework.config.java.process;

import java.lang.reflect.Method;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.core.ScopedProxyMethodProcessor;
import org.springframework.config.java.util.DefaultScopes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * Configuration listener for &#64;ScopedProxy annotation. Similar in
 * functionality with
 * {@link org.springframework.aop.config.ScopedProxyBeanDefinitionDecorator}
 * 
 * @author Costin Leau
 */
class ScopedProxyConfigurationListener extends ConfigurationListenerSupport {

	@Override
	public void handleEvent(Reactor reactor, BeanMethodEvent event) {
		Method method = event.method;
		Bean beanAnnotation = AnnotationUtils.findAnnotation(method, Bean.class);
		ProcessingContext pc = event.processingContext;

		ScopedProxy proxyAnnotation = method.getAnnotation(ScopedProxy.class);
		if (proxyAnnotation != null) {
			// do some validation first related to scoping
			String scope = beanAnnotation.scope();
			if (DefaultScopes.PROTOTYPE.equals(scope) || DefaultScopes.SINGLETON.equals(scope))
				throw new BeanDefinitionStoreException(String.format(
						"[%s] contains an invalid annotation declaration: @ScopedProxy "
								+ "cannot be used on a singleton/prototype bean", method));

			pc.beanDefsGenerated++;
			// TODO: could the code duplication be removed?
			// copied from ScopedProxyBeanDefinitionDecorator

			String originalBeanName = event.name;
			RootBeanDefinition targetDefinition = event.rbd;

			// Create a scoped proxy definition for the original bean name,
			// "hiding" the target bean in an internal target definition.
			String targetBeanName = ScopedProxyMethodProcessor.resolveHiddenScopedProxyBeanName(originalBeanName);
			RootBeanDefinition scopedProxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
			scopedProxyDefinition.getPropertyValues().addPropertyValue("targetBeanName", targetBeanName);

			if (proxyAnnotation.proxyTargetClass()) {
				targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
				// ScopedFactoryBean's "proxyTargetClass" default is TRUE, so we
				// don't need to set it explicitly here.
			}
			else {
				scopedProxyDefinition.getPropertyValues().addPropertyValue("proxyTargetClass", Boolean.FALSE);
			}

			// The target bean should be ignored in favor of the scoped proxy.
			targetDefinition.setAutowireCandidate(false);

			// Register the target bean as separate bean in the factory
			pc.registerBeanDefinition(targetBeanName, targetDefinition, event.hide);

			// replace the original bean definition with the target one
			event.rbd = scopedProxyDefinition;
		}
	}

	@Override
	public void handleEvent(Reactor reactor, MethodEvent event) {
		Method method = event.method;

		// catch invalid declarations
		if (method.isAnnotationPresent(ScopedProxy.class)
				&& (AnnotationUtils.findAnnotation(event.method, Bean.class) == null))
			throw new BeanDefinitionStoreException(String.format(
					"[%s] contains an invalid annotation declaration: @ScopedProxy "
							+ "should be used along side @Bean, not by itself", method));
	}

	@Override
	public boolean understands(Class<?> configurerClass) {
		Assert.notNull(configurerClass);
		return configurerClass.isAnnotationPresent(Configuration.class);
	}

}
