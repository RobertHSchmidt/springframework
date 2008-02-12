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
package org.springframework.config.java.enhancement.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

import org.springframework.config.java.core.EnhancerMethodInvoker;
import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.core.StandardBeanMethodProcessor;

/**
 * CGLIB MethodInterceptor that applies to methods on the configuration
 * instance.
 * 
 * Purpose: subclass configuration to ensure that singleton methods return the
 * same object on subsequent invocations, including self-invocation.
 * 
 * <p/> This implementation is thread-safe.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 */
public class BeanMethodMethodInterceptor implements JavaConfigMethodInterceptor {

	private final StandardBeanMethodProcessor beanMethodProcessor;

	public BeanMethodMethodInterceptor(ProcessingContext pc) {
		this.beanMethodProcessor = new StandardBeanMethodProcessor(pc);
	}

	public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {

		return returnWrappedResultMayBeCached(beanMethodProcessor.getBeanName(m), o, m, args, mp);
	}

	Object returnWrappedResultMayBeCached(final String beanName, final Object o, final Method m, final Object[] args,
			final MethodProxy mp) throws Throwable {

		synchronized (o) {
			return beanMethodProcessor.createNewOrGetCachedSingletonBean(beanName, new EnhancerMethodInvoker() {
				public Method getMethod() {
					return m;
				}

				public Object invokeOriginalClass() throws Throwable {
					return mp.invokeSuper(o, args);
				}
			});
		}
	}

	public boolean understands(Method candidateMethod) {
		return beanMethodProcessor.understands(candidateMethod);
	}

	public int getOrder() {
		return 200;
	}

	public int compareTo(JavaConfigMethodInterceptor that) {
		return this.getOrder() - that.getOrder();
	}
}