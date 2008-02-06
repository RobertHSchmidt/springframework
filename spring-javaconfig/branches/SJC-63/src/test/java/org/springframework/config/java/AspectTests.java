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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * This test showcases the most up-to-date techniques for using aspects within
 * Spring JavaConfig. Currently, the programming model is a bit awkward, perhaps
 * a lot awkward. See the method comments below for details. We'll want to
 * consider changing this before 1.0 GA.
 * 
 * @author Chris Beams
 */
public class AspectTests {
	/**
	 * Here we're going to create a new AppCtx against the
	 * {@link ConfigWithAspects} configuration, and prove that our advice gets
	 * applied properly.
	 */
	@Test
	public void testApplicationOfSimpleAspect() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(ConfigWithAspects.class);
		TestBean foo = context.getBean(TestBean.class);
		assertThat(foo.getName(), equalTo("foo"));

		ConfigWithAspects configBean = context.getBean(ConfigWithAspects.class);
		assertThat(configBean.beforeCount, equalTo(1));
		assertThat(configBean.afterCount, equalTo(1));

		foo.getName();

		assertThat(configBean.beforeCount, equalTo(2));
		assertThat(configBean.afterCount, equalTo(2));
	}

	/**
	 * Notice how the Configuration class is also an Aspect? This is strange,
	 * and I believe unintuitive/surprising for users of Spring AOP coming from
	 * the XML world. As a user, I would expect that I would define my Aspects
	 * and then somehow expose them as beans within my Configurations. In this
	 * example, the advice methods (logGetNameCall, logGetNameCalled) are
	 * defined inline with the Configuration. This is convenient, perhaps, but
	 * it shouldn't be the only way to do it.
	 */
	@Aspect
	@Configuration
	public static class ConfigWithAspects {

		Log logger = LogFactory.getLog(getClass());

		int beforeCount = 0;

		int afterCount = 0;

		@Before("getName(testBean)")
		public void logGetNameCall(TestBean testBean) {
			logger.info("about to call getName on " + testBean);
			beforeCount++;
		}

		@After("getName(testBean)")
		public void logGetNameCalled(TestBean testBean) {
			logger.info("just called getName on " + testBean);
			afterCount++;
		}

		@SuppressWarnings("unused")
		@Pointcut("execution(* *..TestBean.getName(..)) && target(testBean)")
		public void getName(TestBean testBean) {
		}

		@Bean
		public TestBean foo() {
			return new TestBean("foo");
		}
	}

	/**
	 * This test is all about demonstrating how to define a standalone, reusable
	 * aspect and then use it within a given Configuration. Trouble is, that
	 * we'll see that our 'standalone aspect' also has to be a Configuration.
	 */
	@Test
	public void testAspectModularity() {
		// instantiate a context against our AppConfig configuration. Remember
		// that AppConfig uses the @Import annotation to pull in our
		// 'standalone aspect' PropertyChangeTracker. Notice how
		// PropertyChangeTracker is a Configuration? Doesn't that seem strange?
		JavaConfigApplicationContext ctx;
		ctx = new JavaConfigApplicationContext(AppConfig.class);

		// grab out the aspect/configuration bean from the context, we'll
		// introspect it to see if the advice method executed in a moment
		PropertyChangeTracker changeTracker = ctx.getBean(PropertyChangeTracker.class);

		// before any calls to our service object's setCacheSize method, our
		// config beans' respective propertyChangeCount values should be at zero
		assertThat(changeTracker.propertyChangeCount, equalTo(0));

		// get our service object, and change a property on it.
		Service service = ctx.getBean(Service.class);
		service.setCacheSize(2500);

		// if our aspects were applied properly, the propertyChangeCount values
		// should now be incremented by one because we've changed a property
		// exactly once.
		assertThat(changeTracker.propertyChangeCount, equalTo(1));
	}

	// even though this class doesn't declare any advice methods, we still have
	// to mark it as @Aspect in order for any advice defined in other @Aspects
	// to be applied to the @Bean instances defined here. In a way, I suppose
	// this is like <aspectj:autoproxy/> in XML.
	@Aspect
	@Import(PropertyChangeTracker.class)
	@Configuration
	public static class AppConfig {
		Log log = LogFactory.getLog(getClass());

		int propertyChangeCount = 0;

		@Bean
		public Service service() {
			return new Service();
		}

	}

	// here's our 'standalone aspect', but it must be annotated as
	// @Configuration. That's clearly non-ideal, as we might want to apply
	// aspects that were defined from outside an SJC context.
	@Aspect
	@Configuration
	public static class PropertyChangeTracker {
		Log log = LogFactory.getLog(getClass());

		public int propertyChangeCount = 0;

		@Before("execution(* set*(*))")
		public void trackChange() {
			log.info("property just changed...");
			propertyChangeCount++;
		}
	}

	public static class Service {
		private int cacheSize = 100;

		public void setCacheSize(int cacheSize) {
			this.cacheSize = cacheSize;
		}

		public int getCacheSize() {
			return cacheSize;
		}
	}

}
