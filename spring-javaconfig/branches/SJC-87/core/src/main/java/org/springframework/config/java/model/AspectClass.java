package org.springframework.config.java.model;

import static java.lang.String.format;

public class AspectClass {

	private final String name;

	public AspectClass(String fqClassName) {
		this.name = fqClassName;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return format("%s: name=%s", this.getClass().getSimpleName(), name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

}
