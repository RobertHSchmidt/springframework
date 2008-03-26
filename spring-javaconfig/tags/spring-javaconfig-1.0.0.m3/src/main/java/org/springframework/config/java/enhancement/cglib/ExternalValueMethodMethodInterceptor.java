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

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.config.java.core.BeanMethodProcessor;
import org.springframework.config.java.valuesource.ValueResolutionException;

/**
 * CGLIB method interceptor for external property resolution methods.
 * 
 * <p/> This implementation is thread-safe.
 * 
 * @author Chris Beams
 * @author Rod Johnson
 */
class ExternalValueMethodMethodInterceptor implements MethodInterceptor {

	private final BeanMethodProcessor beanMethodProcessor;

	public ExternalValueMethodMethodInterceptor(BeanMethodProcessor beanMethodProcessor) {
		this.beanMethodProcessor = beanMethodProcessor;
	}

	public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
		try {
			return beanMethodProcessor.processMethod(m);
		}
		catch (ValueResolutionException ve) {
			// Look for a concrete implementation
			try {
				return mp.invokeSuper(o, args);
			}
			catch (AbstractMethodError ex) {
				// There was no implementation in the superclass
				throw ve;
			}
		}
	}
}
