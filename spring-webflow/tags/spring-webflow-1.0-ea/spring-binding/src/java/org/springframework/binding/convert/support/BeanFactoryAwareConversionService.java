/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.convert.support;

import java.beans.PropertyEditor;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.Converter;

/**
 * Default, local implementation of a conversion service deployable within a
 * Spring bean factory.
 * <p>
 * Acts as bean factory post processor, registering property editor adapters for
 * each supported conversion with a <code>java.lang.String sourceClass</code>.
 * This makes for very convenient use with the Spring container.
 * 
 * @author Keith Donald
 */
public class BeanFactoryAwareConversionService extends DefaultConversionService implements InitializingBean,
		BeanFactoryAware, BeanFactoryPostProcessor {

	/**
	 * The Spring Bean Factory.
	 */
	private BeanFactory beanFactory;

	public BeanFactoryAwareConversionService() {
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() {
		addConverter(new TextToBean(beanFactory), "bean");
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (getSourceClassConverters() != null) {
			Map sourceStringConverters = (Map)getSourceClassConverters().get(String.class);
			if (sourceStringConverters != null) {
				Iterator it = sourceStringConverters.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry)it.next();
					Class targetClass = (Class)entry.getKey();
					PropertyEditor editor = new ConverterPropertyEditorAdapter(new ConversionExecutor((Converter)entry
							.getValue(), targetClass));
					beanFactory.registerCustomEditor(targetClass, editor);
				}
			}
		}
	}
}