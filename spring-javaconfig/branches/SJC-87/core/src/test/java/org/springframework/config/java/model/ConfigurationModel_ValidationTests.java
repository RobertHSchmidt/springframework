package org.springframework.config.java.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.config.java.process.MalformedJavaConfigurationException;

public class ConfigurationModel_ValidationTests {

	public @Test void modelWithNoConfigurationClassesIsNotValid() {
		ConfigurationModel model = new ConfigurationModel();

		ValidationErrors errors = model.validate();
		assertEquals("had more errors than expected: " + errors, 1, errors.size());
		assertTrue(errors.get(0).contains(ValidationError.MODEL_IS_EMPTY.toString()));
	}

	public @Test void validateCascadesToChildObjects() {
		// configuration class is invalid (no beans defined), so should bubble up as an error at the top level
		ValidationErrors errors = new ConfigurationModel().add(new ConfigurationClass("a")).validate();
		assertTrue("should have reflected nested errors", errors.size() > 0);
	}

	/**
	 * TODO: this is probably unnecessary duplication
	 * @see {@link ConfigurationClassTests#getFinalBeanMethods()}
	 */
	public @Test void validationChecksForIllegalBeanOverrides() {
		ConfigurationModel model = new ConfigurationModel()
			.add(new ConfigurationClass("a").add(new BeanMethod("m")))
			.add(new ConfigurationClass("b").add(new BeanMethod("m", BeanMethodTests.FINAL_BEAN_ANNOTATION)))
			.add(new ConfigurationClass("c").add(new BeanMethod("m")))
		;

		try {
    		model.assertIsValid();
    		fail("should have thrown, model is invalid");
		} catch (MalformedJavaConfigurationException ex) {
			// all good
		}
	}


}
