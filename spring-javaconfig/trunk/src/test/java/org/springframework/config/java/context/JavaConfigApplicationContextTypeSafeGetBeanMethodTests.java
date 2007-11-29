package org.springframework.config.java.context;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.AmbiguousBeanLookupException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.MultiplePrimaryBeanDefinitionException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Primary;

public class JavaConfigApplicationContextTypeSafeGetBeanMethodTests {

	private JavaConfigApplicationContext ctx;

	/** happy path */
	@Test
	public void testGetBeanOfTypeT() {
		ctx = new JavaConfigApplicationContext(SingleBeanConfig.class);

		TestBean testBean = ctx.getBean(TestBean.class);

		assertNotNull("return value should never be null", testBean);
		assertThat(testBean.getName(), equalTo("service"));
	}

	@Test(expected = AmbiguousBeanLookupException.class)
	public void testGetBeanByTypeWithMultipleCanditates() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(TestBean.class); // will throw
	}

	/**
	 * Tests that given two beans having the same supertype but one having a
	 * more specific concrete types than the other, looking up the more specific
	 * bean by it's concrete type succeeds
	 */
	@Test
	public void testDisambiguationBySubclass() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(MyTestBean.class);
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testNoSuchBeanDefinitionException() {
		ctx = new JavaConfigApplicationContext(SingleBeanConfig.class);
		ctx.getBean(String.class);
	}

	@Test
	public void testDisambiguationByPrimaryDesignation() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfigWithPrimary.class);
		ctx.getBean(TestBean.class);
	}

	@Test(expected = MultiplePrimaryBeanDefinitionException.class)
	public void testCannotDisambiguateWithMultiplePrimaryDesignations() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfigWithMultiplePrimaries.class);
		ctx.getBean(TestBean.class);
	}

	@Test
	public void testDisambiguateByProvidingQualifyingBeanName() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		TestBean serviceA = ctx.getBean(TestBean.class, "serviceA");
		assertNotNull(serviceA);
	}

	@Test(expected = BeanNotOfRequiredTypeException.class)
	public void testDisambiguateByProvidingQualifyingBeanNameWithWrongClassName() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(String.class, "serviceA"); // throws
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testDisambiguateByProvidingQualifyingBeanNameWithWrongBeanName() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(TestBean.class, "serviceX"); // throws
	}

	@Configuration
	static class SingleBeanConfig {
		@Bean
		public TestBean service() {
			return new TestBean("service");
		}
	}

	@Configuration
	static class MultiBeanConfig {
		@Bean
		public TestBean serviceA() {
			return new TestBean("serviceA");
		}

		@Bean
		public MyTestBean serviceB() {
			return new MyTestBean("serviceB");
		}
	}

	static class MyTestBean extends TestBean {
		public MyTestBean(String name) {
			super(name);
		}
	}

	@Configuration
	static class MultiBeanConfigWithPrimary {
		@Bean(primary = Primary.TRUE)
		public TestBean serviceA() {
			return new TestBean("serviceA");
		}

		@Bean
		public TestBean serviceB() {
			return new TestBean("serviceB");
		}
	}

	@Configuration
	static class MultiBeanConfigWithMultiplePrimaries {
		@Bean(primary = Primary.TRUE)
		public TestBean serviceA() {
			return new TestBean("serviceA");
		}

		@Bean(primary = Primary.TRUE)
		public TestBean serviceB() {
			return new TestBean("serviceB");
		}
	}

}
