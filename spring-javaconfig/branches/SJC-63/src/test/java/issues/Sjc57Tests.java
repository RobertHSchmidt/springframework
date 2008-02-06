package issues;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.config.java.AspectTests.PropertyChangeTracker;
import org.springframework.config.java.AspectTests.Service;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * SJC-57 revealed a bug where Reusable aspects (@Aspect-annotated
 * @Configuration classes) were not applied if passed in as a constructor
 * argument to JavaConfigApplicationContext.
 * 
 * This issue was resolved WONT FIX, so the test is ignored.
 * 
 * @see org.springframework.config.java.AspectTests
 * @author Christopher Hlubek
 * @author Chris Beams
 */
public class Sjc57Tests {

	@Ignore
	@Test
	public void testReusableAspect() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(StandaloneAppConfig.class,
				PropertyChangeTracker.class);
		PropertyChangeTracker changeTracker = ctx.getBean(PropertyChangeTracker.class);

		Service service = ctx.getBean(Service.class);
		service.setCacheSize(2500);

		// assert that aspect was applied
		assertThat(changeTracker.propertyChangeCount, equalTo(1));
	}

	/*
	 * It should be possible to apply a reusable aspect that can be configured as
	 * an additional config class without a @Import.
	 */
	@Configuration
	public static class StandaloneAppConfig {
		@Bean
		public Service service() {
			return new Service();
		}
	}

}
