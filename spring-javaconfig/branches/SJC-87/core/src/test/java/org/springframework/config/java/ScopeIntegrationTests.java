package org.springframework.config.java;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.springframework.config.java.util.DefaultScopes.PROTOTYPE;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * Basic integration tests proving that scoping works as intended
 *
 * @author Chris Beams
 */
public class ScopeIntegrationTests {
	public @Test void singleton() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(SingletonConfig.class);
		TestBean t1 = ctx.getBean(TestBean.class);
		TestBean t2 = ctx.getBean(TestBean.class);
		assertSame(t1, t2);
	}
	static class SingletonConfig {
		public @Bean TestBean singleton() {
			return new TestBean("singleton");
		}
	}

	public @Test void prototype() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(PrototypeConfig.class);
		TestBean t1 = ctx.getBean(TestBean.class);
		TestBean t2 = ctx.getBean(TestBean.class);
		assertNotSame(t1, t2);
	}
	static class PrototypeConfig {
		public @Bean(scope=PROTOTYPE) TestBean prototype() {
			return new TestBean("prototype");
		}
	}
}
