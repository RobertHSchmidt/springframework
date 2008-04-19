package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.lang.reflect.Modifier;

import org.junit.Test;
import org.springframework.config.java.annotation.Bean;


public class BeanMethodTests {

	public static final Bean FINAL_BEAN_ANNOTATION;
	static {
		class c { @Bean(allowOverriding=false) void m() { } }
		try { FINAL_BEAN_ANNOTATION = c.class.getDeclaredMethod("m").getAnnotation(Bean.class); }
		catch (Exception ex) { throw new RuntimeException(ex); }
	}

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
		assertTrue(errors.get(0).contains(ValidationError.METHOD_MAY_NOT_BE_PRIVATE.toString()));
	}
}
