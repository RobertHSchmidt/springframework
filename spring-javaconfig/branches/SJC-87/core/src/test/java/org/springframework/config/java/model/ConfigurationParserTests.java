package org.springframework.config.java.model;

import static org.junit.Assert.*;

import java.lang.reflect.Modifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Import;

/**
 * Integration test for {@link ConfigurationParser} implementations.
 *
 * <p>Contract for each test:
 * <ul>
 * <li>define a configuration class and assign that class literal to <tt>configClass</tt></li>
 * <li>populate <tt>expectedModel</tt> with the expected results of processing <tt>configClass</tt></li>
 * </ul>
 *
 * {@link #initializeModelsAndParser()} will initialize model objects and call back to
 * {@link newPopulator()} to instantiate a new parser.
 * {@link #populateResultModelAndCompareToTargetModel()} will ensure that the result of processing
 * <tt>configClass</tt> against the parser produces a model equivalent to <tt>expectedModel</tt>
 *
 * @author Chris Beams
 */
public abstract class ConfigurationParserTests {

	private Class<?> configClass;
	private ConfigurationModel actualModel;
	private ConfigurationModel expectedModel;
	private ConfigurationParser parser;

	/**
	 * Each concrete subclass must implement this method in order to receive a callback in
	 * {@link #initializeModelsAndParser()}
	 */
	protected abstract ConfigurationParser newParser(ConfigurationModel model);

	@Before
	public void initializeModelsAndParser() {
		actualModel = new ConfigurationModel();
		expectedModel = new ConfigurationModel();
		parser = newParser(actualModel);
	}

	@After
	public void populateResultModelAndCompareToTargetModel() {
		assertNotNull("configClass has not been set for this test", configClass);
		assertTrue("expectedModel has not been populated for this test",
				expectedModel.getConfigurationClasses().length > 0);

		parser.parse(configClass);

		assertEquals("models were not equivalent", expectedModel, actualModel);
	}


	// -----------------------------------------------
	// Individual tests
	// -----------------------------------------------

	public @Test void simplestPossibleConfigDefinition() {
		class Config { }
		configClass = Config.class;

		expectedModel.add(new ConfigurationClass(Config.class.getName()));
	}

	public @Test void beanMethodsAreRecognized() {
		class Config { @Bean TestBean alice() { return new TestBean(); } }
		configClass = Config.class;

		expectedModel.add(
			new ConfigurationClass(Config.class.getName())
				.add(new BeanMethod("alice")));
	}

	public @Test void nonBeanMethodsAreIgnored() {
		class Config {
			@Bean TestBean alice() { return new TestBean(); }
			TestBean knave() { return new TestBean(); }
		}
		configClass = Config.class;

		expectedModel.add(
			new ConfigurationClass(Config.class.getName())
				.add(new BeanMethod("alice")));
	}


	class BeanMethodOrderConfig {
		@Bean TestBean alice() { return new TestBean(); }
		@Bean TestBean knave() { return new TestBean(); }
	}
	public @Test void beanMethodOrderIsNotSignificantA() {
		configClass = BeanMethodOrderConfig.class;

		expectedModel.add(
			new ConfigurationClass(BeanMethodOrderConfig.class.getName())
				.add(new BeanMethod("alice"))
				.add(new BeanMethod("knave"))
			);
	}
	public @Test void beanMethodOrderIsNotSignificantB() {
		configClass = BeanMethodOrderConfig.class;

		expectedModel.add(
			new ConfigurationClass(BeanMethodOrderConfig.class.getName())
				.add(new BeanMethod("knave"))
				.add(new BeanMethod("alice"))
			);
	}

	public @Test void importIsRecognized() {
		class Imported { @Bean TestBean alice() { return new TestBean(); } }
		@Import(Imported.class)
		class Config { @Bean TestBean knave() { return new TestBean(); } }
		configClass = Config.class;

		expectedModel
			.add(new ConfigurationClass(Config.class.getName()).add(new BeanMethod("knave"))
				.addImportedClass(new ConfigurationClass(Imported.class.getName()).add(new BeanMethod("alice"))));
	}

	public @Test void multipleImportsAreSupported() {
		class Imported1 { @Bean TestBean alice() { return new TestBean(); } }
		class Imported2 { @Bean TestBean queen() { return new TestBean(); } }
		@Import({Imported1.class, Imported2.class})
		class Config { @Bean TestBean knave() { return new TestBean(); } }
		configClass = Config.class;

		expectedModel
			.add(new ConfigurationClass(Config.class.getName()).add(new BeanMethod("knave"))
				.addImportedClass(new ConfigurationClass(Imported1.class.getName()).add(new BeanMethod("alice")))
				.addImportedClass(new ConfigurationClass(Imported2.class.getName()).add(new BeanMethod("queen"))));
	}

	public @Test void nestedImportsAreSupported() {
		class Imported2 { @Bean TestBean queen() { return new TestBean(); } }
		@Import(Imported2.class)
		class Imported1 { @Bean TestBean alice() { return new TestBean(); } }
		@Import(Imported1.class)
		class Config { @Bean TestBean knave() { return new TestBean(); } }
		configClass = Config.class;

		expectedModel
			.add(new ConfigurationClass(Config.class.getName()).add(new BeanMethod("knave"))
				.addImportedClass(new ConfigurationClass(Imported1.class.getName()).add(new BeanMethod("alice"))
					.addImportedClass(new ConfigurationClass(Imported2.class.getName()).add(new BeanMethod("queen")))));
	}

