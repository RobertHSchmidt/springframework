package org.springframework.config.java.model;

import static java.lang.String.format;

import org.aspectj.lang.annotation.Aspect;

public class AspectClass {

	public static final String BEAN_ATTR_NAME = "isJavaConfigurationAspectClass";
	private final String name;
	private Aspect metadata;

	/**
	 * Create an instance without an explicit {@link Aspect @Aspect} annotation.  Allows
	 * the user to create one later in process see {@link #setMetadata(Aspect)}.  If no
	 * Aspect metadata is supplied, {@link validation #validate(ValidationErrors)} will fail.
	 *
	 * @param fqClassName fully-qualified name of Aspect class
	 */
	public AspectClass(String fqClassName) {
		this(fqClassName, null);
	}

	public AspectClass(String fqClassName, Aspect metadata) {
		this.name = fqClassName;
		this.metadata = metadata;
	}

	public void setMetadata(Aspect metadata) {
		this.metadata = metadata;
	}

	public String getName() {
		return name;
	}

	public ValidationErrors validate(ValidationErrors errors) {
		if(metadata == null)
			errors.add(ValidationError.ASPECT_CLASS_MUST_HAVE_ASPECT_ANNOTATION.toString());
		return errors;
	}

	@Override
	public String toString() {
		return format("%s: name=%s", this.getClass().getSimpleName(), name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
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
		AspectClass other = (AspectClass) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
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
