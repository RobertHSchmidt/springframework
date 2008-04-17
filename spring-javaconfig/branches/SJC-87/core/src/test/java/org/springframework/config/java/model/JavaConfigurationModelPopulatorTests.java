package org.springframework.config.java.model;

import static org.junit.Assert.*;

import java.lang.reflect.Modifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;

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
	 * Each concrete subclass must implement this method in order to receive a callback in
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
		class Config {
			@Bean TestBean alice() { return new TestBean(); }
		}
		configClass = Config.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(Config.class.getName())
				.addBeanMethod(new BeanMethod("alice")));
	}

	public @Test void nonBeanMethodsAreIgnored() {
		@Configuration class Config {
			@Bean TestBean alice() { return new TestBean(); }
			TestBean knave() { return new TestBean(); }
		}
		configClass = Config.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(Config.class.getName())
				.addBeanMethod(new BeanMethod("alice")));
	}


	class BeanMethodOrderConfig {
		@Bean TestBean alice() { return new TestBean(); }
		@Bean TestBean knave() { return new TestBean(); }
	}
	public @Test void beanMethodOrderIsNotSignificantA() {
		configClass = BeanMethodOrderConfig.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(BeanMethodOrderConfig.class.getName())
				.addBeanMethod(new BeanMethod("alice"))
				.addBeanMethod(new BeanMethod("knave"))
			);
	}
	public @Test void beanMethodOrderIsNotSignificantB() {
		configClass = BeanMethodOrderConfig.class;

		targetModel.addConfigurationClass(
			new ConfigurationClass(BeanMethodOrderConfig.class.getName())
				.addBeanMethod(new BeanMethod("knave"))
				.addBeanMethod(new BeanMethod("alice"))
			);
	}

	public @Test void importIsRecognized() {
		class Imported {
			@Bean TestBean alice() { return new TestBean(); }
		}

		@Import(Imported.class)
		class Config {
			@Bean TestBean knave() { return new TestBean(); }
		}
		configClass = Config.class;

		targetModel
			.addConfigurationClass(
				new ConfigurationClass(Imported.class.getName())
					.addBeanMethod(new BeanMethod("alice")))
			.addConfigurationClass(
				new ConfigurationClass(Config.class.getName())
					.addBeanMethod(new BeanMethod("knave")))
		;
	}

	public @Test void multipleImportsAreSupported() {
		class Imported1 { @Bean TestBean alice() { return new TestBean(); } }
		class Imported2 { @Bean TestBean queen() { return new TestBean(); } }
		@Import({Imported1.class, Imported2.class})
		class Config { @Bean TestBean knave() { return new TestBean(); } }
		configClass = Config.class;

		targetModel
			.addConfigurationClass(
				new ConfigurationClass(Imported1.class.getName())
					.addBeanMethod(new BeanMethod("alice")))
			.addConfigurationClass(
				new ConfigurationClass(Imported2.class.getName())
					.addBeanMethod(new BeanMethod("queen")))
			.addConfigurationClass(
				new ConfigurationClass(Config.class.getName())
					.addBeanMethod(new BeanMethod("knave")))
		;
	}

	public @Test void nestedImportsAreSupported() {
		class Imported2 { @Bean TestBean queen() { return new TestBean(); } }
		@Import(Imported2.class)
		class Imported1 { @Bean TestBean alice() { return new TestBean(); } }
		@Import(Imported1.class)
		class Config { @Bean TestBean knave() { return new TestBean(); } }
		configClass = Config.class;

		targetModel
			.addConfigurationClass(
				new ConfigurationClass(Imported2.class.getName())
					.addBeanMethod(new BeanMethod("queen")))
			.addConfigurationClass(
				new ConfigurationClass(Imported1.class.getName())
					.addBeanMethod(new BeanMethod("alice")))
			.addConfigurationClass(
				new ConfigurationClass(Config.class.getName())
					.addBeanMethod(new BeanMethod("knave")))
		;
	}

	public @Test void nestedImportsAreProcessedDepthFirst() {
		class Imported3 { @Bean TestBean rabbit() { return new TestBean(); } }
		class Imported2 { @Bean TestBean queen() { return new TestBean(); } }
		@Import(Imported2.class)
		class Imported1 { @Bean TestBean alice() { return new TestBean(); } }
		@Import({Imported1.class, Imported3.class})
		class Config { @Bean TestBean knave() { return new TestBean(); } }
		configClass = Config.class;

		targetModel
			.addConfigurationClass(
				new ConfigurationClass(Imported2.class.getName())
					.addBeanMethod(new BeanMethod("queen")))
			.addConfigurationClass(
				new ConfigurationClass(Imported1.class.getName())
					.addBeanMethod(new BeanMethod("alice")))
			.addConfigurationClass(
				new ConfigurationClass(Imported3.class.getName())
					.addBeanMethod(new BeanMethod("rabbit")))
			.addConfigurationClass(
				new ConfigurationClass(Config.class.getName())
					.addBeanMethod(new BeanMethod("knave")))
		;
	}


	public @Test void publicBeanMethodModifierIsSupported() {
		class Config {
			public @Bean TestBean a() { return new TestBean(); }
			public final @Bean TestBean b() { return new TestBean(); }
		}
		configClass = Config.class;
		targetModel
			.addConfigurationClass(
				new ConfigurationClass(Config.class.getName())
					.addBeanMethod(new BeanMethod("a", Modifier.PUBLIC))
					.addBeanMethod(new BeanMethod("b", Modifier.PUBLIC + Modifier.FINAL))
    			)
    		;
	}


	// -----------------------------------------------
	// Concrete test case implementations (one per implementation of JavaConfigurationModelPopulator)
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
