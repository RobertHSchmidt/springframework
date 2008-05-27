package org.springframework.config.java.factory;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * See SJC-47
 *
 * @author Ben Rowlands
 * @author Chris Beams
 */
public class AutowiredAnnotationBeanPostProcessorCompatibilityTests {
	private JavaConfigApplicationContext context;

	@Before
	public void setUp() {
		context = new JavaConfigApplicationContext(MyConfig.class);
	}

	private static void assertAutowired(MyBeanHolder holder) {
		assertNotNull(holder);
		assertNotNull(holder.bean);
		assertNotNull("widget was not autowired as expected", holder.bean.autoWiredWidget);
	}

	@Test
	public void testAutowiringPrivate() {
		assertAutowired((MyBeanHolder) context.getBean("holderForPrivate"));
	}

	@Test
	public void testAutowiringPublic() {
		assertAutowired((MyBeanHolder) context.getBean("holderForPublic"));
	}

	public static class Widget {
	}

	public static class MyBean {
		@Autowired
		private Widget autoWiredWidget;
	}

	public static class MyBeanHolder {
		private MyBean bean;

		public MyBeanHolder(MyBean bean_) {
			bean = bean_;
		}
	}

	@Configuration
	public static class MyConfig {
		@Bean
		public AutowiredAnnotationBeanPostProcessor myBPP() {
			return new AutowiredAnnotationBeanPostProcessor();
		}

		@Bean
		public Widget widget() {
			return new Widget();
		}

		@Bean
		public MyBeanHolder holderForPrivate() {
			return new MyBeanHolder(privateBean());
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
		MyBean privateBean() {
			return new MyBean();
		}
	}
}
