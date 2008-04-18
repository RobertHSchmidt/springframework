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
package org.springframework.config.java.model;

// import issues.MyConfig;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;

/**
 * Will eventually be an ASM-based implementation to be swapped in during the switchover from
 * reflection. See SJC-87.
 *
 * @see LegacyReflectingJavaConfigBeanDefinitionReader
 * @see ReflectingJavaConfigBeanDefinitionReader
 *
 * @author Chris Beams
 */
public class AsmJavaConfigBeanDefinitionReader extends AbstractJavaConfigBeanDefinitionReader {

	protected AsmJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}

	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		ConfigurationModel model = new ConfigurationModel();

		ConfigurationClass configClass = new ConfigurationClass("c");

		BeanMethod beanMethod = new BeanMethod("username");
		configClass.addBeanMethod(beanMethod);

		model.addConfigurationClass(configClass);

		BeanDefinitionRegisteringConfigurationModelRenderer modelToBeanGen =
			new BeanDefinitionRegisteringConfigurationModelRenderer(this.getRegistry());


		return modelToBeanGen.render(model);
	}

}