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

package org.springframework.config.java;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.process.ConfigurationProcessor;

/**
 * Test multiple ExternalBean annotations that depends between each other.
 * 
 * @author Costin Leau
 * 
 */
public class ExternalBeanDependenciesTest extends TestCase {

	@Configuration
	static class FirstConfiguration {

		// beanA() depends on spouseB()
		@Bean
		public TestBean beanA() {
			TestBean bean = new TestBean("beanA");
			bean.setSpouse(spouseB());
			return bean;
		}

		@Bean
		public TestBean spouseA() {			
			return new TestBean("spouseA");
		}

		@ExternalBean
		public TestBean spouseB() {
			throw new IllegalArgumentException("method was not decorated");
		}
	}

	@Configuration
	static class SecondConfiguration {

		// beanB() depends on spouseA()
		@Bean
		public TestBean beanB() {
			TestBean bean = new TestBean("beanB");
			bean.setSpouse(spouseA());
			return bean;
		}

		@Bean
		public TestBean spouseB() {
			//System.out.println("creating spouseB ");
			return new TestBean("spouseB");
		}

		@ExternalBean
		public TestBean spouseA() {
			throw new IllegalArgumentException("method was not decorated");
		}
	}

	public void testCircularExternalBean() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);

		configurationProcessor.processClass(FirstConfiguration.class);
		configurationProcessor.processClass(SecondConfiguration.class);
		
		TestBean beanA = (TestBean) bf.getBean("beanA");
		TestBean beanB = (TestBean) bf.getBean("beanB");

		assertNotNull(beanA.getSpouse());
		assertNotNull(beanB.getSpouse());
		
		TestBean spouseA = (TestBean) bf.getBean("spouseA");
		TestBean spouseB = (TestBean) bf.getBean("spouseB");
		
		assertSame(spouseB, beanA.getSpouse());
		assertSame(spouseA, beanB.getSpouse());
	}

}
