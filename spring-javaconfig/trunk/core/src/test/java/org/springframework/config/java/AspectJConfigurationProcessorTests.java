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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.DependsOnTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.DependencyCheck;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.model.MalformedJavaConfigurationException;
import org.springframework.config.java.model.ValidationError;
import org.springframework.config.java.util.DefaultScopes;

/**
 * @author Rod Johnson
 * @author Chris Beams
 */
public class AspectJConfigurationProcessorTests {

	private ConfigurableJavaConfigApplicationContext ctx;

	@org.junit.After
	public void nullOutContext() { ctx = null; }


	// TODO: {bean scoping, aop}
	// this may not be a valid test. Would need prototype aspect bean and autoproxy
	// see also: SpringAopConfigurationProcessorTests#testPerInstanceAdviceAndSharedAdvice(),
	//           which is a duplicate of this method
	@Ignore
	public @Test void testPerInstanceAdviceAndSharedAdvice() throws Exception {
		// NOTE: didn't work with LegacyJCAC, either
		ctx = new JavaConfigApplicationContext(PerInstanceCountingAdvice.class);

		TestBean advised1 = ctx.getBean(TestBean.class, "advised");
		Object target1 = ((Advised) advised1).getTargetSource().getTarget();
		TestBean advised2 = ctx.getBean(TestBean.class, "advised");

		// Hashcode works on this
		advised2.setAge(35);

		assertNotSame(advised1, advised2);
		Object target2 = ((Advised) advised2).getTargetSource().getTarget();
		assertNotSame(target1, target2);

		assertEquals("advised", ctx.getBeanNamesForType(TestBean.class)[0]);

		assertEquals(0, CountingConfiguration.getCount(target1));
		advised1.absquatulate();
		assertEquals(0, CountingConfiguration.getCount(target1));
		advised1.getSpouse();
		assertEquals(1, CountingConfiguration.getCount(target1));
		assertEquals(0, CountingConfiguration.getCount(target2));

		advised2.getSpouse();
		assertEquals(1, CountingConfiguration.getCount(target1));
		assertEquals(1, CountingConfiguration.getCount(target2));
	}
	@Aspect
	public abstract static class PerInstanceCountingAdvice extends CountingConfiguration {
		@Before("execution(* *.getSpouse())")
		public void doesntMatter() { }
	}


	// XXX: [aop]
	public @Test void testSharedAfterAdvice() throws Throwable {
		ctx = new JavaConfigApplicationContext(AfterAdvice.class);

		TestBean advised = ctx.getBean(TestBean.class, "advised");
		AfterAdvice.count = 0;
		advised.absquatulate();
		assertEquals(0, AfterAdvice.count);
		advised.exceptional(null);
		assertEquals(1, AfterAdvice.count);
		try { advised.exceptional(new Exception()); }
		catch (Throwable t) { /* Expected */ }
		assertEquals("After advice should count failure", 2, AfterAdvice.count);
	}
	@Aspect
	public static class AfterAdvice extends CountingConfiguration {
		public static int count = 0;

		@After("execution(* *.exceptional(Throwable))")
		public void after() { ++count; }
	}


	/**
	 * Integration test proving that an invalid aspect (i.e.: one without an Aspect
	 * annotation) causes an appropriate error upon validation
	 * <p>
	 * local classes are used because the runtime will never get to the point of
	 * attempting to subclass them with CGLIB (which would fail)
	 */
	// XXX: [aop]
	@Test(expected = MalformedJavaConfigurationException.class)
	public void testAspectJAnnotationsRequireExplicitLocalAspectAnnotation() throws Exception {
    	/** Invalid class, doesn't have an Aspect tag */
    	class InvalidNoAspectAnnotation {
    		@Around("execution(* *.getName())")
    		public Object invalid() throws Throwable { return "around"; }
    	}
    	class Config { public @Bean TestBean alice() { return new TestBean(); } }

    	try {
    		ctx = new JavaConfigApplicationContext();
    		ctx.addConfigClass(Config.class);
    		ctx.addAspectClasses(InvalidNoAspectAnnotation.class);
    		ctx.refresh();
    	} catch (MalformedJavaConfigurationException ex) {
    		assertTrue(ex.getMessage().contains(ValidationError.ASPECT_CLASS_MUST_HAVE_ASPECT_ANNOTATION.toString()));
    		throw ex;
    	}
	}

	/**
	 * If an Aspect class is passed to the JavaConfigApplicationContext constructor
	 * it will not be recognized as an Aspect.  The constructor considers all Class
	 * arguments to be Configuration classes.
	 */
	@Test(expected=MalformedJavaConfigurationException.class)
	public void testModelIsInvalidWithOnlyOneAspect() {
		try {
			ctx = new JavaConfigApplicationContext(ValidAspectAnnotation.class);
		}
		catch (MalformedJavaConfigurationException ex) {
			assertTrue(ex.getMessage().contains(ValidationError.CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN.toString()));
			throw ex;
		}
	}
	/**
	 * Technically a valid Aspect, but it is supplied to the constructor
	 * above, which is incorrect usage
	 */
	@Aspect
	public static class ValidAspectAnnotation {
		@Around("execution(* *.getName())")
		public Object invalid() throws Throwable {
			return "around";
		}
	}


