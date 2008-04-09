package issues;

import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.JavaConfigWebApplicationContext;

public class Sjc74JavaConfigWebApplicationContextTests extends Sjc74Tests {

	@Override
	protected ConfigurableJavaConfigApplicationContext newContext() {
		return new JavaConfigWebApplicationContext();
	}

}
