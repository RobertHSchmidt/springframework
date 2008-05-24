package org.springframework.config.java.internal.factory;

import java.lang.reflect.Modifier;

public enum BeanVisibility {
	PUBLIC, HIDDEN;

	public static BeanVisibility visibilityOf(int modifiers) {
		if(Modifier.isPublic(modifiers))
			return PUBLIC;

		return HIDDEN;
	}
}
