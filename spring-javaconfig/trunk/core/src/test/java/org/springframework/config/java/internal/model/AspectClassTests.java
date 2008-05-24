package org.springframework.config.java.internal.model;

import static org.junit.Assert.*;

import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;
import org.springframework.config.java.internal.model.AspectClass;
import org.springframework.config.java.internal.model.ValidationError;
import org.springframework.config.java.internal.model.ValidationErrors;
import org.springframework.config.java.internal.util.AnnotationExtractionUtils;

public class AspectClassTests {

	private static @Aspect class Prototype { }
	private static Aspect DEFAULT_ASPECT_ANNOTATION =  AnnotationExtractionUtils.extractClassAnnotation(Aspect.class, Prototype.class);

	/**
	 * Example of an invalid aspect class, for use in higher-level tests.
	 * (is invalid because it does not have an explicit Aspect annotation)
	 */
	static final AspectClass INVALID_ASPECT_CLASS = new AspectClass("a");

	/**
	 * Example of a valid aspect class, for use in higher-level tests.
	 * (is invalid because it does not have an explicit Aspect annotation)
	 */
	static final AspectClass VALID_ASPECT_CLASS = new AspectClass("a", DEFAULT_ASPECT_ANNOTATION);


	public @Test void validateAspectClassMustHaveExplicitAspectAnnotation() {
		// test the negative case
		ValidationErrors errors = INVALID_ASPECT_CLASS.validate(new ValidationErrors());
		assertEquals(1, errors.size());
		assertTrue(errors.get(0).contains(ValidationError.ASPECT_CLASS_MUST_HAVE_ASPECT_ANNOTATION.toString()));

		// test the positive case
		ValidationErrors errors2 = new AspectClass("a", DEFAULT_ASPECT_ANNOTATION).validate(new ValidationErrors());
		assertEquals(0, errors2.size());
	}

	public @Test void nameEquality() {
		AspectClass a1 = new AspectClass("a1");
		AspectClass a2 = new AspectClass("a2");
		assertFalse(a1.equals(a2));
		assertFalse(a2.equals(a1));
	}

	public @Test void metadataEquality() {
		AspectClass a1 = new AspectClass("a");
		AspectClass a2 = new AspectClass("a");
		assertTrue(a1.equals(a2));
		assertTrue(a2.equals(a1));
		a1.setMetadata(DEFAULT_ASPECT_ANNOTATION);
		assertFalse(a1.equals(a2));
		assertFalse(a2.equals(a1));
		a2.setMetadata(DEFAULT_ASPECT_ANNOTATION);
		assertTrue(a1.equals(a2));
		assertTrue(a2.equals(a1));
	}
}
