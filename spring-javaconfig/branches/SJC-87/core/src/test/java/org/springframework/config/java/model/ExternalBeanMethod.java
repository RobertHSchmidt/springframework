package org.springframework.config.java.model;

import static java.lang.String.format;

import java.lang.reflect.Modifier;

import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.util.Assert;

// TODO: refactor with BeanMethod to arrive at a common abstraction.  Consider parameterizing
public class ExternalBeanMethod {

	private static final ExternalBean defaultAnno;
	private final String name;
	private final ExternalBean metadata;
	private final int modifiers;

	// hack required to get an instance of @ExternalBean for defaulting purposes
	static {
		try {
    		class c { @ExternalBean void m() { } }
    		defaultAnno = c.class.getDeclaredMethod("m").getAnnotation(ExternalBean.class);
		} catch (NoSuchMethodException ex) { throw new RuntimeException(ex); }
	}

	/** for testing convenience */
	ExternalBeanMethod(String name) { this(name, defaultAnno); }

	/** for testing convenience */
	ExternalBeanMethod(String name, int modifiers) { this(name, defaultAnno, modifiers); }

	public ExternalBeanMethod(String name, ExternalBean beanAnno) { this(name, beanAnno, 0); }

	public ExternalBeanMethod(String name, ExternalBean metadata, int modifiers) {
		Assert.hasText(name);
		this.name = name;

		Assert.notNull(metadata);
		this.metadata = metadata;

		Assert.isTrue(modifiers >= 0, "modifiers must be non-negative: " + modifiers);
		this.modifiers = modifiers;
	}

	public int getModifiers() {
		return modifiers;
	}

	public ValidationErrors validate(ValidationErrors errors) {
		if(Modifier.isPrivate(modifiers))
			// TODO: needs to have reference to parent class for better diagnostics
			// TODO: needs to distinguish that this is an @ExternalBean vs @Bean
			errors.add(ValidationError.METHOD_MAY_NOT_BE_PRIVATE + ": " + name);
		return errors;
	}

	@Override
	public String toString() {
		return format("%s: name=%s; modifiers=%d",
				       getClass().getSimpleName(), name, modifiers);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + modifiers;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalBeanMethod other = (ExternalBeanMethod) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
			return false;
		if (modifiers != other.modifiers)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

}
