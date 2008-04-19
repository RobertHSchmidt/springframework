package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.lang.reflect.Modifier;

import org.junit.Test;


public class ExternalBeanMethodTests {

	// ------------------------------
	// Equivalence tests
	// ------------------------------
	public @Test void equivalentMethodsAreEqual() {
		ExternalBeanMethod methodA = new ExternalBeanMethod("foo");
		ExternalBeanMethod methodB = new ExternalBeanMethod("foo");

		assertThat(methodA, equalTo(methodB));
	}

	public @Test void methodsWithDifferentModifiersAreNotEqual() {
		ExternalBeanMethod methodA = new ExternalBeanMethod("foo");
		ExternalBeanMethod methodB = new ExternalBeanMethod("foo", Modifier.PUBLIC);

		assertThat(methodA, not(equalTo(methodB)));
	}

	/*
	 * creating a new ExternalBeanMethod("foo") is equivalent to a class that declares:
	 *
	 *     class Config {
	 *         @ExternalBean TestBean foo() { ... }
	 *     }
	 */
	public @Test void byDefaultMethodShouldHaveNoModifiers() {
		ExternalBeanMethod method = new ExternalBeanMethod("foo");

		int modifiers = method.getModifiers();

		// 0 signifies 'no modifiers' - see java.lang.reflect.Modifier
		assertEquals(0, modifiers);
	}

	// ------------------------------
	// Validation tests
	// ------------------------------

	public @Test void privateExternalBeanMethodsAreNotValid() {
		ValidationErrors errors = new ExternalBeanMethod("foo", Modifier.PRIVATE).validate(new ValidationErrors());
		assertTrue(errors.get(0).contains(ValidationError.METHOD_MAY_NOT_BE_PRIVATE.toString()));
	}
}
