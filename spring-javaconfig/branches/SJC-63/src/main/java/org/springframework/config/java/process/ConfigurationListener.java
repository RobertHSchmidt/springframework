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

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 * SPI interface that allows extension of a ConfigurationProcessor.
 * ConfigurationMethodListener instances are notified about the processing of
 * configuration classes and the processing of methods they contain.
 * Implementations should be thread safe.
 * 
 * @author Rod Johnson
 */
interface ConfigurationListener extends BeanMethodReturnValueProcessor {

	/**
	 * Does this configurer understand the given configuration class, which
	 * isn't a regular configuration class.
	 * @param configurerClass candidate configuration class
	 * @return whether this class is understood by this configurer
	 */
	boolean understands(Class<?> configurerClass);

	/**
	 * Help to process the return value of a bean definition.
	 * @param originallyCreatedBean
	 * @param method
	 * @param pf simply don't modify it if necessary
	 * @return whether or not the proxy was changed. If all listeners return
	 * false, the return value may not need to be proxied.
	 */
	boolean processBeanMethodReturnValue(BeanFactory childBeanFactory, Object originallyCreatedBean, Method method,
			ProxyFactory pf);

	void handleEvent(Reactor processor, Event event);

	void handleEvent(Reactor reactor, MethodEvent event);

	void handleEvent(Reactor reactor, BeanMethodEvent event);

	JavaConfigMethodInterceptor getMethodInterceptor(ProcessingContext pc);
}
