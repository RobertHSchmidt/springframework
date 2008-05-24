package org.springframework.config.java.context;

import static org.junit.Assert.*;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.aop.Aspects;

/**
 * Quick and dirty brainstorm on everything that should be possible from
 * a high level with Aspects
 *
 * @author Chris Beams
 */
public class AspectIntegrationTests {
	public @Test void addAspect() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext();
		ctx.addConfigClasses(SomeConfig.class);
		ctx.addAspectClasses(SomeAspect.class);
		ctx.refresh();

		TestBean alice = ctx.getBean(TestBean.class);
		SomeAspect aspect = ctx.getBean(SomeAspect.class);
		assertFalse(aspect.beforeAdviceExecuted);
		alice.getName();
		assertTrue(aspect.beforeAdviceExecuted);
	}
	static class SomeConfig {
		public @Bean TestBean alice() { return new TestBean(); }
	}
	@Aspect
	static class SomeAspect {
		boolean beforeAdviceExecuted = false;
		@Before("execution(* getName())")
		public void flipBit() { beforeAdviceExecuted = true; }
	}


	public @Test void basicAspectImport() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(ConfigA.class);

		TestBean alice = ctx.getBean(TestBean.class);
		AspectA aspect = ctx.getBean(AspectA.class);
		assertFalse(aspect.beforeAdviceExecuted);
		alice.getName();
		assertTrue(aspect.beforeAdviceExecuted);
	}
	@Aspects(AspectA.class)
	static class ConfigA {
		public @Bean TestBean alice() { return new TestBean(); }
	}
	@Aspect
	static class AspectA {
		boolean beforeAdviceExecuted = false;
		@Before("execution(* getName())")
		public void flipBit() { beforeAdviceExecuted = true; }
	}


	public @Test void aspectImportsShouldApplyToAllConfigClasses() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(ConfigX.class, ConfigY.class);

		// AspectX advice should apply to ConfigY, even though ConfigX was the one to import it.
		AspectX aspect = ctx.getBean(AspectX.class);
		assertFalse(aspect.beforeAdviceExecuted);

		TestBean cat = ctx.getBean(TestBean.class, "alice");
		cat.getName();

		assertTrue(aspect.beforeAdviceExecuted);

	}
	@Aspects(AspectX.class)
	static class ConfigX {
		public @Bean TestBean alice() { return new TestBean(); }
	}
	static class ConfigY {
		public @Bean TestBean cat() { return new TestBean(); }
	}
	@Aspect
	static class AspectX {
		boolean beforeAdviceExecuted = false;
		@Before("execution(* getName())")
		public void flipBit() { beforeAdviceExecuted = true; }
	}


	public @Test void configClassesCanPlayDoubleDutyAsAspectClasses() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(AspectConfig.class);
		AspectConfig config = ctx.getBean(AspectConfig.class);
		assertFalse(config.beforeAdviceExecuted);
		TestBean alice = ctx.getBean(TestBean.class);
		TestBean alice2 = ctx.getBean(TestBean.class);
		assertSame(alice, alice2);
		alice.getName();
		assertTrue(config.beforeAdviceExecuted);
	}
	@Aspect @Configuration
	static class AspectConfig {
		public @Bean TestBean alice() { return new TestBean(); }

		boolean beforeAdviceExecuted = false;
		@Before("execution(* getName())")
		public void flipBit() { beforeAdviceExecuted = true; }
	}
}
