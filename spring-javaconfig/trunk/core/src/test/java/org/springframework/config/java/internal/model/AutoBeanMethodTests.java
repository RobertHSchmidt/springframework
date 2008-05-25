package org.springframework.config.java.internal.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.extractMethodAnnotation;

import java.lang.reflect.Modifier;

import org.junit.Test;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.internal.util.MethodAnnotationPrototype;
import org.springframework.config.java.model.ModelClass;

/**
 * Unit tests for {@link AutoBeanMethod}
 *
 * @author Chris Beams
 */
public class AutoBeanMethodTests {

	static AutoBean DEFAULT_METADATA = extractMethodAnnotation(AutoBean.class, new MethodAnnotationPrototype() {
		public @AutoBean void targetMethod() { } }.getClass());

	static AutoBean CUSTOM_METADATA = extractMethodAnnotation(AutoBean.class, new MethodAnnotationPrototype() {
		public @AutoBean(autowire=Autowire.BY_NAME) void targetMethod() { } }.getClass());

	static final ModelClass DEFAULT_RETURN_TYPE = ModelClass.forClass(TestBean.class);
	static final AutoBeanMethod VALID_AUTOBEAN_METHOD = new AutoBeanMethod("m", DEFAULT_RETURN_TYPE, 0, DEFAULT_METADATA);

	public @Test void testConstruction() {
		AutoBeanMethod m = new AutoBeanMethod("a", DEFAULT_RETURN_TYPE, 0, DEFAULT_METADATA);
	}

	public @Test void equality() {
		AutoBeanMethod a1 = new AutoBeanMethod("a", DEFAULT_RETURN_TYPE, 0, DEFAULT_METADATA);
		AutoBeanMethod a2 = new AutoBeanMethod("a", DEFAULT_RETURN_TYPE, 0, DEFAULT_METADATA);
		assertThat(a1, equalTo(a2));
		assertThat(a2, equalTo(a1));

		a2 = new AutoBeanMethod("b", DEFAULT_RETURN_TYPE, 0, DEFAULT_METADATA);
		assertThat(a1, not(equalTo(a2)));
		assertThat(a2, not(equalTo(a1)));

		a2 = new AutoBeanMethod("a", DEFAULT_RETURN_TYPE, 0, CUSTOM_METADATA);
		assertThat(a1, not(equalTo(a2)));
		assertThat(a2, not(equalTo(a1)));

		a2 = new AutoBeanMethod("a", DEFAULT_RETURN_TYPE, Modifier.PUBLIC, DEFAULT_METADATA);
		assertThat(a1, not(equalTo(a2)));
		assertThat(a2, not(equalTo(a1)));

		ModelClass iface = ModelClass.forClass(ITestBean.class);
		a2 = new AutoBeanMethod("a", iface, 0, DEFAULT_METADATA);
		assertThat(a1, not(equalTo(a2)));

		assertThat(a1, equalTo(a1));
		assertThat(a1, not(equalTo(null)));
		assertThat(null, not(equalTo(a1)));
		assertThat(a1, not(equalTo(new Object())));
	}

	public @Test void validity() {
		{ // valid case
			AutoBeanMethod valid = VALID_AUTOBEAN_METHOD;
			ValidationErrors errors = new ValidationErrors();
			valid.validate(errors);
			assertEquals(0, errors.size());
		}

		{ // invalid because return type is interface
			ModelClass rtInvalid = ModelClass.forClass(ITestBean.class);
			AutoBeanMethod valid = new AutoBeanMethod("invalid", rtInvalid, 0, DEFAULT_METADATA);
			ValidationErrors errors = new ValidationErrors();
			valid.validate(errors);
			assertEquals(errors.toString(), 1, errors.size());
			assertTrue(errors.get(0).contains(ValidationError.AUTOBEAN_MUST_BE_CONCRETE_TYPE.toString()));
		}
	}

	public @Test void testAccessors() {
		AutoBeanMethod m = new AutoBeanMethod("a", DEFAULT_RETURN_TYPE, 0, DEFAULT_METADATA);
		assertThat(m.getName(), equalTo("a"));
		assertThat(m.getMetadata(), equalTo(DEFAULT_METADATA));
		assertThat(m.getReturnType(), equalTo(DEFAULT_RETURN_TYPE));
	}
}
