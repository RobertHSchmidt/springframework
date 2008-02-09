package issues;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.process.ConfigurationProcessor;

public class Sjc63Tests {
	@Test
	public void ultraSimple() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(MyConfig.class);
	}

	@Test
	public void directCallToProcessClass() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor p = new ConfigurationProcessor(bf);
		p.processClass(MyConfig.class);

		TestBean bean = (TestBean) bf.getBean("testBean");
		System.out.println(bean);
		Assert.assertNotNull(bean);
	}

	@Test
	public void simpleImport() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor p = new ConfigurationProcessor(bf);
		p.processClass(MainConfig.class);
		Assert.assertThat(((TestBean) bf.getBean("otherBean")).getName(), CoreMatchers.equalTo("other"));
	}

	@Aspect
	@Configuration
	static class MyConfig {
		@Bean
		public TestBean testBean() {
			return new TestBean("ultraSimpleBean");
		}

		@Before("execution(* *..TestBean.toString())")
		public void log() {
			System.out.println("got here");
		}
	}

	@Import(OtherConfig.class)
	@Configuration
	static class MainConfig {
		@Bean
		public TestBean mainBean() {
			return new TestBean("main");
		}
	}

	@Configuration
	static class OtherConfig {
		@Bean
		public TestBean otherBean() {
			return new TestBean("other");
		}
	}
}
