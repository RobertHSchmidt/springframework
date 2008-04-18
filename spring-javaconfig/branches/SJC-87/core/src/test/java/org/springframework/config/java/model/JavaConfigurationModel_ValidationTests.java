package org.springframework.config.java.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JavaConfigurationModel_ValidationTests {

	public @Test void modelWithNoConfigurationClassesIsNotValid() {
		JavaConfigurationModel model = new JavaConfigurationModel();

		ValidationErrors errors = model.validate();
		assertEquals("had more errors than expected: " + errors, 1, errors.size());
		assertEquals(ValidationError.MODEL_IS_EMPTY, errors.get(0));
	}

	public @Test void validateCascadesToChildObjects() {
		// configuration class is invalid (no beans defined), so should bubble up as an error at the top level
		ValidationErrors errors = new JavaConfigurationModel().addConfigurationClass(new ConfigurationClass("a")).validate();
		assertTrue("should have reflected nested errors", errors.size() > 0);
	}

}
