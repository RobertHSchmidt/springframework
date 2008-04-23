package issues;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * SJC-17 demonstrates the fact that JavaConfig's approach to programmatic bean
 * creation is fundamentally incompatible with
 * RequiredAnnotationBeanPostProcessor. SJC-91 addresses this issue by creating
 * a new strategy for checking {@link Required @Required} properties.
 *
 * This test simply proves the incompatibility.
 *
 * @author Chris Beams
 *
 * @see Sjc91Tests
 */
public class Sjc17Tests {
	@Test(expected = BeanCreationException.class)
	public void proveIncompatibility() {
		new JavaConfigApplicationContext(ApplicationConfiguration.class);
	}
}

@Configuration
class ApplicationConfiguration {
	@Bean
	public XYZ xyz() {
		return new XYZ();
	}

	@Bean
	public RequiredAnnotationBeanPostProcessor bpp() {
		return new RequiredAnnotationBeanPostProcessor();
	}
}

class XYZ {
	@SuppressWarnings("unused")
	private int i;

	@Required
	public void setFoo(int i) {
		this.i = i;
	}
}
