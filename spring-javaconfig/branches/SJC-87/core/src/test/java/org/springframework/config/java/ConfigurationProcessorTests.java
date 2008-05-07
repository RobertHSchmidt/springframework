/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.config.java;

import static org.junit.Assert.*;
import static org.springframework.config.java.test.Assert.getNonInternalBeanDefinitionCount;

import java.awt.Point;
import java.lang.reflect.Method;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.After;
import org.junit.Test;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.DependsOnTestBean;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.AspectJConfigurationProcessorTests.AroundAdviceWithNamedPointcut;
import org.springframework.config.java.ConfigurationProcessorTests.HiddenBeansConfig.BFAwareBean;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.aop.targetsource.HotSwappable;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.context.LegacyJavaConfigApplicationContext;
import org.springframework.config.java.model.ConfigurationModelBeanDefinitionReaderTests;
import org.springframework.config.java.model.ValidationError;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.process.MalformedJavaConfigurationException;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.config.java.util.DefaultScopes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Tests for {@link ConfigurationProcessor}
 *
 * @author Rod Johnson
 * @author Chris Beams
 */
// TODO: rename to JCACIntegrationTests?
public class ConfigurationProcessorTests {

	private ConfigurableJavaConfigApplicationContext ctx;

	/** Context should be initialized by each test */
	@After
	public void nullOutContext() { ctx = null; }


	public @Test void testSingletonNature() {
		ctx = new JavaConfigApplicationContext(BaseConfiguration.class);
		assertTrue(ctx.containsBean(BaseConfiguration.class.getName()));

		ITestBean tb = (ITestBean) ctx.getBean("tom");
		assertEquals("tom", tb.getName());
		assertEquals("becky", tb.getSpouse().getName());
		ITestBean tomsBecky = tb.getSpouse();
		ITestBean factorysBecky = (ITestBean) ctx.getBean("becky");

		// given that becky is a singleton-scoped bean, these objects should be same instance
		assertSame(tomsBecky, factorysBecky);
	}


	/**
	 * Beans that implement {@link BeanNameAware} should be injected
	 * with their bean names appropriately
	 */
	public @Test void testBeanNameAware() {
		ctx = new JavaConfigApplicationContext(BaseConfiguration.class);
		assertTrue(ctx.containsBean(BaseConfiguration.class.getName()));

		TestBean tom = (TestBean) ctx.getBean("tom");
		assertEquals("tom", tom.getName());
		assertEquals("tom", tom.getBeanName());
	}


	public @Test void testMethodOverrideWithJava() {
		ctx = new JavaConfigApplicationContext(MethodOverrideConfiguration.class);

		assertTrue(ctx.containsBean(MethodOverrideConfiguration.class.getName()));

		TestBean tom = ctx.getBean(TestBean.class, "tom");
		assertEquals("overridden", tom.getName());
	}
	public static class MethodOverrideConfiguration extends BaseConfiguration {
		@Override
		public TestBean tom() {
			return new TestBean() {
				@Override
				public String getName() {
					return "overridden";
				}
			};
		}
	}


	public @Test void testAfterPropertiesSetInvokedBeforeExplicitWiring() {
		ctx = new JavaConfigApplicationContext(AfterPropertiesConfiguration.class);

		// This is enough to run the test - see assertions in test() bean method below
		ctx.getBean(TestBean.class, "test");
	}
	public static class AfterPropertiesConfiguration {
		public @Bean TestBean test() {
			assertEquals("AfterPropertiesSet must have been called by now", 5, apt().sum());
			return new TestBean();
		}

		public @Bean AfterPropertiesTest apt() {
			AfterPropertiesTest apt = new AfterPropertiesTest();
			apt.setA(2);
			apt.setB(3);
			return apt;
		}
	}
	public static class AfterPropertiesTest implements InitializingBean {
		private int a, b, sum;
		public void setA(int a) { this.a = a; }
		public void setB(int b) { this.b = b; }
		public int sum() { return sum; }
		public void afterPropertiesSet() throws Exception { sum = a + b; }
	}


