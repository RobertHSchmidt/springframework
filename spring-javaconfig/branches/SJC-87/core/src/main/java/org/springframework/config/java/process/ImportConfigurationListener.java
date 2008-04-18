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

import org.springframework.config.java.annotation.Import;

class ImportConfigurationListener extends ConfigurationListenerSupport {

	@Override
	public boolean understands(Class<?> configurationClass) {
		// TODO: does Import work with inheritance?
		return configurationClass.isAnnotationPresent(Import.class);
	}

	@Override
	public void configurationClass(ConfigurationProcessor configurationProcessor, String configurerBeanName,
			Class<?> configurationClass) {
		Import importAnnotation = configurationClass.getAnnotation(Import.class);
		Class<?>[] configurationClassesToImport = reverse(importAnnotation.value());
		for (Class<?> configurationClassToImport : configurationClassesToImport) {
			// duplicate check - process only if we've never encountered before
			if (!configurationProcessor.owningBeanFactory.containsBeanDefinition(configurationClassToImport.getName()))
				configurationProcessor.beanDefsGenerated += configurationProcessor
						.processClass(configurationClassToImport);
		}
	}

	/**
	 * Reverse the contents of <var>array</var>.
	 * 
	 * <p/>This method is used for reversing the order of classes passed into
	 * constructors of
	 * {@link org.springframework.config.java.context.LegacyJavaConfigApplicationContext}
	 * or {@link org.springframework.config.java.annotation.Import}.
	 * 
	 * <p/>TODO: shouldn't actually be necessary. Root out the real issue with
	 * ordering
	 * 
	 * @see org.springframework.config.java.context.LegacyJavaConfigApplicationContext#reverse()
	 * @param array - array to reverse
	 * @return reverse of <var>array</var>, null if <var>array</var> is null.
	 */
	private static Class<?>[] reverse(Class<?>[] array) {
		if (array == null)
			return array;

		int size = array.length;
		Class<?>[] reversed = new Class<?>[size];

		for (int i = 0; i < size; i++)
			reversed[size - i - 1] = array[i];

		return reversed;
	}

}