	public @Test void nestedImportsAreSupported2() {
		class Imported3 { @Bean TestBean rabbit() { return new TestBean(); } }
		class Imported2 { @Bean TestBean queen() { return new TestBean(); } }
		@Import(Imported2.class)
		class Imported1 { @Bean TestBean alice() { return new TestBean(); } }
		@Import({Imported1.class, Imported3.class})
		class Config { @Bean TestBean knave() { return new TestBean(); } }
		configClass = Config.class;

		expectedModel
			.add(new ConfigurationClass(Config.class.getName()).add(new BeanMethod("knave"))
				.addImportedClass(new ConfigurationClass(Imported1.class.getName()).add(new BeanMethod("alice"))
					.addImportedClass(new ConfigurationClass(Imported2.class.getName()).add(new BeanMethod("queen"))))
				.addImportedClass(new ConfigurationClass(Imported3.class.getName()).add(new BeanMethod("rabbit"))))
			;
	}

	public @Test void variousBeanMethodModifiersAreSupported() {
		class Config {
			public @Bean TestBean a() { return new TestBean(); }
			public final @Bean TestBean b() { return new TestBean(); }
			private strictfp @Bean TestBean c() { return new TestBean(); }
		}
		configClass = Config.class;
		expectedModel
			.add(
				new ConfigurationClass(Config.class.getName())
					.add(new BeanMethod("a", Modifier.PUBLIC))
					.add(new BeanMethod("b", Modifier.PUBLIC + Modifier.FINAL))
					.add(new BeanMethod("c", Modifier.PRIVATE + Modifier.STRICT))
    			)
    		;
	}

	public @Test void externalBeanMethodsAreSupported() {
		class Config {
			@Bean TestBean bean() { return new TestBean(); }
			@ExternalBean TestBean extbean() { return new TestBean(); }
		}
		configClass = Config.class;
		expectedModel
			.add(
				new ConfigurationClass(Config.class.getName())
					.add(new BeanMethod("bean"))
					.add(new ExternalBeanMethod("extbean"))
				);
	}


	public @Test void declaringClassesAreSupported() {
		class Main {
			class OuterConfig {
				@Bean TestBean b() { return new TestBean(); }
				class InnerConfig {
					@Bean TestBean a() { return new TestBean(); }
				}
			}
		}

		configClass = Main.OuterConfig.InnerConfig.class;
		expectedModel.add(
			new ConfigurationClass(Main.OuterConfig.InnerConfig.class.getName())
				.add(new BeanMethod("a"))
				.setDeclaringClass(
					new PotentialConfigurationClass(Main.OuterConfig.class.getName())
						.add(new BeanMethod("b"))
						.setDeclaringClass(
							new PotentialConfigurationClass(Main.class.getName()))));
	}

	// often during testing, classes are declared as member classes within a JUnit test case.
	// this is a convenience, but adversely affects equality comparisons on model objects,
	// because the declaring TestCase class shows up in the model parsed from the literal class.
	// for this reason it is important that parser implementations explicitly ignore declaring
	// classes when they are test cases.  In a JUnit4 context, the only reasonable way to do this
	// is by comparing names.  If the declaring class ends in Test/Tests, ignore it.
	public @Test void ignoreDeclaringClassIfEndsInTests() {
		abstract class FooTests {
			class MemberConfigClass { @Bean TestBean m() { return new TestBean(); } }
		}
		configClass = FooTests.MemberConfigClass.class;

		// note: expectedModel does NOT include a declaring class
		expectedModel.add(new ConfigurationClass(FooTests.MemberConfigClass.class.getName()).add(new BeanMethod("m")));
	}
	public @Test void ignoreDeclaringClassIfEndsInTest() {
		class FooTest {
			class MemberConfigClass { @Bean TestBean m() { return new TestBean(); } }
		}
		configClass = FooTest.MemberConfigClass.class;

		// note: expectedModel does NOT include a declaring class
		expectedModel.add(new ConfigurationClass(FooTest.MemberConfigClass.class.getName()).add(new BeanMethod("m")));
	}
	public @Test void includeDeclaringClass() {
		class FooTestsX {
			class MemberConfigClass { @Bean TestBean m() { return new TestBean(); } }
		}
		configClass = FooTestsX.MemberConfigClass.class;

		// note: expectedModel DOES include a declaring class
		expectedModel.add(new ConfigurationClass(FooTestsX.MemberConfigClass.class.getName())
						.add(new BeanMethod("m"))
						.setDeclaringClass(new PotentialConfigurationClass(FooTestsX.class.getName())));
	}


	// -----------------------------------------------
	// Concrete test case implementations (one per implementation of JavaConfigurationModelPopulator)
	// -----------------------------------------------
	public static class ReflectingConfigurationParserTests extends ConfigurationParserTests {
		@Override
		protected ConfigurationParser newParser(ConfigurationModel model) {
			return new ReflectingConfigurationParser(model);
		}
	}

	/* TODO: implement and uncomment
	public static class AsmConfigurationParserTests extends JavaConfigurationModelPopulatorTests {
		@Override
		protected ConfigurationParser newParser(ConfigurationModel model) {
			return new AsmConfigurationParser(model);
		}
	}
	*/
}
