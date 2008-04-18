package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.lang.reflect.Modifier;

import org.junit.Test;


public class BeanMethodTests {

	// ------------------------------
	// Equivalence tests
	// ------------------------------
	public @Test void equivalentMethodsAreEqual() {
		BeanMethod methodA = new BeanMethod("foo");
		BeanMethod methodB = new BeanMethod("foo");

		assertThat(methodA, equalTo(methodB));
	}

	public @Test void methodsWithDifferentModifiersAreNotEqual() {
		BeanMethod methodA = new BeanMethod("foo");
		BeanMethod methodB = new BeanMethod("foo", Modifier.PUBLIC);

		assertThat(methodA, not(equalTo(methodB)));
	}

	/*
	 * creating a new BeanMethod("foo") is equivalent to a class that declares:
	 *
	 *     class Config {
	 *         @Bean TestBean foo() { ... }
	 *     }
	 */
	public @Test void byDefaultMethodShouldHaveNoModifiers() {
		BeanMethod method = new BeanMethod("foo");

		int modifiers = method.getModifiers();

		// 0 signifies 'no modifiers' - see java.lang.reflect.Modifier
		assertEquals(0, modifiers);
	}

	// ------------------------------
	// Validation tests
	// ------------------------------

	public @Test void privateBeanMethodsAreNotValid() {
		ValidationErrors errors = new BeanMethod("foo", Modifier.PRIVATE).validate(new ValidationErrors());
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationError.BEAN_METHOD_MAY_NOT_BE_PRIVATE));
	}
}
