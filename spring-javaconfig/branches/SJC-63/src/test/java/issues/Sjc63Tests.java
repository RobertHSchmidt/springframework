package issues;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

public class Sjc63Tests {
	@Test
	public void ultraSimple() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(MyConfig.class);
	}

	@Configuration
	static class MyConfig {
		@Bean
		public TestBean testBean() {
			return new TestBean("ultraSimpleBean");
		}
	}
}
