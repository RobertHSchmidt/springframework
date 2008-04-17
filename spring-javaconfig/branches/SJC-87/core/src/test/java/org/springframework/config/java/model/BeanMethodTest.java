package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Modifier;

import org.junit.Test;


public class BeanMethodTest {
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

		assertFalse(Modifier.isPublic(modifiers));
		assertFalse(Modifier.isPrivate(modifiers));
		assertFalse(Modifier.isProtected(modifiers));
		assertFalse(Modifier.isFinal(modifiers));
		assertFalse(Modifier.isStatic(modifiers));
		assertFalse(Modifier.isAbstract(modifiers));
	}
}
