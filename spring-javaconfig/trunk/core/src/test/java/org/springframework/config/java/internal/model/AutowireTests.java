package org.springframework.config.java.internal.model;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

public class AutowireTests {

	//XXX: [autowire]
	public @Test void test() {
		BeanFactory bf = new DefaultListableBeanFactory();

		RootBeanDefinition bdFoo = new RootBeanDefinition();
		bdFoo.setBeanClassName(Foo.class.getName());
		bdFoo.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

		RootBeanDefinition bdBar = new RootBeanDefinition();
		bdBar.setBeanClassName(Bar.class.getName());

		((BeanDefinitionRegistry) bf).registerBeanDefinition("foo", bdFoo);
		((BeanDefinitionRegistry) bf).registerBeanDefinition("bar", bdBar);

		Foo foo = (Foo) bf.getBean("foo");

		Assert.assertNotNull("bar did not get autowired", foo.getBar());
	}

	//XXX: [autowire]
	public @Test void testSJC() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(MyConfig.class);
		Foo foo = ctx.getBean(Foo.class);

		Assert.assertNotNull("bar did not get autowired", foo.getBar());
	}

	@Configuration(defaultAutowire=Autowire.BY_NAME)
	public static class MyConfig {
		@Bean(autowire=Autowire.BY_TYPE)
		public Foo foo() { return new Foo(); }

		@Bean
		public Bar barX() { return new Bar(); }
	}


	public static class Foo {
		private Bar bar;
		public void setBar(Bar bar) { this.bar = bar; }
		public Bar getBar() { return bar; }
	}

	public static class Bar { }

}
