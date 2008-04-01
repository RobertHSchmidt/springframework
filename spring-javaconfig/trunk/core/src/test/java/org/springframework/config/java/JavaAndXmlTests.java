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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * Test for http://opensource.atlassian.com/projects/spring/browse/SJC-13.
 */
@ContextConfiguration(locations = { "classpath:org/springframework/config/java/javaandxml.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class JavaAndXmlTests {

	private TestBean root;

	private TestBean dependency;

	public static class TestBean {
		private List<?> dependencies;

		private String name;

		public TestBean(String pName, List<?> pDependencies) {
			name = pName;
			dependencies = pDependencies;
		}

		public List<?> getDependencies() {
			return dependencies;
		}

		public void setDependencies(List<?> pDependencies) {
			dependencies = pDependencies;
		}

		public String getName() {
			return name;
		}

		public void setName(String pName) {
			name = pName;
		}
	}

	@Configuration
	public static class TestConfiguration implements BeanFactoryAware {
		private BeanFactory beanFactory;

		@Bean
		public TestBean root() {
			return new TestBean("root", (List<?>) beanFactory.getBean("root-dependencies"));
		}

		@Bean
		public TestBean dependency() {
			return new TestBean("dependency", Collections.EMPTY_LIST);
		}

		public void setBeanFactory(BeanFactory pBeanFactory) {
			beanFactory = pBeanFactory;
		}
	}

	@Test
	public void testJavaConfiguration() {
		assertNotNull(root);

		assertEquals("root", root.getName());
		assertEquals(Arrays.asList(new TestBean[] { dependency, }), root.getDependencies());

		assertNotNull(dependency);
	}

	public TestBean getDependency() {
		return dependency;
	}

	@Resource
	public void setDependency(TestBean pDependency) {
		dependency = pDependency;
	}

	public TestBean getRoot() {
		return root;
	}

	@Resource
	public void setRoot(TestBean pRoot) {
		root = pRoot;
	}
}
