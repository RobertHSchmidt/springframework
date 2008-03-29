package issues;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

public class Sjc91Tests {
	private JavaConfigApplicationContext ctx;

	@Before
	public void setUp() {
		ctx = new JavaConfigApplicationContext();
	}

	@Test
	public void valid() {
		ctx.addConfigClass(ValidConfig.class);
		ctx.refresh();
	}

	@Test(expected = BeanCreationException.class)
	public void invalid() {
		ctx.addConfigClass(InvalidConfig.class);
		ctx.refresh();
	}

}

@Configuration(checkRequired = false)
class ValidConfig {
	public @Bean
	Alice alice() {
		Alice alice = new Alice();
		alice.setLocation("tea party");
		return alice;
	}
}

@Configuration(checkRequired = true)
class InvalidConfig {
	public @Bean
	Alice alice() {
		Alice alice = new Alice();
		// alice.setLocation("tea party");
		return alice;
	}
}

class Alice {
	private String location;

	@Required
	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "{name=[alice], location=[" + location + "]}";
	}
}