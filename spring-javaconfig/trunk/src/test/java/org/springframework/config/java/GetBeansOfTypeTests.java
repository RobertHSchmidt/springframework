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
package org.springframework.config.java;

import java.awt.Point;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Costin Leau
 * 
 */
public class GetBeansOfTypeTests extends TestCase {

	@Configuration
	public static class PropertiesConfig {
		@Bean
		public Properties properties() {
			Properties properties = new Properties();
			properties.put("metal", "militia");
			return properties;
		}

		@Bean
		public Map<?, ?> map() {
			Properties properties = new Properties();
			properties.put("2x", "4");
			return properties;
		}

		@Bean
		public Point point() {
			return new Point();
		}

	}

	@Configuration
	public static class AnotherConfig {
		@Bean
		public Map<String, String> anotherMap() {
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("re", "load");
			return attributes;
		}

		@Bean
		public IdentityHashMap<?, ?> parentMap() {
			return new IdentityHashMap<Object, Object>();
		}
	}

	public void testGetBeansOfType() throws Exception {
		ApplicationContext propertiesContext = new JavaConfigApplicationContext(PropertiesConfig.class);
		ApplicationContext anotherContext = new JavaConfigApplicationContext(propertiesContext, AnotherConfig.class);

		String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(anotherContext, Properties.class);
		assertEquals(2, names.length);
		names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(anotherContext, Map.class);
		assertEquals(4, names.length);

		// get beans only from the parent
		names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(anotherContext, Point.class);
		assertEquals(1, names.length);

		// get beans only from the child
		names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(anotherContext, IdentityHashMap.class);
		assertEquals(1, names.length);
	}

	public void testGetBeansFromJavaAndXml() throws Exception {
		JavaConfigApplicationContext propertiesContext = new JavaConfigApplicationContext(PropertiesConfig.class);
		ApplicationContext xmlCtx = new ClassPathXmlApplicationContext(
				new String[] { "org/springframework/config/java/simpleCtx.xml" }, propertiesContext);
		String names[] = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(xmlCtx, Point.class);
		assertEquals(2, names.length);
	}
}
