package issues;

import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.JavaConfigApplicationContext;

public class Sjc74JavaConfigApplicationContextTests extends Sjc74Tests {

	@Override
	protected ConfigurableJavaConfigApplicationContext newContext() {
		return new JavaConfigApplicationContext();
	}

}
