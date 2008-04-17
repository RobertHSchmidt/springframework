package org.springframework.config.java.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;

/**
 * Integration test for {@link JavaConfigurationModelPopulator} implementations.
 *
 * <p>Contract for each test:
 * <ul>
 * <li>define a configuration class and assign that class literal to <tt>configClass</tt></li>
 * <li>populate <tt>targetModel</tt> with the expected results of processing <tt>configClass</tt></li>
 * </ul>
 *
 * {@link #initializeModelsAndPopulator()} will initialize model objects and call back to
 * {@link newPopulator()} to instantiate a new populator.
 * {@link #populateResultModelAndCompareToTargetModel()} will ensure that the result of processing
 * <tt>configClass</tt> against the populator produces a model equivalent to <tt>targetModel</tt>
 *
 * @author Chris Beams
 */
public abstract class JavaConfigurationModelPopulatorTests {

	private Class<?> configClass;
	private JavaConfigurationModel resultModel;
	private JavaConfigurationModel targetModel;
	private JavaConfigurationModelPopulator populator;

	/**
	 * Each concrete subclass must implement this method in order to receive a call back in
	 * {@link #initializeModelsAndPopulator()}
	 */
	protected abstract JavaConfigurationModelPopulator newPopulator(JavaConfigurationModel model);

	@Before
	public void initializeModelsAndPopulator() {
		resultModel = new JavaConfigurationModel();
		targetModel = new JavaConfigurationModel();
		populator = newPopulator(resultModel);
	}

	@After
	public void populateResultModelAndCompareToTargetModel() {
		assertNotNull("configClass has not been set for this test", configClass);
		assertTrue("targetModel has not been populated for this test",
				targetModel.getConfigurationClasses().length > 0);

		populator.addToModel(configClass);

		assertEquals("models were not equivalent", targetModel, resultModel);
	}


	// -----------------------------------------------
	// Individual tests
	// -----------------------------------------------

	public @Test void simplestPossibleConfigDefinition() {
		class Config { }
		configClass = Config.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(Config.class.getName()));
	}

	public @Test void beanMethodsAreRecognized() {
		@Configuration class Config {
			@Bean TestBean alice() {
				return new TestBean("alice");
			}
		}
		configClass = Config.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(Config.class.getName())
				.addBeanMethod(new BeanMethod("alice")));
	}

	public @Test void nonBeanMethodsAreIgnored() {
		@Configuration class Config {
			@Bean TestBean alice() {
				return new TestBean("alice");
			}
			TestBean knave() {
				return new TestBean("knave");
			}
		}
		configClass = Config.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(Config.class.getName())
				.addBeanMethod(new BeanMethod("alice")));
	}

	/* it's not necessarily important to preserve order of @Bean methods
	 * within a given class, but it will be important when dealing with
	 * @Import.  TODO: write this test
	 */
	@Ignore // not possible with java reflection. getDeclaredClasses does not preserve order
	public @Test void beanMethodOrderIsPreservedA() {
		@Configuration class Config {
			@Bean TestBean alice() {
				return new TestBean("alice");
			}
			@Bean TestBean knave() {
				return new TestBean("knave");
			}
		}
		configClass = Config.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(Config.class.getName())
				.addBeanMethod(new BeanMethod("alice"))
				.addBeanMethod(new BeanMethod("knave")));
	}

	@Ignore // not possible with java reflection. getDeclaredClasses does not preserve order
	public @Test void beanMethodOrderIsPreservedB() {
		@Configuration class Config {
			@Bean TestBean knave() {
				return new TestBean("knave");
			}
			@Bean TestBean alice() {
				return new TestBean("alice");
			}
		}
		configClass = Config.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(Config.class.getName())
				.addBeanMethod(new BeanMethod("knave"))
				.addBeanMethod(new BeanMethod("alice")));
	}


	// -----------------------------------------------
	// Concrete test implementations (one per implementation of JavaConfigurationModelPopulator)
	// -----------------------------------------------
	public static class ReflectiveJavaConfigurationModelPopulatorTests extends JavaConfigurationModelPopulatorTests {
		@Override
		protected JavaConfigurationModelPopulator newPopulator(JavaConfigurationModel model) {
			return new ReflectiveJavaConfigurationModelPopulator(model);
		}
	}

	/* TODO: implement and uncomment
	public static class AsmJavaConfigurationModelPopulatorTests extends JavaConfigurationModelPopulatorTests {
		@Override
		protected JavaConfigurationModelPopulator<?> newPopulator(JavaConfigurationModel model) {
			return new AsmJavaConfigurationModelPopulator(model);
		}
	}
	*/
}
