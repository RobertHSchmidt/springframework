package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

	public @Test void validateMustDeclareAtLeastOneBean() {
		ConfigurationClass configClass = new ConfigurationClass("a");
		ValidationErrors errors = new ValidationErrors();
		configClass.validate(errors);
		assertTrue(errors.size() > 0);
		assertTrue(errors.get(0).contains(ValidationError.CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN.toString()));
	}

	public @Test void validateAbstractConfigurationsNotValid() {
		ConfigurationClass configClass =
			new ConfigurationClass("a", Modifier.ABSTRACT)
				.add(new BeanMethod("m"));
		ValidationErrors errors = new ValidationErrors();
		configClass.validate(errors);
		assertTrue("expected errors during validation", errors.size() > 0);
		assertTrue(errors.get(0).contains(ValidationError.ABSTRACT_CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_EXTERNALBEAN.toString()));
	}

}
