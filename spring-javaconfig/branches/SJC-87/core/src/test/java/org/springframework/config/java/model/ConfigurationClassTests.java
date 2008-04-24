package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.springframework.config.java.model.AnnotationExtractionUtils.extractClassAnnotation;
import static org.springframework.config.java.model.ValidationError.*;

import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.Configuration;

public class ConfigurationClassTests {

	private ConfigurationClass configClass;

	@Before
	public void setUp() {
		configClass = new ConfigurationClass("c");
	}

	public @Test void modifiers() {
		assertEquals("should have no modifiers by default", 0, new ConfigurationClass("c").getModifiers());

		assertEquals("all modifiers should be preserved",
				Modifier.ABSTRACT, new ConfigurationClass("c", Modifier.ABSTRACT).getModifiers());
	}

	/**
	 * If a Configuration class is not explicitly annotated with
	 * {@link Configuration @Configuration}, a default instance
	 * of the annotation should be applied.  Essentially, Configuration
	 * metadata should never be null.
	 */
	public @Test void defaultConfigurationMetadataIsAlwaysPresent() {
		ConfigurationClass c = new ConfigurationClass("c");
		assertNotNull("default metadata is not present", c.getMetadata());
	}

	public @Test void getFinalBeanMethods() {
		BeanMethod finalBeanMethod = new BeanMethod("y", BeanMethodTests.FINAL_BEAN_ANNOTATION);
    	configClass
    		.add(new BeanMethod("x"))
    		.add(finalBeanMethod)
    		.add(new BeanMethod("z"))
    	;

    	assertArrayEquals(new BeanMethod[] { finalBeanMethod }, configClass.getFinalBeanMethods());
	}

	public @Test void equality() {
		{ // unlike names causes inequality
    		ConfigurationClass c1 = new ConfigurationClass("a");
    		ConfigurationClass c2 = new ConfigurationClass("b");

    		Assert.assertThat(c1, not(equalTo(c2)));
		}

		{ // like names causes equality
    		ConfigurationClass c1 = new ConfigurationClass("a");
    		ConfigurationClass c2 = new ConfigurationClass("a");
    		Assert.assertThat(c1, equalTo(c2));
		}

		{ // order of bean methods is not significant
    		ConfigurationClass c1 = new ConfigurationClass("a")
    			.add(new BeanMethod("m"))
    			.add(new BeanMethod("n"))
			;
    		ConfigurationClass c2 = new ConfigurationClass("a")
    			.add(new BeanMethod("n")) // only difference is order
    			.add(new BeanMethod("m"))
			;
    		Assert.assertThat(c1, equalTo(c2));
		}

		{ // but different bean methods is significant
    		ConfigurationClass c1 = new ConfigurationClass("a")
    			.add(new BeanMethod("a"))
    			.add(new BeanMethod("b"))
			;
    		ConfigurationClass c2 = new ConfigurationClass("a")
    			.add(new BeanMethod("a"))
    			.add(new BeanMethod("z")) // only difference
			;
    		Assert.assertThat(c1, not(equalTo(c2)));
		}

		{ // same object instance causes equality
    		ConfigurationClass c1 = new ConfigurationClass("a");
    		Assert.assertThat(c1, equalTo(c1));
		}

		{ // null comparison causes inequality
    		ConfigurationClass c1 = new ConfigurationClass("a");
    		Assert.assertThat(c1, not(equalTo(null)));
		}

		{ // is declaring class considered when evaluating equality?
			ConfigurationClass c1 = new ConfigurationClass("c").setDeclaringClass(new PotentialConfigurationClass("p"));
			ConfigurationClass c2 = new ConfigurationClass("c");
			Assert.assertThat(c1, not(equalTo(c2)));
			c2.setDeclaringClass(new PotentialConfigurationClass("p"));
			Assert.assertThat(c1, equalTo(c2));
			Assert.assertThat(c2, equalTo(c1));
			c2.getDeclaringClass().add(new BeanMethod("f"));
			Assert.assertThat(c1, not(equalTo(c2)));
		}

		{ // is @Configuration metadata considered when evaluating equality?
			@Configuration(defaultAutowire=Autowire.BY_TYPE) class Prototype { }
			Configuration metadata = extractClassAnnotation(Configuration.class, Prototype.class);

			ConfigurationClass c1 = new ConfigurationClass("c", metadata);
			ConfigurationClass c2 = new ConfigurationClass("c");
			Assert.assertThat(c1, not(equalTo(c2)));
			Assert.assertThat(c2, not(equalTo(c1)));
			c2 = new ConfigurationClass("c", metadata);
			Assert.assertThat(c1, equalTo(c2));
			Assert.assertThat(c2, equalTo(c1));
		}
	}