	public @Test void testBeanFactoryAware() {
		ctx = new JavaConfigApplicationContext(BaseConfiguration.class);

		assertTrue(ctx.containsBean(BaseConfiguration.class.getName()));

		TestBean becky = (TestBean) ctx.getBean("becky");
		assertEquals("becky", becky.getName());
		assertSame(ctx.getBeanFactory(), becky.getBeanFactory());
	}


	// TODO: [hiding]
	public @Test void simplestPossibleHidingScenario() {
		ctx = new JavaConfigApplicationContext(SimpleHiding.class);
		TestBean publicBean = ctx.getBean(TestBean.class, "publicBean");
		assertEquals("public", publicBean.getName());
		assertEquals("hidden", publicBean.getSpouse().getName());
		assertFalse("hiddenBean was not hidden!", ctx.containsBean("hiddenBean"));

	}
	static class SimpleHiding {
		public @Bean TestBean publicBean() {
			TestBean publicBean = new TestBean("public");
			publicBean.setSpouse(hiddenBean());
			return publicBean;
		}
		protected @Bean TestBean hiddenBean() { return new TestBean("hidden"); }
	}

	// TODO: [hiding]
	public @Test void testHidden() {
		ctx = new LegacyJavaConfigApplicationContext(BaseConfiguration.class);

		assertTrue(ctx.containsBean(BaseConfiguration.class.getName()));

		ITestBean dependsOnHidden = (ITestBean) ctx.getBean("dependsOnHidden");
		ITestBean hidden = dependsOnHidden.getSpouse();
		assertFalse("hidden bean 'hidden' should not be available via ctx", ctx.containsBean("hidden"));
		assertEquals("hidden", hidden.getName());
		assertEquals("becky", hidden.getSpouse().getName());
		ITestBean hiddenBecky = hidden.getSpouse();
		ITestBean factorysBecky = (ITestBean) ctx.getBean("becky");
		assertSame(hiddenBecky, factorysBecky);
	}


	// XXX: [autowiring, aop]
	public @Test void testAutowireOnBeanDefinition() {
		ctx = new JavaConfigApplicationContext(AroundAdviceWithNamedPointcut.class);

		ctx.getBean("dotb");
		DependsOnTestBean dotb1 = (DependsOnTestBean) ctx.getBean("dotb");
		DependsOnTestBean dotb2 = (DependsOnTestBean) ctx.getBean("dotb");
		assertSame(dotb1, dotb2);
		assertSame(dotb1.getTestBean(), dotb2.getTestBean());
		assertNotNull("autowiring failed", dotb1.getTestBean());
	}


	// XXX: [autowiring, aop]
	public @Test void testAutowireOnProxiedBeanDefinition() {
		ctx = new JavaConfigApplicationContext(ProxiesDotb.class);

		ProxiesDotb.count = 0;
		DependsOnTestBean sarah = (DependsOnTestBean) ctx.getBean("sarah");
		assertTrue("bean 'sarah' should have been an AOP proxy", AopUtils.isAopProxy(sarah));
		assertTrue("bean 'sarah' should have been an CGLIB proxy", AopUtils.isCglibProxy(sarah));
		assertNotNull("autowiring did not complete successfully", sarah.getTestBean());

		assertEquals(1, ProxiesDotb.count);
	}
	@Aspect @Configuration
	public static class ProxiesDotb {
		public static int count = 0;

		public @Bean TestBean adrian() { return new TestBean("adrian", 34); }

		@Bean(autowire = Autowire.BY_TYPE)
		public DependsOnTestBean sarah() { return new DependsOnTestBean(); }

		@Before("execution(* getTestBean())")
		public void println() {
			++count;
		}
	}


	@Test(expected=MalformedJavaConfigurationException.class)
	public void testInvalidFinalConfigurationClass() {
		try {
			// should throw, rejecting final configuration class;
			new JavaConfigApplicationContext(InvalidFinalConfigurationClass.class);
		} catch (MalformedJavaConfigurationException ex) {
			assertTrue(ex.getMessage().contains(ValidationError.CONFIGURATION_MUST_BE_NON_FINAL.toString()));
			throw ex;
		}
	}
	@Configuration
	public final static class InvalidFinalConfigurationClass {
		@Bean
		public DummyFactory factoryBean() {
			return new DummyFactory();
		}
	}


