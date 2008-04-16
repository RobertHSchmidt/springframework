package org.springframework.config.java.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;

public class ReflectiveJavaConfigurationModelPopulatorTests {

	private JavaConfigurationModel model;
	private ReflectiveJavaConfigurationModelPopulator populator;

	@Before
	public void initModelPopulator() {
		model = new JavaConfigurationModel();
		populator = new ReflectiveJavaConfigurationModelPopulator(model);
	}

	@Test
	public void simple() {
		// fixture class
		@Configuration class Config {
			@SuppressWarnings("unused")
			@Bean TestBean alice() {
				return new TestBean("alice");
			}
		}

		// expected model
		JavaConfigurationModel expectedModel =
			new JavaConfigurationModel()
    			.addConfigurationClass(
    				new ConfigurationClass(Config.class.getName())
    					.addBeanMethod(new BeanMethod("alice")));

		populator.addToModel(Config.class);

		assertEquals(expectedModel, model);

		assertEquals(1, model.getConfigurationClasses().length);
	}

}