	public @Test void containsBeanMethod() {
		configClass
			.add(new BeanMethod("x"))
			.add(new BeanMethod("y"))
			.add(new BeanMethod("z"))
		;

		assertTrue(configClass.containsBeanMethod("x"));
		assertTrue(configClass.containsBeanMethod("y"));
		assertTrue(configClass.containsBeanMethod("z"));

		assertFalse(configClass.containsBeanMethod("n"));

		try {
    		assertFalse(configClass.containsBeanMethod(""));
    		fail("should throw when given invalid input");
		} catch (IllegalArgumentException ex) { /* expected */ }
	}

	// valid configurations must declare at least one bean
	public @Test void validateConfigurationMustDeclareAtLeastOneBean() {
		ConfigurationClass configClass = new ConfigurationClass("a");
		assertErrorsContains(configClass, CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN);
	}

	// as an exception to the above, a configuration may be empty of @Bean methods if it imports another configuration
	// of course, any imported configuration will be subject to the same rules.
	public @Test void validateConfigurationMustDeclareAtLeastOneBeanOrImport() {
		ConfigurationClass configClass =
			new ConfigurationClass("c1")
				.addImportedClass(
					new ConfigurationClass("c2")
						.add(new BeanMethod("m")));

		ValidationErrors errors = new ValidationErrors();
		configClass.validate(errors);
		assertTrue("expected no errors but instead got: " + errors, errors.size() == 0);
	}

	public @Test void validateAbstractConfigurationsNotValid() {
		ConfigurationClass configClass =
			new ConfigurationClass("a", Modifier.ABSTRACT)
				.add(new BeanMethod("m"));
		assertErrorsContains(configClass, ABSTRACT_CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_EXTERNALBEAN);
	}

	public @Test void validationCascadesToImportedClasses() {
		configClass
			.add(new BeanMethod("m"))
			// import an non-well-formed configuration class (no bean methods)
			.addImportedClass(new ConfigurationClass("i"));

		assertErrorsContains(configClass, CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN);
	}

	public @Test void validationCascadesToBeanMethods() {
		// create any simple, invalid bean method definition
		configClass.add(new BeanMethod("m", Modifier.PRIVATE));

		assertErrorsContains(configClass, METHOD_MAY_NOT_BE_PRIVATE);
	}

	/** See JavaDoc for {@link ConfigurationClass#getSelfAndAllImports()} */
	public @Test void getSelfAndAllImports() {
		ConfigurationClass A = new ConfigurationClass("A");
		ConfigurationClass B = new ConfigurationClass("B");
		ConfigurationClass Y = new ConfigurationClass("Y");
		ConfigurationClass Z = new ConfigurationClass("Z");
		ConfigurationClass M = new ConfigurationClass("M");

		A.addImportedClass(B);
		Y.addImportedClass(Z);

		M.addImportedClass(A);
		M.addImportedClass(Y);

		ConfigurationClass[] expected = new ConfigurationClass[] { B, A, Z, Y, M };

		assertArrayEquals(expected, M.getSelfAndAllImports().toArray());
	}

	/**
	 * Calls <var>configClass</var>.validate() and asserts that the resulting
	 * set of errors contains <var>error</var>
	 * @param configClass
	 */
	private void assertErrorsContains(ConfigurationClass configClass, ValidationError error) {
		ValidationErrors errors = new ValidationErrors();
		configClass.validate(errors);
		assertTrue("expected errors during validation", errors.size() > 0);
		assertTrue(errors.get(0).contains(error.toString()));
	}

}
