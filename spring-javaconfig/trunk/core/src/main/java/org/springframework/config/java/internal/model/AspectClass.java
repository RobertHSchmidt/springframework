package org.springframework.config.java.internal.model;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.config.java.model.ModelClass;

public class AspectClass extends ModelClass {

	public static final String BEAN_ATTR_NAME = "isJavaConfigurationAspectClass";
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
		super(fqClassName);
		this.metadata = metadata;
	}

	public void setMetadata(Aspect metadata) {
		this.metadata = metadata;
	}

	public ValidationErrors validate(ValidationErrors errors) {
		if(metadata == null)
			errors.add(ValidationError.ASPECT_CLASS_MUST_HAVE_ASPECT_ANNOTATION.toString());
		return errors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
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
		return true;
	}

}
