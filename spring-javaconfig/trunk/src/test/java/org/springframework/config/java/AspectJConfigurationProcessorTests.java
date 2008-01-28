/*
 * Copyright 2002-2004 the original author or authors.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.junit.Ignore;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.beans.DependsOnTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.DependencyCheck;
import org.springframework.config.java.process.ConfigurationListenerRegistry;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.util.DefaultScopes;

/**
 * 
 * @author Rod Johnson
 */
public class AspectJConfigurationProcessorTests extends TestCase {

	{
		new ConfigurationListenerRegistry();
	}

	// TODO this may not be a valid test. Would need prototype aspect bean
	// and autoproxy
	public void xtestPerInstanceAdviceAndSharedAdvice() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(PerInstanceCountingAdvice.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		Object target1 = ((Advised) advised1).getTargetSource().getTarget();
		TestBean advised2 = (TestBean) bf.getBean("advised");

		// Hashcode works on this
		advised2.setAge(35);

		assertNotSame(advised1, advised2);
		Object target2 = ((Advised) advised2).getTargetSource().getTarget();
		assertNotSame(target1, target2);

		assertEquals("advised", bf.getBeanNamesForType(TestBean.class)[0]);

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

	public void testSharedAfterAdvice() throws Throwable {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(AfterAdvice.class);

		TestBean advised = (TestBean) bf.getBean("advised");
		AfterAdvice.count = 0;
		advised.absquatulate();
		assertEquals(0, AfterAdvice.count);
		advised.exceptional(null);
		assertEquals(1, AfterAdvice.count);
		try {
			advised.exceptional(new Exception());
		}
		catch (Throwable t) {
			// Expected
		}
		assertEquals("After advice should count failure", 2, AfterAdvice.count);
	}

	/**
	 * TODO inherited
	 * @param clazz
	 * @throws Exception
	 */
	public void testAspectJAnnotationsRequireAspectAnnotationDirect() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		assertFalse("Aspect annotationName required", configurationProcessor
				.processClass(InvalidNoAspectAnnotation.class) > 0);
	}

	public void testInvalidInheritanceFromConcreteAspect() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		try {
			configurationProcessor.processClass(InvalidInheritanceFromConcreteAspect.class);
			fail("Cannot extend a concrete aspect");
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}

	public void testAspectJAroundAdviceWithImplicitScope() throws Exception {
		doTestAspectJAroundAdviceWithImplicitScope(AroundSingletonCountingAdvice.class);
	}

	public void testAspectJAroundAdviceWithImplicitScopeAndNamedPointcut() throws Exception {
		doTestAspectJAroundAdviceWithImplicitScope(AroundAdviceWithNamedPointcut.class);
	}

	// TODO: this test is broken as of the changes for SJC-38. Not sure why
	// yet...
	@Ignore
	public void XtestAspectJAroundAdviceWithAspectInnerClass() throws Exception {
		doTestAspectJAroundAdviceWithImplicitScope(InnerClassAdvice.class);
	}

