package org.springframework.config.java.internal.model;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class NonJavaConfigMethod {

	private final String name;
	private final Annotation[] annotations;

	NonJavaConfigMethod(String name) {
		this(name, 0);
	}

	public NonJavaConfigMethod(String name, int modifiers, Annotation... annotations) {
		this.name = name;
		this.annotations = annotations;
	}

	public void validate(ValidationErrors errors) {
		for(Annotation anno : annotations)
			errors.add(ValidationError.INVALID_ANNOTATION_DECLARATION + ": " + name + " " + anno.annotationType().getSimpleName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(annotations);
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
		NonJavaConfigMethod other = (NonJavaConfigMethod) obj;
		if (!Arrays.equals(annotations, other.annotations))
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