	// XXX: [model validation]
	 public @Test void testValidEvenThoughBeanMethodIsFinal() {
		// strangely, this does not throw even though the @Bean method is final.
		ctx = new JavaConfigApplicationContext(InvalidDueToFinalBeanMethod.class);
		Object b1 = ctx.getBean("factoryBean");
		Object b2 = ctx.getBean("factoryBean");
		// prove that we actually got a singleton - this shows that the final method was actually proxied
		assertSame(b1, b2);
	}
	public static class InvalidDueToFinalBeanMethod {
		public final @Bean DummyFactory factoryBean() {
			return new DummyFactory();
		}
	}


	// TODO: should fail fast with a MalformedJavaConfigurationException
	// XXX: [model validation]
	@Test(expected = BeanCreationException.class)
	public void testInvalidDueToFinalBeanClass() {
		ctx = new JavaConfigApplicationContext(InvalidDueToFinalBeanClass.class);
		// Arguably should spot this earlier
		ctx.getBean("test");
		// should have thrown, rejecting final Bean method
	}
	@Configuration @Aspect
	public static class InvalidDueToFinalBeanClass {
		@Before("execution(* get*())")
		public void empty() { }

		public @Bean FinalTestBean test() { return new FinalTestBean(); }
	}
	private final static class FinalTestBean extends TestBean { }


	@Test(expected=MalformedJavaConfigurationException.class)
	public void testInvalidDueToPrivateBeanMethod() {
		// should throw, rejecting private Bean method
		new JavaConfigApplicationContext(InvalidDuePrivateBeanMethod.class);
	}
	static class InvalidDuePrivateBeanMethod {
		public @Bean Object ok() { return new Object(); }
		@SuppressWarnings("unused")
		private @Bean Object notOk() { return new Object(); }
	}


	// XXX: [aop]
	public @Test void testValidWithDynamicProxy() {
		ctx = new JavaConfigApplicationContext(ValidWithDynamicProxies.class);
		ITestBean tb = (ITestBean) ctx.getBean("test");
		assertTrue(AopUtils.isJdkDynamicProxy(tb));
	}
	@Configuration @Aspect
	public static class ValidWithDynamicProxies {
		@Before("execution(* get*())")
		public void empty() { }

		public @Bean ITestBean test() { return new FinalTestBean(); }
	}


	public @Test void testApplicationContextAwareCallbackWithGenericApplicationContext() {
		ctx = new JavaConfigApplicationContext(ApplicationContextAwareConfiguration.class);
		ApplicationContextAwareImpl acai = (ApplicationContextAwareImpl) ctx.getBean("ai");
		assertNotNull("ApplicationContextAware callback must be honoured", acai.applicationContext);
	}
	public static class ApplicationContextAwareConfiguration {
		public @Bean ApplicationContextAwareImpl ai() { return new ApplicationContextAwareImpl(); }
	}
	public static class ApplicationContextAwareImpl implements ApplicationContextAware {
		public ApplicationContext applicationContext;
		public void setApplicationContext(ApplicationContext ac) { this.applicationContext = ac; }
	}


	// TODO test override in XML: possible
	// TODO conflict in Java config: illegal
	// TODO multiple advice on the one method
	// TODO deep getBeans
	// TODO circular get beans
	// TODO default lazy and other lazy


	public @Test void testDefaultAutowire() {
		ctx = new JavaConfigApplicationContext(DefaultAutowireConfiguration.class);

		DependsOnTestBean sarah = (DependsOnTestBean) ctx.getBean("sarah");
		assertNotNull("autowiring did not occur: sarah should have TestBean", sarah.getTestBean());
		assertEquals("autowiring error", "adrian", sarah.getTestBean().getName());
	}
	@Configuration(defaultAutowire = Autowire.BY_TYPE)
	public static class DefaultAutowireConfiguration {
		public @Bean TestBean adrian() { return new TestBean("adrian", 34); }
		public @Bean DependsOnTestBean sarah() { return new DependsOnTestBean(); }
	}


