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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;

import org.springframework.config.java.enhancement.ConfigurationEnhancer;
import org.springframework.util.Assert;

/**
 * {@link ConfigurationEnhancer} implementation that uses CGLIB to subclass and
 * enhance a target
 * {@link org.springframework.config.java.annotation.Configuration} class.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 */
public class CglibConfigurationEnhancer implements ConfigurationEnhancer {

	private final SortedSet<JavaConfigMethodInterceptor> methodInterceptors = new TreeSet<JavaConfigMethodInterceptor>();

	public CglibConfigurationEnhancer() {
		registerMethodInterceptor(new NoOpMethodInterceptor());
	}

	public void registerMethodInterceptor(JavaConfigMethodInterceptor methodInterceptor) {
		methodInterceptors.add(methodInterceptor);
	}

	public <T> Class<? extends T> enhanceConfiguration(Class<T> configurationClass) {
		Assert.notNull(configurationClass, "configuration class required");

		final ArrayList<Class<? extends JavaConfigMethodInterceptor>> callbackTypes = new ArrayList<Class<? extends JavaConfigMethodInterceptor>>();
		for (JavaConfigMethodInterceptor methodInterceptor : methodInterceptors)
			callbackTypes.add(methodInterceptor.getClass());

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(configurationClass);
		enhancer.setUseFactory(false);
		enhancer.setCallbackFilter(new CallbackFilter() {
			public int accept(Method candidateMethod) {
				for (JavaConfigMethodInterceptor methodInterceptor : methodInterceptors)
					if (methodInterceptor.understands(candidateMethod))
						return callbackTypes.indexOf(methodInterceptor.getClass());

				throw new IllegalStateException(format("could not find a MethodInterceptor that understands method"
						+ " [%s]. Did you forget to register a catch-all interceptor?" + "Consider using %s",
						candidateMethod, NoOpMethodInterceptor.class.getSimpleName()));
			}
		});

		enhancer.setCallbackTypes(callbackTypes.toArray(new Class<?>[] {}));

		Class<?> configurationSubclass = enhancer.createClass();

		Enhancer.registerCallbacks(configurationSubclass, methodInterceptors.toArray(new Callback[] {}));

		return configurationSubclass.asSubclass(configurationClass);
	}

	/**
	 * Ensures class is suitable for later enhancement, throws an exception
	 * otherwise.
	 * 
	 * @param candidateConfigurationClass class to validate
	 * @throws IllegalArgumentException if <var>configurationClass</var> is
	 * declared final
	 */
	public static void validateSuitabilityForEnhancement(Class<?> configurationClass) {
		if (Modifier.isFinal(configurationClass.getModifiers()))
			throw new IllegalArgumentException(String.format(
					"%s is not enhanceable: classes may not be declared final", configurationClass.getName()));
	}
}
