package org.springframework.config.java.model;

import static org.junit.Assert.*;

import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;

public class AspectClassTests {
	/**
	 * Example of an invalid aspect class, for use in higher-level tests.
	 * (is invalid because it does not have an explicit Aspect annotation)
	 */
	static AspectClass INVALID_ASPECT_CLASS = new AspectClass("a");

	private static @Aspect class Prototype { }
	private static Aspect DEFAULT_ASPECT_ANNOTATION =  AnnotationExtractionUtils.extractClassAnnotation(Aspect.class, Prototype.class);

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
