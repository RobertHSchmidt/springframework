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
package issues;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

class JavaConfigContextLoader implements org.springframework.test.context.ContextLoader {

	public ApplicationContext loadContext(String... locations) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] processLocations(Class<?> clazz, String... locations) {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * SJC-55 proposes an improved programming model for configuring aspects with
 * JavaConfig. These tests validate that new approach.
 * 
 * @author Chris Beams
 */
@ContextConfiguration(loader = JavaConfigContextLoader.class)
public class Sjc55Tests {
	@Aspect
	public static class MyAspect {
		private int count = 0;

		@Before("execution(* *(..))")
		public void logSomething() {
			count++;
		}

		public int getCount() {
			return count;
		}
	}

	@Configuration
	static class AspectJAutoProxyConfiguration {

		@Bean
		// (aliases = { AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME })
		public AnnotationAwareAspectJAutoProxyCreator autoprox() {
			AnnotationAwareAspectJAutoProxyCreator creator = new AnnotationAwareAspectJAutoProxyCreator();
			creator.setProxyTargetClass(true);
			return creator;
		}

	}

	@Target( { ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@interface AspectJAutoProxy {

		String[] include();

		boolean proxyTargetClass() default false;

	}

	@Import(AspectJAutoProxyConfiguration.class)
	@Configuration
	@AspectJAutoProxy(proxyTargetClass = true, include = { "aspect1", "aspect2" })
	static class MyConfig { // extends AspectJAutoProxyConfiguration {
		@Bean
		public TestBean foo() {
			return new TestBean("foo");
		}

		@Bean
		public MyAspect someAspect() {
			return new MyAspect();
		}

		// @Bean
		public AnnotationAwareAspectJAutoProxyCreator autoprox23() {
			AnnotationAwareAspectJAutoProxyCreator creator = new AnnotationAwareAspectJAutoProxyCreator();
			creator.setProxyTargetClass(true);
			return creator;
		}

	}

	// TODO: work in progress
	@Ignore
	@Test
	public void test() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(MyConfig.class);
		TestBean foo = ctx.getBean(TestBean.class);
		MyAspect aspect = ctx.getBean(MyAspect.class);

		// the call to getName() below should trigger our aspect
		assertThat("foo", equalTo(foo.getName()));

		assertThat(aspect.getCount(), equalTo(1));
	}

	/**
	 * Of course, configuring our aspect via XML works fine.
	 */
	@Test
	public void validateAspectWithXmlConfig() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("issues/Sjc55.xml");
		TestBean foo = (TestBean) ctx.getBean("foo");
		MyAspect aspect = (MyAspect) ctx.getBean("someAspect");

		// should trigger our aspect
		assertThat("foo", equalTo(foo.getName()));

		assertThat(aspect.getCount(), equalTo(1));
	}
}
