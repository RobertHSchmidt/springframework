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

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HiddenBeansAutowiringTests {

	protected ApplicationContext createContext() throws Exception {
		return new JavaConfigApplicationContext(MyConfig.class) {
			@Override
			protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
				// need to register processor via definition to get
				// bean-creation callback(s)

				// We need to add this as a post processor, not just as a bean
				// definition
				AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
				beanFactory.initializeBean(autowiredAnnotationBeanPostProcessor, null);
				beanFactory.addBeanPostProcessor(autowiredAnnotationBeanPostProcessor);
			}
		};
	}

	private static void assertAutowired(MyBeanHolder holder) {
		Assert.assertNotNull(holder);
		Assert.assertNotNull(holder.bean);
		Assert.assertNotNull("Bean must have been autowired", holder.bean.autoWiredWidget);
	}

	@Test
	public void testAutowiringProtected() throws Exception {
		ApplicationContext context = createContext();
		assertAutowired((MyBeanHolder) context.getBean("holderForProtected"));
	}

	@Test
	public void testAutowiringPublic() throws Exception {
		ApplicationContext context = createContext();
		assertAutowired((MyBeanHolder) context.getBean("holderForPublic"));
	}

	@Test
	public void testAutowiringPublicWithXml() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/config/java/hiddenBeansWithAutowire.xml");
		assertAutowired((MyBeanHolder) context.getBean("holderForPublic"));
	}

	// TODO fixme
	@Ignore
	@Test
	public void testAutowiringProtectedWithXml() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/config/java/hiddenBeansWithAutowire.xml");
		assertAutowired((MyBeanHolder) context.getBean("holderForProtected"));
	}

	public static class Widget {
	}

	public static class MyBean {
		@Autowired
		private Widget autoWiredWidget;
	}

	public static class MyBeanHolder {
		private MyBean bean;

		public MyBeanHolder(MyBean bean) {
			this.bean = bean;
		}
	}

	@Configuration
	public static class MyConfig {
		@Bean
		public Widget widget() {
			return new Widget();
		}

		@Bean
		public MyBeanHolder holderForProtected() {
			return new MyBeanHolder(protectedBean());
		}

		@Bean
		public MyBeanHolder holderForPublic() {
			return new MyBeanHolder(publicBean());
		}

		@Bean
		public MyBean publicBean() {
			return new MyBean();
		}

		@Bean
		protected MyBean protectedBean() {
			return new MyBean();
		}
	}
}
