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
package org.springframework.config.java.aspects;

import static java.lang.String.format;
import static java.lang.System.identityHashCode;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * Thread-local storage for recording which beans' required methods have been
 * invoked. Used in conjunction with {@link RequiredMethodInvocationTracker}.
 *
 * Currently limited to recording methods annotated with
 * {@link Required @Required} (as opposed to Spring's ability to track
 * user-specified annotations. This has to do with limitations in AspectJ - it
 * is currently not possible to change the definition of a pointcut at runtime.
 *
 * @author Chris Beams
 */
class RequiredMethodInvocationRegistry extends ThreadLocal<Set<String>> {

	@Override
	protected Set<String> initialValue() {
		return new HashSet<String>();
	}

	/**
	 * Record the invocation of a {@link Required @Required} method for later
	 * interrogation.
	 *
	 * @see #interrogateRequiredMethods(Object, String)
	 *
	 * @param bean object instance being on which <var>methodName</var> is
	 * being invoked
	 * @param className declaring class for <var>methodName</var>
	 * @param methodName Required method currently being invoked
	 */
	public void registerMethodInvocation(Object bean, String className, String methodName) {
		get().add(qualifyMethodName(bean, className, methodName));
	}

	/**
	 * Interrogate any {@link Required @Required} methods within <var>bean</var>
	 * and determine whether they have been invoked.
	 *
	 * @param bean object to interrogate
	 * @param beanName id/name of <var>bean</var> within its respective
	 * BeanFactory
	 */
	public void interrogateRequiredMethods(final Object bean, final String beanName) {
		ReflectionUtils.doWithMethods(

		bean.getClass(),

		new MethodCallback() {
			public void doWith(Method requiredMethod) throws IllegalArgumentException, IllegalAccessException {
				if (!hasMethodBeenInvoked(bean, requiredMethod))
					throw new BeanInitializationException(format("Method '%s' is required for bean '%s'",
							requiredMethod.getName(), beanName));
			}
		},

		new ReflectionUtils.MethodFilter() {
			public boolean matches(Method candidate) {
				return !candidate.getDeclaringClass().equals(Object.class)
						&& (AnnotationUtils.findAnnotation(candidate, Required.class) != null);
			}
		}

		);
	}

	/**
	 * Remove all method invocation records from the registry.
	 */
	public void clear() {
		get().clear();
	}

	private boolean hasMethodBeenInvoked(Object bean, Method requiredMethod) {
		return get().contains(
				qualifyMethodName(bean, requiredMethod.getDeclaringClass().getName(), requiredMethod.getName()));
	}

	private String qualifyMethodName(Object bean, String className, String methodName) {
		return format("%s#%s#%s", identityHashCode(bean), className, methodName);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}