	public @Test void testFactoryBean() {
		ctx = new JavaConfigApplicationContext(ContainsFactoryBean.class);
		assertTrue("Factory bean must return created type", ctx.getBean("factoryBean") instanceof TestBean);
	}
	public static class ContainsFactoryBean {
		public @Bean DummyFactory factoryBean() { return new DummyFactory(); }
	}


	/**
	 * Bean methods should be 'inherited'
	 */
	public @Test void testNewAnnotationNotRequiredOnConcreteMethod() {
		ctx = new JavaConfigApplicationContext(InheritsWithoutNewAnnotation.class);

		TestBean tom = ctx.getBean(TestBean.class, "tom");
		TestBean becky = ctx.getBean(TestBean.class, "becky");
		assertSame(tom, becky.getSpouse());
		assertSame(becky, ctx.getBean("becky"));
	}
	public static abstract class DefinesAbstractBeanMethod {
		public @Bean TestBean becky() {
			TestBean becky = new TestBean();
			becky.setSpouse(tom());
			return becky;
		}

		public @Bean abstract TestBean tom();
	}
	public static class InheritsWithoutNewAnnotation extends DefinesAbstractBeanMethod {
		@Override
		public TestBean tom() { return new TestBean(); }
	}


	public @Test void testProgrammaticProxyCreation() {
		ctx = new JavaConfigApplicationContext(ProxyConfiguration.class);

		ITestBean proxy = ctx.getBean(ITestBean.class, "proxied");
		assertSame(proxy, ctx.getBean("proxied"));
		ProxyConfiguration.count = 0;
		String name = "Shane Warne";
		proxy.setName(name);
		assertEquals(1, ProxyConfiguration.count);
		assertEquals(name, proxy.getName());
		assertEquals(2, ProxyConfiguration.count);
	}
	public static class ProxyConfiguration {
		public static int count;
		public @Bean ITestBean proxied() {
			TestBean tb = new TestBean();
			ProxyFactory pf = new ProxyFactory(tb);
			pf.addAdvice(new MethodBeforeAdvice() {
				public void before(Method method, Object[] args, Object target) throws Throwable {
					++count;
				}
			});
			return (ITestBean) pf.getProxy();
		}
	}


	/**
	 * @see ConfigurationModelBeanDefinitionReaderTests#renderWithAliases() (unit test)
	 */
	public @Test void testBeanAliases() {
		ctx = new JavaConfigApplicationContext(AliasesConfiguration.class);

		ITestBean aliasedBean = ctx.getBean(ITestBean.class, "aliasedBean");
		assertEquals("Legion", aliasedBean.getName());
		assertSame(aliasedBean, ctx.getBean("aliasedBean"));
		assertSame(aliasedBean, ctx.getBean("tom"));
		assertSame(aliasedBean, ctx.getBean("dick"));
		assertSame(aliasedBean, ctx.getBean("harry"));
		assertFalse(ctx.containsBean("Glen"));
	}
	public static class AliasesConfiguration {
		@Bean(aliases = { "tom", "dick", "harry" })
		public TestBean aliasedBean() {
			TestBean tb = new TestBean();
			tb.setName("Legion");
			return tb;
		}
	}


	// TODO: [aop, hot-swap]
	public @Test void testHotSwappable() {
		ctx = new LegacyJavaConfigApplicationContext(HotSwapConfiguration.class);

		TestBean hs = (TestBean) ctx.getBean("hotSwappable");
		assertTrue(AopUtils.isCglibProxy(hs));
		assertEquals("hotSwappable", hs.getName());
		Advised adv = (Advised) hs;
		assertTrue(adv.getTargetSource() instanceof HotSwappableTargetSource);

		ITestBean ihs = (ITestBean) ctx.getBean("hotSwappableInterface");
		assertFalse(ihs instanceof TestBean);
		assertTrue(AopUtils.isAopProxy(ihs));
		assertTrue("Should not proxy target class if return type is an interface", AopUtils.isJdkDynamicProxy(ihs));
		assertEquals("hotSwappableInterface", ihs.getName());
		adv = (Advised) ihs;
		assertTrue(adv.getTargetSource() instanceof HotSwappableTargetSource);
	}
	public static class HotSwapConfiguration extends ConfigurationSupport {
		public @HotSwappable @Bean TestBean hotSwappable() {
			TestBean tb = new TestBean();
			tb.setName("hotSwappable");
			return tb;
		}