	// XXX: [aop]
	@Test(expected = MalformedJavaConfigurationException.class)
	public void testInvalidInheritanceFromConcreteAspect() throws Exception {
		// should throw, cannot extend a concrete aspect
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext();
		ctx.addConfigClasses(ValidConfigClass.class);
		ctx.addAspectClasses(InvalidInheritanceFromConcreteAspect.class);
		ctx.refresh();
	}
	public static class ValidConfigClass { public @Bean TestBean alice() { return new TestBean(); } }
	public static class InvalidInheritanceFromConcreteAspect extends AroundSingletonCountingAdvice { }


	// XXX: [aop]
	public @Test void testAspectJAroundAdviceWithImplicitScope() throws Exception {
		ctx = new JavaConfigApplicationContext(AroundSingletonCountingAdvice.class);

		TestBean advised1 = (TestBean) ctx.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("around", advised1.getName());
	}
	@Aspect
	public static class AroundSingletonCountingAdvice extends AbstractSingletonCountingAdvice {
		@Around("execution(* *.getName())")
		public Object newValue() throws Throwable {
			return "around";
		}
	}


	// XXX: [aop]
	public @Test void testAspectJAroundAdviceWithImplicitScopeAndNamedPointcut() throws Exception {
		doTestAspectJAroundAdviceWithImplicitScope(AroundAdviceWithNamedPointcut.class);
	}
	@Aspect
	public static class AroundAdviceWithNamedPointcut extends AbstractSingletonCountingAdvice {

		@Pointcut("execution(* *.getName())")
		public void getName() {
		}

		@Around("getName()")
		public Object newValue() throws Throwable {
			return "around";
		}
	}


	@Aspect
	public abstract static class AbstractSingletonCountingAdvice extends CountingConfiguration {
		@Before("execution(* *.getSpouse())")
		public void doesntMatter() { }
	}


	private void doTestAspectJAroundAdviceWithImplicitScope(Class<?> clazz) throws Exception {
		ctx = new JavaConfigApplicationContext(clazz);

		TestBean advised1 = (TestBean) ctx.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("around", advised1.getName());
	}


	/*
	 * The original intention of this test was to prove that a @Configuration
	 * class would respect any inner @Aspect class and apply its advice automatically.
	 * This functionality is NO LONGER SUPPORTED at this time.  Of course, if the inner
	 * class is explicitly specified as an aspect class, it will work fine, but this is
	 * not in alignment with the original functionality.
	 */
	// XXX: [aop]
	// XXX: [breaks-backward-compat]
	public @Test void testAspectJAroundAdviceWithAspectInnerClass() throws Exception {
		ctx = new JavaConfigApplicationContext();
		ctx.addConfigClass(InnerClassAdviceConfig.class);
		ctx.addAspectClasses(InnerClassAdviceConfig.InnerAroundAdvice.class);
		ctx.refresh();

		TestBean advised1 = (TestBean) ctx.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("around", advised1.getName());
	}
	public static class InnerClassAdviceConfig extends CountingConfiguration {
		// This is enough to bring it in
		@Aspect
		static class InnerAroundAdvice extends AroundAdviceClass { }
	}


	// XXX: [aop]
	public @Test void testAspectJNoAroundAdvice() throws Exception {
		// Superclass doesn't have around advice
		ctx = new JavaConfigApplicationContext(SingletonCountingAdvice.class);

		TestBean advised1 = ctx.getBean(TestBean.class, "advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("tony", advised1.getName());
	}
	public static class SingletonCountingAdvice extends AbstractSingletonCountingAdvice { }


	public abstract static class CountingConfiguration {
		// map from target to invocation count
		public static Map<Object, Integer> counts = new HashMap<Object, Integer>();

		public static int getCount(Object target) {
			Integer count = counts.get(target);
			return (count != null) ? count : 0;
		}

		@Bean(scope = DefaultScopes.PROTOTYPE)
		public TestBean advised() { return new TestBean("tony"); }

		@Bean(autowire = Autowire.BY_TYPE, dependencyCheck = DependencyCheck.ALL)
		public DependsOnTestBean dotb() { return new DependsOnTestBean(); }
	}


	// Invalid, doesn't have aspect tag
	public static class InvalidAroundAdviceClassWithNoAspectAnnotation {
		@Around("execution(* *.getName())")
		public Object newValue() throws Throwable { return "around"; }
	}

	@Aspect
	public abstract static class ValidAroundAdviceClassWithAspectAnnotation {
		@Around("execution(* *.getName())")
		public Object newValue() throws Throwable { return "around"; }
	}

	@Aspect @Configuration
	public abstract static class AroundAdviceClass extends ValidAroundAdviceClassWithAspectAnnotation { }


	// XXX: [aop]
	public @Test void testAroundAdviceWithArguments() throws Exception {
		ctx = new JavaConfigApplicationContext(SumAroundAdvice.class);
		ReturnZero rz = ctx.getBean(ReturnZero.class, "willAdd");
		assertEquals("Must add arguments, not return zero", 25, rz.returnZero(10, 15));
	}
	@Aspect
	public static class SumAroundAdvice {
		@Around("execution(int *.returnZero(int, int)) && args(a,b)")
		public Object newValue(int a, int b) throws Throwable { return a + b; }

		public @Bean ReturnZero willAdd() { return new ReturnZero(); }
	}
	public static class ReturnZero {
		public int returnZero(int a, int b) { return 0; }
	}

}
