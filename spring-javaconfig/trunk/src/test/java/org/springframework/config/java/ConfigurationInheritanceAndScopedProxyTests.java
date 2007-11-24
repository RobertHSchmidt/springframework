package org.springframework.config.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.support.ConfigurationSupport;

/**
 * <code>ConfigurationInheritanceTest</code> TODO gduchesneau JavaDoc.
 * 
 * Test for SJC-25. TODO: Test method is currently being Ignored. Review and
 * apply Guillaume's proposed fix.
 * 
 * @author gduchesneau
 * @since 2.0
 */
public class ConfigurationInheritanceAndScopedProxyTests {

	private static boolean[] createNewScope = new boolean[] { true };

	public static final String SCOPE = "my scope";

	public static String flag = "1";

	private DefaultListableBeanFactory bf;

	private ConfigurationProcessor configurationProcessor;

	@Before
	public void setUp() throws Exception {
		bf = new DefaultListableBeanFactory();
		bf.registerScope(SCOPE, new MyCustomScope());
		configurationProcessor = new ConfigurationProcessor(bf);

		// and do the processing
		configurationProcessor.processClass(ExtendedConfigurationClass.class);
	}

	public static abstract class BaseConfigurationClass extends ConfigurationSupport {

		@Bean(scope = SCOPE)
		@ScopedProxy
		public TestBean overridenTestBean() {
			TestBean tb = new TestBean();
			tb.setName("overridenTestBean");
			return tb;
		}

		@Bean(scope = SCOPE)
		@ScopedProxy
		public abstract TestBean abstractTestBean();
	}

	public static class ExtendedConfigurationClass extends BaseConfigurationClass {

		@Override
		public TestBean overridenTestBean() {
			TestBean tb = super.overridenTestBean();
			tb.setName(tb.getName() + "-modified");
			return tb;
		}

		@Override
		public TestBean abstractTestBean() {
			TestBean tb = new TestBean();
			tb.setName("abstractTestBean");
			return tb;
		}
	}

	/**
	 * Simple scope implementation which creates object based on a flag.
	 * 
	 * @author Costin Leau
	 * 
	 */
	public static class MyCustomScope implements Scope {

		private Map<String, Object> beans = new HashMap<String, Object>();

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.beans.factory.config.Scope#get(java.lang.String,
		 * org.springframework.beans.factory.ObjectFactory)
		 */
		public Object get(String name, ObjectFactory objectFactory) {

			if (createNewScope[0]) {
				beans.clear();
				// reset the flag back
				createNewScope[0] = false;
			}

			Object bean = beans.get(name);
			// if a new object is requested or none exists under the current
			// name, create one
			if (bean == null) {
				beans.put(name, objectFactory.getObject());
			}
			return beans.get(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.beans.factory.config.Scope#getConversationId()
		 */
		public String getConversationId() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String,
		 * java.lang.Runnable)
		 */
		public void registerDestructionCallback(String name, Runnable callback) {
			// do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
		 */
		public Object remove(String name) {
			return beans.remove(name);
		}

	}

	@Ignore
	@Test
	public void testConfigurationInheritance() {

		TestBean overridenTestBean = (TestBean) bf.getBean("overridenTestBean");
		assertNotNull(overridenTestBean);
		assertEquals("overridenTestBean-modified", overridenTestBean.getName());

		TestBean abstractTestBean = (TestBean) bf.getBean("abstractTestBean");
		assertNotNull(abstractTestBean);
		assertEquals("abstractTestBean", abstractTestBean.getName());

		createNewScope[0] = true;

		TestBean overridenTestBean2 = (TestBean) bf.getBean("overridenTestBean");
		assertNotNull(overridenTestBean2);
		assertEquals("overridenTestBean-modified", overridenTestBean2.getName());
		assertNotSame(overridenTestBean, overridenTestBean2);

		TestBean abstractTestBean2 = (TestBean) bf.getBean("abstractTestBean");
		assertNotNull(abstractTestBean2);
		assertEquals("abstractTestBean", abstractTestBean2.getName());
		assertNotSame(abstractTestBean, abstractTestBean2);
	}
}