		public @HotSwappable @Bean ITestBean hotSwappableInterface() {
			TestBean tb = new TestBean();
			tb.setName("hotSwappableInterface");
			return tb;
		}
	}


	public @Test void testBeanFactoryAwareConfiguration() {
		ctx = new JavaConfigApplicationContext(BeanFactoryAwareConfiguration.class);

		ITestBean marriedToInjection = ctx.getBean(ITestBean.class, "marriedToInjection");
		TestBean spouse = ctx.getBean(TestBean.class, "spouse");
		assertNotNull(marriedToInjection.getSpouse());
		assertSame(spouse, marriedToInjection.getSpouse());
	}
	public static class BeanFactoryAwareConfiguration extends ConfigurationSupport {
		public @Bean TestBean spouse() { return new TestBean("spouse"); }
		public @Bean TestBean marriedToInjection() {
			TestBean tb = new TestBean();
			tb.setSpouse((TestBean) getBeanFactory().getBean("spouse"));
			return tb;
		}
	}


	// TODO: [hiding, autowiring]
	public @Test void testEffectOfHidingOnAutowire() {
		ctx = new LegacyJavaConfigApplicationContext(AutowiringConfiguration.class);

		assertFalse(ctx.containsBean("testBean"));
		DependsOnTestBean dotb = ctx.getBean(DependsOnTestBean.class, "autowireCandidate");
		assertNull("Should NOT have autowired with hidden bean", dotb.tb);
	}
	public static class AutowiringConfiguration {
		protected @Bean TestBean testBean() { return new TestBean(); }

		@Bean(autowire = Autowire.BY_TYPE)
		public DependsOnTestBean autowireCandidate() { return new DependsOnTestBean(); }
	}


	// TODO: [hiding, autowiring]
	public @Test void testHiddenBeansDoNotConfuseAutowireByType() {
		ctx = new LegacyJavaConfigApplicationContext(AutowiringConfigurationWithNonHiddenWinner.class);

		assertFalse(ctx.containsBean("testBean"));
		DependsOnTestBean dotb = ctx.getBean(DependsOnTestBean.class, "autowireCandidate");
		assertNotNull("autowiring failed", dotb.tb);
		assertEquals("autowire winner must be visible", "visible", dotb.tb.getName());
	}
	public static class AutowiringConfigurationWithNonHiddenWinner {
		protected @Bean TestBean testBean() { return new TestBean(); }

		public @Bean TestBean nonHiddenTestBean() {
			TestBean tb = new TestBean();
			tb.setName("visible");
			return tb;
		}

		@Bean(autowire = Autowire.BY_TYPE)
		public DependsOnTestBean autowireCandidate() { return new DependsOnTestBean(); }
	}


	// XXX: [autowiring]
	@Test(expected=UnsatisfiedDependencyException.class)
	public void testAutowireAmbiguityIsRejected() {
		try {
			ctx = new JavaConfigApplicationContext(InvalidAutowiringConfigurationWithAmbiguity.class);
			ctx.getBean("autowireCandidate");
		}
		catch (UnsatisfiedDependencyException ex) {
			assertFalse("Useful error message required", ex.getMessage().indexOf("autowireCandidate") == -1);
			throw(ex);
		}
	}
	public static class InvalidAutowiringConfigurationWithAmbiguity {
		public @Bean TestBean testBean() { return new TestBean(); }
		public @Bean TestBean nonHiddenTestBean() { return new TestBean("visible"); }
		@Bean(autowire = Autowire.BY_TYPE)
		public DependsOnTestBean autowireCandidate() { return new DependsOnTestBean(); }
	}


