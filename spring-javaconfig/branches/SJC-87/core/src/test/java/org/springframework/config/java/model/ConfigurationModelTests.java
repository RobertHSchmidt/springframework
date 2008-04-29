package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.config.java.process.MalformedJavaConfigurationException;

/**
 * Primarily exists to test equals() implementation of model objects
 *
 * @author Chris Beams
 */
public class ConfigurationModelTests {

	// -- equivalence tests ---------------------------------------------------

	public @Test void emptyModelsAreEqual() {
		ConfigurationModel modelA = new ConfigurationModel();
		ConfigurationModel modelB = new ConfigurationModel();

		assertThat(modelA, equalTo(modelB));
	}

	public @Test void emptyVsNonEmptyModelsAreNotEqual() {
		ConfigurationModel empty = new ConfigurationModel();
		ConfigurationModel nonEmpty = new ConfigurationModel().add(new ConfigurationClass("a"));

		assertThat(empty, not(equalTo(nonEmpty)));
	}

	public @Test void equivalentModelsAreEqual() {
		ConfigurationModel modelA = new ConfigurationModel().add(new ConfigurationClass("a"));
		ConfigurationModel modelB = new ConfigurationModel().add(new ConfigurationClass("a"));

		assertThat(modelA, equalTo(modelB));
	}

	public @Test void nonEquivalentModelsAreNotEqual() {
		ConfigurationModel modelA = new ConfigurationModel().add(new ConfigurationClass("a"));
		ConfigurationModel modelB = new ConfigurationModel().add(new ConfigurationClass("b"));

		assertThat(modelA, not(equalTo(modelB)));
	}

	public @Test void equivalentDeepModelsAreEqual() {
		class util {
			ConfigurationModel createDeepModel() {
				return new ConfigurationModel()
					.add(new ConfigurationClass("classX")
						.add(new BeanMethod("methodX")));
			}
		}
		ConfigurationModel modelA = new util().createDeepModel();
		ConfigurationModel modelB = new util().createDeepModel();

		assertThat(modelA, not(sameInstance(modelB)));
		assertThat(modelA, equalTo(modelB));
	}

	/**
	 * The order that classes are introduced into the model is significant - it
	 * will determine precedence in bean overriding cases.
	 */
	public @Test void configClassOrderIsSignificant() {
		ConfigurationModel modelA = new ConfigurationModel()
			.add(new ConfigurationClass("classA"))
			.add(new ConfigurationClass("classB"));

		ConfigurationModel modelB = new ConfigurationModel()
			.add(new ConfigurationClass("classB"))
			.add(new ConfigurationClass("classA"));

		assertThat(modelA, not(equalTo(modelB)));
	}

	/**
	 * The order that Bean methods are introduced to the model is not significant -
	 * given the constraints of java classes, they are guaranteed to be unique within
	 * any given class, so precedence with regard to bean overriding is not a concern.
	 *
	 * @see {@link #configClassOrderIsSignificant()}
	 * @see {@link ConfigurationParserTests#beanMethodOrderIsNotSignificantA()}
	 */
	public @Test void beanMethodOrderIsNotSignificant() {
		ConfigurationModel modelA = new ConfigurationModel()
			.add(new ConfigurationClass("classA")
				.add(new BeanMethod("methodA"))
				.add(new BeanMethod("methodB")))
			.add(new ConfigurationClass("classB")
				.add(new BeanMethod("methodC"))
				.add(new BeanMethod("methodD")));

		ConfigurationModel modelB = new ConfigurationModel()
			.add(new ConfigurationClass("classA")
				.add(new BeanMethod("methodA"))
				.add(new BeanMethod("methodB")))
			.add(new ConfigurationClass("classB")
				.add(new BeanMethod("methodD")) // only difference!
				.add(new BeanMethod("methodC")));

		assertThat(modelA, equalTo(modelB));
	}

