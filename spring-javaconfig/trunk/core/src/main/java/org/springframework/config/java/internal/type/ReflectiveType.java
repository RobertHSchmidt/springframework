package org.springframework.config.java.internal.type;

public class ReflectiveType implements Type {

	private final Class<?> delegate;

	public ReflectiveType(Class<?> delegate) {
		this.delegate = delegate;
	}

	public String getName() {
		return delegate.getName();
	}

	public String getSimpleName() {
		return delegate.getSimpleName();
	}

	public boolean isInterface() {
		return delegate.isInterface();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		ReflectiveType other = (ReflectiveType) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		}
		else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

}