	public @Test void testBeanCreationMethodsThatMayThrowExceptions() {
		BeanCreationMethodsThrowExceptions.makeItFail = false;
		ctx = new JavaConfigApplicationContext(BeanCreationMethodsThrowExceptions.class);
		assertNotNull(ctx.getBean("throwsException"));
		assertNotNull(ctx.getBean("throwsThrowable"));
		assertNotNull(ctx.getBean("throwsOtherCheckedException"));
	}
	@Test(expected=BeanCreationException.class)
	public void testBeanCreationMethodsThatDoThrowExceptions() {
		BeanCreationMethodsThrowExceptions.makeItFail = true;
		ctx = new JavaConfigApplicationContext(BeanCreationMethodsThrowExceptions.class);
		ctx.getBean("throwsException");
	}
	public static class BeanCreationMethodsThrowExceptions {
		public static boolean makeItFail;

		public @Bean TestBean throwsException() throws Exception {
			if (makeItFail) throw new Exception();
			return new TestBean();
		}

		public @Bean TestBean throwsThrowable() throws Throwable {
			if (makeItFail) throw new Throwable();
			return new TestBean();
		}

		public @Bean TestBean throwsOtherCheckedException() throws InterruptedException {
			if (makeItFail) throw new InterruptedException();
			return new TestBean();
		}
	}


	@Test(expected=BeanCreationException.class)
	public void testBeanCreationMethodReturnsNull() {
		// should throw upon pre-instantiation of singleton 'returnsNull'
		ctx = new JavaConfigApplicationContext(BeanCreationMethodReturnsNull.class);
	}
	public static class BeanCreationMethodReturnsNull {
		public @Bean TestBean returnsNull() { return null; }
	}


	// TODO: should fail fast with MalformedJavaConfigurationException
	// XXX: [model validation]
	@Test(expected=BeanCreationException.class)
	public void testBeanCreationMethodCannotHaveVoidReturn() {
		ctx = new JavaConfigApplicationContext(BeanCreationMethodReturnsVoid.class);
	}
	public static class BeanCreationMethodReturnsVoid {
		public @Bean void invalidReturnsVoid() { }
	}


	// TODO: [aop, autowiring]
	public @Test void testAutowiringOnProxiedBean() {
		ctx = new LegacyJavaConfigApplicationContext(AdvisedAutowiring.class);
		Husband husband = ctx.getBean(Husband.class, "husband");
		assertTrue(AopUtils.isAopProxy(husband));
		assertNotNull("Advised object should have still been autowired", husband.getWife());
	}
	@Aspect @Configuration
	public static class AdvisedAutowiring {
		@Bean(autowire = Autowire.BY_TYPE)
		public Husband husband() { return new HusbandImpl(); }

		public @Bean Wife wife() { return new Wife(); }

		@Before("execution(* getWife())")
		protected void log() { /* nothing */ }
	}
	public static class Wife { }
	public static interface Husband { Wife getWife(); }
	public static class HusbandImpl implements Husband {
		private Wife wife;
		public Wife getWife() { return wife; }
		public void setWife(Wife wife) { this.wife = wife; }
	}


	// XXX: [@AutoBean]
	public @Test void testValidAutoBean() {
		ctx = new JavaConfigApplicationContext(ValidAutoBeanTest.class);

		TestBean kerry = ctx.getBean(TestBean.class, "kerry");
		assertEquals("AutoBean was not autowired", "Rod", kerry.getSpouse().getName());
	}
	public abstract static class ValidAutoBeanTest extends ConfigurationSupport {
		public @Bean TestBean rod() { return new TestBean("Rod"); }
		public abstract @AutoBean TestBean kerry();
	}


	// XXX: [@AutoBean]
	// XXX: [breaks-backward-compat] previously expected BeanDefinitionStoreException
	@Test(expected = MalformedJavaConfigurationException.class)
	public void testInvalidAutoBean() {
		try {
		ctx = new JavaConfigApplicationContext(InvalidAutoBeanTest.class);
		} catch (MalformedJavaConfigurationException ex) {
			assertTrue(ex.getMessage().contains(ValidationError.AUTOBEAN_MUST_BE_CONCRETE_TYPE.toString()));
			throw ex;
		}
	}
	public abstract static class InvalidAutoBeanTest extends ConfigurationSupport {
		public @Bean TestBean rod() { return new TestBean(kerry()); }
		// Invalid, as it's on an interface type
		public abstract @AutoBean ITestBean kerry();
	}