	/**
	 * Tests that ConfigurationModel's {@link ConfigurationModel#equals(Object)} method
	 * considers aspect classes when determining equivalence
	 */
	public @Test void equalsConsidersAspectClasses() {
		ConfigurationModel m1 = new ConfigurationModel().add(new AspectClass("a"));
		ConfigurationModel m2 = new ConfigurationModel();
		assertFalse(m1.equals(m2));
		assertFalse(m2.equals(m1));
		m2.add(new AspectClass("a"));
		assertTrue(m1.equals(m2));
		assertTrue(m2.equals(m1));
	}

	/**
	 * The order in which Aspect classes are introduced to the model is not
	 * significant - this is why aspectClasses is implemented as a HashSet.
	 */
	public @Test void aspectClassOrderIsNotSignificant() {
		ConfigurationModel m1 = new ConfigurationModel().add(new AspectClass("a")).add(new AspectClass("b"));
		ConfigurationModel m2 = new ConfigurationModel().add(new AspectClass("b")).add(new AspectClass("a"));
		assertEquals(m1, m2);
	}

	// -- validation tests ---------------------------------------------------

	public @Test void modelWithNoConfigurationClassesIsNotValid() {
		ConfigurationModel model = new ConfigurationModel();

		ValidationErrors errors = model.validate();
		assertEquals("had more errors than expected: " + errors, 1, errors.size());
		assertTrue(errors.get(0).contains(ValidationError.MODEL_IS_EMPTY.toString()));
	}

	public @Test void validateCascadesToConfigurationClasses() {
		// configuration class is invalid (no beans defined), so should bubble up as an error at the top level
		ValidationErrors errors = new ConfigurationModel().add(ConfigurationClassTests.INVALID_CONFIGURATION_CLASS).validate();
		assertTrue("should have reflected nested validation errors", errors.size() > 0);
	}

	public @Test void validateCascadesToAspectClasses() {
		ValidationErrors errors = new ConfigurationModel().add(AspectClassTests.INVALID_ASPECT_CLASS).validate();
		assertTrue("should have reflected nested validation errors", errors.size() > 0);
	}


	public @Test void validationChecksForIllegalBeanOverrides() {
		ConfigurationModel model = new ConfigurationModel()
			.add(new ConfigurationClass("a").add(new BeanMethod("m")))
			.add(new ConfigurationClass("b").add(new BeanMethod("m", BeanMethodTests.FINAL_BEAN_ANNOTATION)))
			.add(new ConfigurationClass("c").add(new BeanMethod("m")))
		;

		try {
    		model.assertIsValid();
    		fail("should have thrown exception - configuration " +
				 "class 'c' illegally overrides final bean method 'm' declared in class 'b'");
		} catch (MalformedJavaConfigurationException ex) {
			assertTrue("got unexpected exception message: " + ex.getMessage(),
				ex.getMessage().contains(ValidationError.ILLEGAL_BEAN_OVERRIDE.toString()));
		}
	}

	public @Test void validationChecksForIllegalBeanOverridesIncludingImports() {
		ConfigurationModel model = new ConfigurationModel()
			.add(new ConfigurationClass("a").add(new BeanMethod("m")))
			.add(new ConfigurationClass("b").add(new BeanMethod("m", BeanMethodTests.FINAL_BEAN_ANNOTATION)))
			.add(new ConfigurationClass("c").addImportedClass(new ConfigurationClass("i").add(new BeanMethod("m"))))
		;

		try {
    		model.assertIsValid();
    		fail("should have thrown exception - imported configuration " +
				 "class 'i' illegally overrides bean method 'm' declared in class 'b'");
		} catch (MalformedJavaConfigurationException ex) {
			assertTrue("got unexpected exception message: " + ex.getMessage(),
				ex.getMessage().contains(ValidationError.ILLEGAL_BEAN_OVERRIDE.toString()));
		}
	}

	/**
	 * A ConfigurationModel must consist of at least one ConfigurationClass to be valid.
	 * If the user has supplied only Aspect classes, validation should fail
	 */
	public @Test void modelContainingOnlyAspectClassesIsInvalid() {
		ConfigurationModel m = new ConfigurationModel().add(new AspectClass("a"));

		ValidationErrors errors = m.validate();
		assertEquals("expected one error", 1, errors.size());
		assertTrue(errors.get(0).contains(ValidationError.MODEL_IS_EMPTY.toString()));
	}


}
