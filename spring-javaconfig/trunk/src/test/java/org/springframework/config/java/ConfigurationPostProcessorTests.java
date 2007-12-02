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

import static org.junit.Assert.*;

import java.awt.Point;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.CountingBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.beans.DependsOnTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.config.java.ConfigurationProcessorTests.BaseConfiguration;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.aop.SpringAdvice;
import org.springframework.config.java.process.ConfigurationPostProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rod Johnson
 */
public class ConfigurationPostProcessorTests {

	@Test
	public void testPriorityOrdering() {
		ConfigurationPostProcessor cpp = new ConfigurationPostProcessor();
		assertEquals(Integer.MIN_VALUE, cpp.getOrder());
	}

	@Test
	public void testSingleton() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/test.xml");

		ITestBean tb = (ITestBean) bf.getBean("tom");
		assertEquals("tom", tb.getName());
		assertEquals("becky", tb.getSpouse().getName());

		ITestBean tb2 = (ITestBean) bf.getBean("tom");
		assertSame(tb, tb2);

		// System.out.println(((Advised) tb).toProxyConfigString());

		ITestBean tomsBecky = tb.getSpouse();
		ITestBean factorysBecky = (ITestBean) bf.getBean("becky");
		assertSame(tomsBecky, factorysBecky);
	}

	@Test
	public void testInjectedConfig() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/injectedTest.xml");

		ITestBean tb = (ITestBean) bf.getBean("testBean");
		assertEquals("Injected age returned", 33, tb.getAge());
	}

	/**
	 * Test that a bean defined in XML can be injected with a bean from a config
	 */
	@Test
	public void testReverseInjectionWithAutowire() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/reverseInjection.xml");

		DependsOnTestBean tb = (DependsOnTestBean) bf.getBean("dependsOnTestBean");
		assertNotNull(tb.tb);
		assertEquals(33, tb.tb.getAge());
	}

	@Test
	public void testReverseInjectionExplicit() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/reverseInjection.xml");

		DependsOnTestBean tb = (DependsOnTestBean) bf.getBean("dependsOnTestBeanExplicit");
		assertNotNull(tb.tb);
		assertEquals(33, tb.tb.getAge());
	}

	@Test
	public void testProxiedPrototype() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/test.xml");

		ITestBean tb = (ITestBean) bf.getBean("prototype");
		assertEquals("prototype", tb.getName());
		assertEquals("becky", tb.getSpouse().getName());

		ITestBean tb2 = (ITestBean) bf.getBean("prototype");
		assertEquals("prototype", tb.getName());
		assertEquals("becky", tb.getSpouse().getName());

		assertNotSame(tb, tb2);

		ITestBean tomsBecky = tb.getSpouse();
		ITestBean factorysBecky = (ITestBean) bf.getBean("becky");
		assertSame(tomsBecky, factorysBecky);
	}

	@Test
	public void testNonProxiedPrototype() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/test.xml");

		Point tb = (Point) bf.getBean("prototypePoint");
		// assertEquals(3, tb.);
		// assertEquals("becky", tb.getSpouse().getName());

		Point tb2 = (Point) bf.getBean("prototypePoint");
		// assertEquals("prototype", tb.getName());
		// assertEquals("becky", tb.getSpouse().getName());

		assertNotSame(tb, tb2);

		// System.out.println(((Advised) tb).toProxyConfigString());

		// ITestBean tomsBecky = tb.getSpouse();
		// ITestBean factorysBecky = (ITestBean) bf.getBean("becky");
		// assertSame(tomsBecky, factorysBecky);
	}

	@Test
	public void testPointcut() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/test.xml");
		IOther becky = (IOther) bf.getBean("becky");

		assertEquals("No advisors put in factory", 0, bf.getBeanNamesForType(Advisor.class).length);

		assertTrue("Becky bean should be proxied", AopUtils.isAopProxy(becky));

		try {
			// Should fire pointcut
			becky.absquatulate();
			fail();
		}
		catch (UnsupportedOperationException ex) {
			// OK
		}

		assertFalse("No pointcut in factory: method was protected (hidden)", bf.containsBean("debugAdvice"));
	}

	@Test
	public void testProgrammaticProxyCreation() throws Exception {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/test.xml");

		Point proxied = (Point) bf.getBean("proxied");
		// ITestBean tom = (ITestBean) bf.getBean("tom");
		Advised a = (Advised) proxied;

		// TODO
		// assertSame(a.getTargetSource().getTarget(), tom);
		assertEquals(1, a.getAdvisors().length);
		assertEquals(bf.getBean("counter"), a.getAdvisors()[0].getAdvice());
		// proxied.getName();
		proxied.getLocation();
	}

	@Test
	public void testAbstractBeanDefinition() throws Exception {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/abstractDef.xml");

		// there should be two, nothing more!
		assertEquals(2, bf.getBeanDefinitionCount());
		assertFalse(AbstractConfig.testBeanCreated);
	}

	@Configuration
	static class AbstractConfig {

		public static boolean testBeanCreated = false;

		@Bean
		public TestBean testBean() {
			// this should never occur
			testBeanCreated = true;
			return new TestBean();
		}
	}

	@Configuration
	static class InjectedConfig {
		private int age;

		public void setAge(int age) {
			this.age = age;
		}

		@Bean
		public TestBean testBean() {
			TestBean tb = new TestBean();
			tb.setAge(age);
			return tb;
		}
	}

	@Configuration
	@Aspect
	static class AdvisedBaseConfiguration extends BaseConfiguration {

		// @Beans
		// public Map getBeans() {
		// // TODO
		// factory method is the map method, arg is keys
		// but need to know what the keys are...
		// could be static, but would ideally like to be
		// based on instance config values
		// }

		@Bean()
		@SpringAdvice("execution(* absquatulate())")
		protected Advice debugAdvice() {
			return new MethodBeforeAdvice() {
				public void before(Method method, Object[] args, Object target) throws Throwable {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Bean
		public Advice counter() {
			return new MethodBeforeAdvice() {
				public void before(Method method, Object[] args, Object target) throws Throwable {
					System.out.println("Before " + method);
				}
			};
		}

		@Bean
		public Point point() {
			return new Point(3, 2);
		}

		@Bean
		public Point proxied() {
			ProxyFactory pf = new ProxyFactory(point());
			NameMatchMethodPointcut nmpc = new NameMatchMethodPointcut();
			nmpc.addMethodName("foo");
			pf.addAdvisor(new DefaultPointcutAdvisor(nmpc, counter()));
			pf.setProxyTargetClass(true);
			return (Point) pf.getProxy();
		}
	}

	// public void testXmlAutoProxyCreator() {
	// GenericApplicationContext gac = new GenericApplicationContext();
	// Configurer cfg = new Configurer(gac);
	// cfg.xml(getClass(), "test.xml");
	// cfg.add("testBean", TestBean.class).prop("name", "tom");
	//
	// // Not picked up by get beans of type
	//
	// // TODO register that does factory bean or factory method!?
	// // how to parameterize? would need to add class!?
	//
	// //cfg.addSingleton("nopAdvisor", new DefaultPointcutAdvisor(new
	// NopInterceptor()));
	//
	// ((DefaultPointcutAdvisor) cfg.add("nopAdvisor",
	// DefaultPointcutAdvisor.class))
	// .setAdvice(new NopInterceptor());
	//
	// gac.refresh();
	//
	// DefaultPointcutAdvisor a = (DefaultPointcutAdvisor)
	// gac.getBean("nopAdvisor");
	// NopInterceptor ni = (NopInterceptor) a.getAdvice();
	//
	// DefaultAdvisorAutoProxyCreator apc = (DefaultAdvisorAutoProxyCreator)
	// gac.getBean("autoproxy");
	//
	// System.out.println(gac);
	// assertEquals(0, ni.getCount());
	// ITestBean tb = (ITestBean) gac.getBean("testBean");
	// assertEquals("tom", tb.getName());
	// assertEquals(1, ni.getCount());
	// assertTrue(tb instanceof Advised);
	// }
	//
	// public void testGroovyScript() {
	// GenericApplicationContext bf = new GenericApplicationContext();
	// Configurer cfg = new Configurer(bf);
	//
	// String propVal = "zoe";
	//
	// cfg.add("gsf", GroovyScriptFactory.class);
	//
	// // TODO doesn't support DI here
	// cfg.addFactoryBean("hello", "gsf", "create")
	// .carg("org/springframework/beans/factory/script/groovy/PropertyHello.groovy")
	// .prop("message", propVal);
	//
	// bf.refresh();
	//
	// Hello hello = (Hello) bf.getBean("hello");
	//
	// assertTrue("Not a script", hello instanceof DynamicScript);
	//
	// assertEquals(propVal, hello.sayHello());
	// }
	//

	@Test
	public void testFactoryBean() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/myfactory.xml");

		ITestBean tb = (ITestBean) bf.getBean("factoryCreatedTestBean");
		assertEquals("jenny", tb.getName());
	}

	@Configuration
	static class FactoryBeanConfig {
		@Bean
		public MyFactory factoryCreatedTestBean() {
			String myString = "jenny";
			MyFactory f = new MyFactory();
			f.setMyString(myString);
			return f;
		}
	}

	static class MyFactory implements FactoryBean {

		private String myString;

		public void setMyString(String myString) {
			this.myString = myString;
		}

		public String getMyString() {
			return myString;
		}

		/**
		 * @see org.springframework.config.java.context.java.beans.factory.FactoryBean#getObject()
		 */
		public Object getObject() throws Exception {
			TestBean tb = new TestBean();
			tb.setName(myString);
			return tb;
		}

		/**
		 * @see org.springframework.config.java.context.java.beans.factory.FactoryBean#getObjectType()
		 */
		public Class<?> getObjectType() {
			return TestBean.class;
		}

		/**
		 * @see org.springframework.config.java.context.java.beans.factory.FactoryBean#isSingleton()
		 */
		public boolean isSingleton() {
			return true;
		}
	}

	@Configuration
	@Aspect
	static class Advised1 {
		@Bean
		public TestBean oldJohannes() {
			return new TestBean("johannes", 29);
		}

		@Around("execution(int *.getAge())")
		public Object age(ProceedingJoinPoint pjp) throws Throwable {
			int realAge = (Integer) pjp.proceed();
			return realAge * 2;
		}
	}

	@Configuration
	@Aspect
	static class Advised2 {
		@Bean
		public TestBean youngJohannes() {
			return new TestBean("johannes", 29);
		}

		@Around("execution(int *.getAge())")
		public Object age() throws Throwable {
			return 21;
		}
	}

	@Test
	public void testAspectsAreIndependent() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/independenceTest.xml");

		TestBean realJohannes = (TestBean) bf.getBean("realJohannes");
		assertFalse(AopUtils.isAopProxy(realJohannes));
		assertEquals("Should NOT be affected by aspects", 29, realJohannes.getAge());

		TestBean oldJohannes = (TestBean) bf.getBean("oldJohannes");
		assertTrue(AopUtils.isAopProxy(oldJohannes));
		assertEquals(realJohannes.getAge() * 2, oldJohannes.getAge());

		TestBean youngJohannes = (TestBean) bf.getBean("youngJohannes");
		assertTrue(AopUtils.isAopProxy(youngJohannes));
		assertEquals(21, youngJohannes.getAge());
	}

	//
	// // TODO SHOULD be able to run same tests on ac and bf

	// public void testOnApplicationContextWithPostProcessors() {
	// AbstractApplicationContext ac = new ParameterizableApplicationContext();
	// NopInterceptor nop = new NopInterceptor();
	// ac.getBeanFactory().registerSingleton("nopInterceptor", nop);
	// BeanNameAutoProxyCreator bnapc = new BeanNameAutoProxyCreator();
	// bnapc.setInterceptorNames(new String[] { "nopInterceptor"});
	// bnapc.setBeanNames(new String[] { "test*" });
	// bnapc.setBeanFactory(ac.getBeanFactory());
	//
	// ac.getBeanFactory().addBeanPostProcessor(bnapc);
	// JavaBeanDefinitionReader jbr = new
	// JavaBeanDefinitionReader((BeanDefinitionRegistry)
	// ac.getBeanFactory());
	// assertEquals(1, jbr.addDefinitions(MyBeans.class));
	// System.out.println(ac);
	// ITestBean tb = (ITestBean) ac.getBean("testBean");
	// assertEquals("tom", tb.getName());
	//
	// assertTrue(AopUtils.isAopProxy(tb));
	// assertEquals(1, nop.getCount());
	// }

	@Configuration
	@Aspect
	static class PostProcessedConfig {
		private int counter;

		@Bean
		public ITestBean adrian() {
			TestBean adrian = new TestBean();
			adrian.setName("adrian");
			return adrian;
		}

		@Bean
		public ITestBean testEligibleForAutoproxying() {
			return new TestBean();
		}

		@Bean
		public AdvisedByConfig advisedByConfig() {
			return new AdvisedByConfig();
		}

		// TODO: this method never gets used. is this by design? (cbeams)
		@SuppressWarnings("unused")
		@Around("execution(int *.intValue())")
		private int returnCount(ProceedingJoinPoint pjp) {
			return counter++;
		}
	}

	@Aspect
	static class CountAspect {
		public static int counter;

		// TODO: this method never gets used. is this by design? (cbeams)
		@SuppressWarnings("unused")
		@Around("execution(int *.intValue())")
		private int countIntValueInvocation(ProceedingJoinPoint pjp) throws Throwable {
			++counter;
			return (Integer) pjp.proceed();
		}
	}

	static class AdvisedByConfig {
		public int intValue() {
			return 0;
		}
	}

	@Test
	public void testBeanNamePostProcessorAppliesToBeansFromConfig() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/beanNamePostProcessor.xml");

		ITestBean adrian = (ITestBean) bf.getBean("adrian");
		assertFalse(AopUtils.isAopProxy(adrian));

		ITestBean autoProxied = (ITestBean) bf.getBean("testEligibleForAutoproxying");
		assertTrue(AopUtils.isAopProxy(autoProxied));
		CountingBeforeAdvice cba = (CountingBeforeAdvice) bf.getBean("countingBeforeAdvice");
		assertEquals(0, cba.getCalls());
		autoProxied.getName();
		assertEquals(1, cba.getCalls());
		// System.out.println(((Advised) autoProxied).toProxyConfigString());
	}

	@Test
	public void testAspectIsLocalToConfig() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/beanNamePostProcessor.xml");

		AdvisedByConfig advisedByConfig = (AdvisedByConfig) bf.getBean("advisedByConfig");
		AdvisedByConfig notAdvisedByConfig = (AdvisedByConfig) bf.getBean("externalNotAdvisedByAspectWithinConfig");
		assertFalse(AopUtils.isAopProxy(notAdvisedByConfig));
		assertEquals(0, notAdvisedByConfig.intValue());
		assertEquals(0, notAdvisedByConfig.intValue());

		assertEquals(0, advisedByConfig.intValue());
		assertEquals("Must have incremented in aspect", 1, advisedByConfig.intValue());
	}

	// NB: Requires that singletons are not materialized to derive their type:
	// see AbstractAutowireCapableBeanFactory.getTypeForFactoryMethod
	// TODO: check behavior with Spring 2.0.1
	@Test
	public void testLocalAndExternalAspects() throws Exception {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/localAndExternalAspects.xml");

		System.out.println(Arrays.toString(bf.getBeanDefinitionNames()));
		Object bean = bf.getBean("advisedByConfig");
		Advised outerProxy = (Advised) bean;

		assertTrue(AopUtils.isAopProxy(outerProxy));
		System.out.println(outerProxy.toProxyConfigString());
		// Object innerProxy = outerProxy.getTargetSource().getTarget();
		// System.out.println(innerProxy);
		Advised innerProxy = (Advised) outerProxy.getTargetSource().getTarget();
		assertTrue(bean instanceof AdvisedByConfig);
		assertTrue(innerProxy instanceof AdvisedByConfig);
		AdvisedByConfig doublyAdvised = (AdvisedByConfig) bean;
		assertEquals(0, CountAspect.counter);
		assertEquals(0, doublyAdvised.intValue());
		assertEquals(1, doublyAdvised.intValue());
		System.out.println(CountAspect.counter);
		assertEquals(2, CountAspect.counter);
	}

	@Configuration
	public static abstract class ExternalBeanConfiguration {
		@Bean
		public TestBean bob() {
			TestBean bob = new TestBean();
			bob.setSpouse(ann());
			return bob;
		}

		// Will be taken from XML
		@ExternalBean
		public abstract TestBean ann();
	}

	@Configuration
	public static class ExternalBeanProvidingConfiguration {
		@Bean
		public TestBean ann() {
			return new TestBean();
		}
	}

	@Configuration
	static class ExternalBeanConfigurationNonAbstract extends ExternalBeanConfiguration {

		@Override
		@ExternalBean
		public TestBean ann() {
			throw new UnsupportedOperationException("should not be called");
		}
	}

	@Test
	public void testExternalBean() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"org/springframework/config/java/externalBean.xml");
		TestBean bob = (TestBean) bf.getBean("bob");
		assertTrue(bf.containsBean("ann"));
		assertEquals("External bean must have been satisfied", "Ann", bob.getSpouse().getName());
	}

	@Test
	public void testExternalBeanNonAbstract() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(
				"org/springframework/config/java/externalBean.xml");
		TestBean bob = (TestBean) bf.getBean("bob");
		assertTrue(bf.containsBean("ann"));
		assertEquals("External bean must have been satisfied", "Ann", bob.getSpouse().getName());
	}

	/**
	 * TODO: test for SJC-17. Ignored while determining if changes need to be
	 * made to Spring Core to support this.
	 */
	@Ignore
	@Test
	public void testRequiredAnnotation() {
		// this is going to throw a BeanCreationException, complaining that
		// 'alice' hasn't been properly configured
		new ClassPathXmlApplicationContext("org/springframework/config/java/requiredBean.xml");
	}

	@Configuration
	static class RequiredBeanConfig {
		@Bean
		public Alice alice() {
			Alice bean = new Alice();
			// notice I'm explicitly setting the @Required property with a value
			bean.setName("alice");
			return bean;
		}
	}

	static class Alice {
		@SuppressWarnings("unused")
		private String name;

		@Required
		public void setName(String name) {
			this.name = name;
		}
	}

}