	// TODO: [hiding]
	public @Test void testHiddenBeans() {
		ctx = new LegacyJavaConfigApplicationContext(HiddenBeansConfig.class);

		BeanFactory bf = ctx.getBeanFactory();
		// hidden beans
		assertFalse(bf.containsBean("protectedBean"));
		assertFalse(bf.containsBean("packageBean"));

		BFAwareBean beans = (BFAwareBean) bf.getBean("beans");
		assertEquals(2, beans.getBeans().length);

		BeanFactory hiddenBF = ((BFAwareBean) beans.getBeans()[1]).bf;
		assertNotSame(bf, hiddenBF);

		assertTrue(hiddenBF.containsBean("protectedBean"));
		assertTrue(hiddenBF.containsBean("packageBean"));
	}
	public static class HiddenBeansConfig {
		public static class BFAwareBean implements BeanFactoryAware {
			public BeanFactory bf;
			private Object[] beans;
			private String name;
			public void setBeanFactory(BeanFactory beanFactory) throws BeansException { this.bf = beanFactory; }
			public void setBeans(Object[] beans) { this.beans = beans; }
			public Object[] getBeans() { return beans; }
			public BFAwareBean(String name) { this.name = name; }
			public BeanFactory getBf() { return bf; }
		}

		@Bean Object packageBean() { return new BFAwareBean("package"); }
		protected @Bean Object protectedBean() { return new BFAwareBean("protected"); }
		public @Bean BFAwareBean beans() {
			BFAwareBean bean = new BFAwareBean("public");
			bean.setBeans(new Object[] { packageBean(), protectedBean() });
			return bean;
		}
	}


	public @Test void testBeanDefinitionCount() throws Exception {
		// TODO: [hiding]
		// 3 @Bean + 1 @Configuration - 2 hidden @Bean
		assertEquals(2, getNonInternalBeanDefinitionCount(new LegacyJavaConfigApplicationContext(HiddenBeansConfig.class)));
		// 2 @Bean + 1 @Configuration
		assertEquals(3, getNonInternalBeanDefinitionCount(new JavaConfigApplicationContext(AdvisedAutowiring.class)));
		// TODO: [hiding]
		// 2 @Bean + 1 @Configuration - 1 hidden @Bean
		assertEquals(2, getNonInternalBeanDefinitionCount(new LegacyJavaConfigApplicationContext(AutowiringConfiguration.class)));
		// TODO: [hiding]
		// 6 @Bean + 1 Configuration - 1 hidden @Bean
		assertEquals(6, getNonInternalBeanDefinitionCount(new LegacyJavaConfigApplicationContext(BaseConfiguration.class)));
		// 2 @Bean + 1 Configuration
		assertEquals(3, getNonInternalBeanDefinitionCount(new JavaConfigApplicationContext(HotSwapConfiguration.class)));
		// XXX: [@AutoBean]
		// 1 @Bean + 1 @Autobean + 1 Configuration
		assertEquals(3, getNonInternalBeanDefinitionCount(new JavaConfigApplicationContext(ValidAutoBeanTest.class)));
	}


	/**
	 * Base class used across tests for easy reuse of configuration scenarios
	 */
	public static class BaseConfiguration {
		@Bean(scope = DefaultScopes.SINGLETON, lazy = Lazy.FALSE)
		public TestBean tom() {
			TestBean tom = basePerson();
			tom.setName("tom");
			tom.setSpouse(becky());
			return tom;
		}

		@Bean(scope = DefaultScopes.PROTOTYPE)
		public Point prototypePoint() { return new Point(3, 4); }

		@Bean(scope = DefaultScopes.PROTOTYPE, lazy = Lazy.FALSE)
		public TestBean prototype() {
			TestBean tom = basePerson();
			tom.setName("prototype");
			tom.setSpouse(becky());
			return tom;
		}

		// Parent template mechanism
		protected TestBean basePerson() { return new TestBean(); }

		public @Bean TestBean becky() { return new TestBean("becky"); }

		public @Bean TestBean dependsOnHidden() { return new TestBean(hidden()); }

		protected @Bean TestBean hidden() {
			TestBean hidden = new TestBean();
			hidden.setName("hidden");
			hidden.setSpouse(becky());
			return hidden;
		}
	}

}
