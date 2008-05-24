package org.springframework.config.java.context;

import java.lang.reflect.Modifier;

public enum BeanVisibility {
	PUBLIC, HIDDEN;

	public static BeanVisibility visibilityOf(int modifiers) {
		if(Modifier.isPublic(modifiers))
			return PUBLIC;

		return HIDDEN;
	}
}
