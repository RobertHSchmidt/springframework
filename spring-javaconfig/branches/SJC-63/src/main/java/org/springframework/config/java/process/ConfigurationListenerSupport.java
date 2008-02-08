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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * Convenient base class for implementation of the ConfigurationListener
 * interface, offer no op implementations of all methods.
 * 
 * @author Rod Johnson
 * @author Chris Beams
 */
public abstract class ConfigurationListenerSupport implements ConfigurationListener {

	protected final Log log = LogFactory.getLog(getClass());

	protected ProcessingContext getProcessingContext() {
		return ProcessingContext.getCurrentContext();
	}

	public boolean understands(Class<?> configurerClass) {
		return false;
	}

	public boolean processBeanMethodReturnValue(BeanFactory childFactory, Object originallyCreatedBean, Method method,
			ProxyFactory pf) {
		return false;
	}

	public void handleEvent(Reactor reactor, MethodEvent event) {
	}

	public void handleEvent(Reactor reactor, BeanMethodEvent event) {
	}

	public void handleEvent(final Reactor reactor, final Event event) {
		// TODO Auto-generated method stub

		log.info("got event: " + event);

		ReflectionUtils.doWithMethods(this.getClass(), new MethodCallback() {

			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				if (method.getDeclaringClass().equals(Object.class))
					return;
				log.warn("method: " + method);
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length != 2)
					return;
				log.warn(event.getClass());
				if (parameterTypes[0].isAssignableFrom(reactor.getClass())
						&& parameterTypes[1].equals(event.getClass())) {
					try {
						method.invoke(ConfigurationListenerSupport.this, reactor, event);
						return;
					}
					catch (InvocationTargetException ex) {
						throw new RuntimeException(ex);
					}
				}

			}

		});
	}

}
