package org.springframework.config.java.internal.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.springframework.config.java.util.AnnotationExtractionUtils.extractMethodAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.internal.model.BeanMethod;
import org.springframework.config.java.internal.model.ValidationError;
import org.springframework.config.java.internal.model.ValidationErrors;
import org.springframework.config.java.util.MethodAnnotationPrototype;


public class BeanMethodTests {

	public static final Bean FINAL_BEAN_ANNOTATION =
		extractMethodAnnotation(Bean.class,
			new MethodAnnotationPrototype() {
        		@Bean(allowOverriding=false)
        		public void targetMethod() {}
    		}.getClass());

	static final Bean DEFAULT_METADATA = extractMethodAnnotation(Bean.class,
			new MethodAnnotationPrototype() { public @Bean void targetMethod() {} }.getClass());

	public static final BeanMethod VALID_BEAN_METHOD = new BeanMethod("m", 0, DEFAULT_METADATA);

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

	public @Test void unknownAnnotationCausesIncompatibilityError() {
		Bean beanAnno = DEFAULT_METADATA;
		Bogus unknownAnno = extractMethodAnnotation(Bogus.class, new MethodAnnotationPrototype() {
			public @Bogus void targetMethod() { }
		}.getClass());

		BeanMethod method = new BeanMethod("m", 0, beanAnno, unknownAnno);
		ValidationErrors errors = new ValidationErrors();
		method.validate(errors);
		assertTrue(errors.get(0).contains(ValidationError.INCOMPATIBLE_ANNOTATION.toString()));
	}


	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	static @interface Bogus { }
}
