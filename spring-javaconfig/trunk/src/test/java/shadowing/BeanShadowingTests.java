package shadowing;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @see BeanOverridingTests
 * @author Chris Beams
 */
public class BeanShadowingTests {

	/**
	 * XML config works on a LIFO-based shadowing model. JavaConfig should work
	 * the same way.
	 */
	@Test
	public void demonstrateXmlShadowingIsBasedOnOrder() {
		{
			String[] configLocations = new String[] { "second.xml", "first.xml" };
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocations, getClass());
			assertEquals("first", ctx.getBean("foo"));
		}

		{
			String[] configLocations = new String[] { "first.xml", "second.xml" };
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocations, getClass());
			assertEquals("second", ctx.getBean("foo"));
		}
	}

	@Test
	public void testShadowingIsBasedOnOrder1() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(Second.class);
		assertEquals("second", context.getBean(TestBean.class).getName());
	}

	@Test
	public void testShadowingIsBasedOnOrder2() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(Second.class, First.class);
		assertEquals("first", context.getBean(TestBean.class).getName());
	}

	@Configuration
	public static class First {
		@Bean(allowOverriding = true)
		public TestBean foo() {
			return new TestBean("first");
		}
	}

	@Configuration
	public static class Second {
		@Bean(allowOverriding = true)
		public TestBean foo() {
			return new TestBean("second");
		}

		@Bean
		public String bar() {
			return "bar";
		}
	}

	// ------------------------------------------------------------------------

	@Test
	public void testLegalShadowingViaXml() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext("legalShadow.xml", getClass());
		TestBean bob = (TestBean) bf.getBean("bob");
		assertTrue(bf.containsBean("ann"));

		String msg = "Property value must have come from XML override, not @Bean method";
		assertThat(msg, "Ann", equalTo(bob.getSpouse().getName()));
	}

	@Test(expected = IllegalStateException.class)
	public void testIllegalShadowingViaXml() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext("illegalShadow.xml", getClass());
		bf.getBean("ann");
	}

	@Configuration
	static class LegalShadowConfiguration {
		@Bean
		public TestBean bob() {
			TestBean bob = new TestBean();
			bob.setSpouse(ann());
			return bob;
		}

		@Bean(allowOverriding = true)
		public TestBean ann() {
			return new TestBean();
		}
	}

	@Configuration
	static class IllegalShadowConfiguration {
		@Bean
		public TestBean bob() {
			TestBean bob = new TestBean();
			bob.setSpouse(ann());
			return bob;
		}

		// Does not allow overriding
		@Bean(allowOverriding = false)
		public TestBean ann() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * XML config ensures that beans defined in a child context override any
	 * beans in the parent context with the same name. JavaConfig should work
	 * the same way.
	 */
	@Test
	public void demonstrateXmlShadowingWorksProperlyWhenNestingContexts() {
		ApplicationContext parent = new ClassPathXmlApplicationContext("first.xml", getClass());
		ApplicationContext child = new ClassPathXmlApplicationContext(new String[] { "second.xml" }, getClass(), parent);
		assertEquals("first", parent.getBean("foo"));
		assertEquals("second", child.getBean("foo"));
	}

	@Test
	public void testChildContextBeanShadowsParentContextBean() {
		JavaConfigApplicationContext firstContext = new JavaConfigApplicationContext(First.class);
		JavaConfigApplicationContext secondContext = new JavaConfigApplicationContext(firstContext);
		secondContext.setConfigClasses(Second.class);
		secondContext.refresh();

		assertEquals("first", ((TestBean) firstContext.getBean("foo")).getName());
		assertEquals("second", ((TestBean) secondContext.getBean("foo")).getName());
		assertEquals("bar", secondContext.getBean("bar"));
	}

	// corners a subtle bug I found along the way...
	@Test
	public void testChildContextBeanShadowsParentContextBeanWhenUsingTypeSafeGetBeanMethod() {
		JavaConfigApplicationContext firstContext = new JavaConfigApplicationContext(First.class);
		JavaConfigApplicationContext secondContext = new JavaConfigApplicationContext(firstContext);
		secondContext.setConfigClasses(Second.class);
		secondContext.refresh();

		// assertEquals("first", firstContext.getBean(TestBean.class.getName());
		assertEquals("first", (firstContext.getBean(TestBean.class)).getName());
		assertEquals("second", (secondContext.getBean(TestBean.class)).getName());
		assertEquals("bar", secondContext.getBean(String.class));
	}

}
