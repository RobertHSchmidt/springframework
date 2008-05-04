package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.springframework.config.java.model.AnnotationExtractionUtils.extractMethodAnnotation;

import java.lang.reflect.Modifier;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.type.ReflectiveType;
import org.springframework.config.java.type.Type;

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

	static final Type DEFAULT_RETURN_TYPE = new ReflectiveType(TestBean.class);
	static final AutoBeanMethod VALID_AUTOBEAN_METHOD = new AutoBeanMethod("m", DEFAULT_METADATA, DEFAULT_RETURN_TYPE, 0);

	public @Test void testConstruction() {
		AutoBeanMethod m = new AutoBeanMethod("a", DEFAULT_METADATA, DEFAULT_RETURN_TYPE, 0);
	}

	public @Test void equality() {
		AutoBeanMethod a1 = new AutoBeanMethod("a", DEFAULT_METADATA, DEFAULT_RETURN_TYPE, 0);
		AutoBeanMethod a2 = new AutoBeanMethod("a", DEFAULT_METADATA, DEFAULT_RETURN_TYPE, 0);
		assertThat(a1, equalTo(a2));
		assertThat(a2, equalTo(a1));

		a2 = new AutoBeanMethod("b", DEFAULT_METADATA, DEFAULT_RETURN_TYPE, 0);
		assertThat(a1, not(equalTo(a2)));
		assertThat(a2, not(equalTo(a1)));

		a2 = new AutoBeanMethod("a", CUSTOM_METADATA, DEFAULT_RETURN_TYPE, 0);
		assertThat(a1, not(equalTo(a2)));
		assertThat(a2, not(equalTo(a1)));

		a2 = new AutoBeanMethod("a", DEFAULT_METADATA, DEFAULT_RETURN_TYPE, Modifier.PUBLIC);
		assertThat(a1, not(equalTo(a2)));
		assertThat(a2, not(equalTo(a1)));

		assertThat(a1, equalTo(a1));
		assertThat(a1, not(equalTo(null)));
		assertThat(null, not(equalTo(a1)));
		assertThat(a1, not(equalTo(new Object())));
	}

	public @Test void getReturnType() {
		AutoBeanMethod m = new AutoBeanMethod("a", DEFAULT_METADATA, DEFAULT_RETURN_TYPE, 0);
		assertThat(m.getReturnType(), equalTo(DEFAULT_RETURN_TYPE));
	}
}