	private void doTestAspectJAroundAdviceWithImplicitScope(Class<?> clazz) throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(clazz);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("around", advised1.getName());
	}

	public void testAspectJAroundAdviceWithAspectClassScope() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(SingletonCountingAdvice.class);

		assertFalse("Must not allow class that does not define beans or aspects", configurationProcessor
				.processClass(InvalidAroundAdviceClassWithNoAspectAnnotation.class) > 0);
		configurationProcessor.processClass(AroundAdviceClass.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("around", advised1.getName());
	}

	// TODO do we really want the configuration class to *be* an aspect?
	// The model elsewhere is that configuration *contains* aspects
	// Structure it? It's Java 5 only?

	public void testAspectJNoAroundAdvice() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		// Superclass doesn't have around advice
		configurationProcessor.processClass(SingletonCountingAdvice.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("tony", advised1.getName());
	}

	// public void testPointcutExpressionWithoutArgs() throws Exception {
	// Set supportedPrimitives = new HashSet();
	// supportedPrimitives.add(PointcutPrimitive.EXECUTION);
	// supportedPrimitives.add(PointcutPrimitive.ARGS);
	// PointcutParser parser = new PointcutParser(supportedPrimitives);
	// PointcutExpression expression =
	// parser.parsePointcutExpression("execution(*
	// *.getN*() )");
	//
	// Method getNameMethod = ITestBean.class.getMethod("getName", null);
	// Method setNameMethod = ITestBean.class.getMethod("setName", new Class[] {
	// String.class} );
	//
	// FuzzyBoolean matches = expression.matchesMethodExecution(getNameMethod,
	// TestBean.class);
	// assertSame(FuzzyBoolean.YES,
	// expression.matchesMethodExecution(getNameMethod,
	// TestBean.class));
	// assertSame(FuzzyBoolean.NO,
	// expression.matchesMethodExecution(setNameMethod,
	// TestBean.class));
	// }

	public void testPointcutExpressionWithPointcutReference() throws Exception {
		Set<PointcutPrimitive> supportedPrimitives = new HashSet<PointcutPrimitive>();
		supportedPrimitives.add(PointcutPrimitive.EXECUTION);
		supportedPrimitives.add(PointcutPrimitive.ARGS);
		supportedPrimitives.add(PointcutPrimitive.REFERENCE);
		// TODO can hold state in pointcut parser
		// PointcutParser parser = new PointcutParser(supportedPrimitives);
		// PointcutExpression expression = parser.parsePointcutExpression("foo",
		// "execution(* *.getN*() )");
		// PointcutExpression orPc =
		// parser.parsePointcutExpression("execution(void
		// *.absquatulate()) || foo()");
		//
		// Method getNameMethod = ITestBean.class.getMethod("getName", null);
		// Method setNameMethod = ITestBean.class.getMethod("setName", new
		// Class[] {
		// String.class} );
		// Method absquatulateMethod = TestBean.class.getMethod("absquatulate",
		// null );
		//
		// FuzzyBoolean matches =
		// expression.matchesMethodExecution(getNameMethod,
		// TestBean.class);
		// assertSame(FuzzyBoolean.YES,
		// orPc.matchesMethodExecution(getNameMethod,
		// TestBean.class));
		// assertSame(FuzzyBoolean.NO,
		// orPc.matchesMethodExecution(setNameMethod,
		// TestBean.class));
		// assertSame(FuzzyBoolean.YES,
		// orPc.matchesMethodExecution(absquatulateMethod,
		// TestBean.class));
	}

	/**
	 * Invalid class, doesn't have an Aspect tag
	 * 
	 */
	public static class InvalidNoAspectAnnotation {
		@Around("execution(* *.getName())")
		public Object invalid() throws Throwable {
			return "around";
		}
	}

	public abstract static class CountingConfiguration {
		/**
		 * Map from target to invocation count
		 */
		public static Map<Object, Integer> counts = new HashMap<Object, Integer>();

		public static int getCount(Object target) {
			Integer count = counts.get(target);
			return (count != null) ? count : 0;
		}

		@Bean(scope = DefaultScopes.PROTOTYPE)
		public TestBean advised() {
			TestBean tb = new TestBean();
			tb.setName("tony");
			return tb;
		}

		@Bean(autowire = Autowire.BY_TYPE, dependencyCheck = DependencyCheck.ALL)
		public DependsOnTestBean dotb() {
			return new DependsOnTestBean();
		}
	}

	@Aspect
	public static class AfterAdvice extends CountingConfiguration {

		public static int count = 0;

		@After("execution(* *.exceptional(Throwable))")
		public void after() {
			++count;
		}
	}

	@Aspect
	public abstract static class AbstractSingletonCountingAdvice extends CountingConfiguration {

		@Before("execution(* *.getSpouse())")
		public void doesntMatter() {
			// Integer count = counts.get(target);
			// if (count == null) {
			// count = 0;
			// }
			// ++count;
			// counts.put(target, count);
		}
	}

	public static class SingletonCountingAdvice extends AbstractSingletonCountingAdvice {
	}

	// TODO different binding
	@Aspect
	public abstract static class PerInstanceCountingAdvice extends CountingConfiguration {

		@Before("execution(* *.getSpouse())")
		public void doesntMatter() {
			// Integer count = counts.get(target);
			// if (count == null) {
			// count = 0;
			// }
			// ++count;
			// counts.put(target, count);
		}
	}

	@Aspect
	public static class AroundSingletonCountingAdvice extends AbstractSingletonCountingAdvice {
		@Around("execution(* *.getName())")
		public Object newValue() throws Throwable {
			return "around";
		}
	}

	// TODO isn't aspect inherited? Clarify with Adrian
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

	public static class InnerClassAdvice extends CountingConfiguration {

		// This is enough to bring it in
		@Aspect
		static class InnerAroundAdvice extends AroundAdviceClass {
		}
	}

	// Invalid, doesn't have aspect tag
	public static class InvalidAroundAdviceClassWithNoAspectAnnotation {
		@Around("execution(* *.getName())")
		public Object newValue() throws Throwable {
			return "around";
		}
	}

	@Aspect
	public abstract static class ValidAroundAdviceClassWithAspectAnnotation {
		@Around("execution(* *.getName())")
		public Object newValue() throws Throwable {
			return "around";
		}
	}

	@Aspect
	@Configuration
	public abstract static class AroundAdviceClass extends ValidAroundAdviceClassWithAspectAnnotation {

	}

	public static class InvalidInheritanceFromConcreteAspect extends AroundSingletonCountingAdvice {

	}

	@Aspect
	public abstract static class SumAroundAdvice {
		@Around("execution(int *.returnZero(int, int)) && args(a,b)")
		public Object newValue(int a, int b) throws Throwable {
			return a + b;
		}

		@Bean
		public ReturnZero willAdd() {
			return new ReturnZero();
		}
	}

	public static class ReturnZero {
		@SuppressWarnings("unused")
		public int returnZero(int a, int b) {
			return 0;
		}
	}

	// TODO: fix w/ Maven
	public void tstAroundAdviceWithArguments() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(SumAroundAdvice.class);
		ReturnZero rz = (ReturnZero) bf.getBean("willAdd");
		assertEquals("Must add arguments, not return zero", 25, rz.returnZero(10, 15));
	}

}
