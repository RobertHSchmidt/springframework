/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.config.java.model;

import static java.lang.String.format;

import org.springframework.config.java.annotation.Bean;

public class BeanMethod {
	private static final Bean defaultBeanAnnotation;
	private final String name;
	private final Bean beanAnnotation;
	private final int modifiers;

	// hack required to get an instance of @Bean for defaulting purposes
	static {
		try {
    		class c { @Bean void m() { } }
    		defaultBeanAnnotation = c.class.getDeclaredMethod("m").getAnnotation(Bean.class);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** for testing convenience */
	BeanMethod(String name) { this(name, defaultBeanAnnotation); }

	/** for testing convenience */
	BeanMethod(String name, int modifiers) { this(name, defaultBeanAnnotation, modifiers); }

	public BeanMethod(String name, Bean beanAnno) { this(name, beanAnno, 0); }

	public BeanMethod(String methodName, Bean beanAnno, int modifiers) {
		this.name = methodName;
		this.beanAnnotation = beanAnno;
		this.modifiers = modifiers;
	}

	public Bean getBeanAnnotation() {
		return beanAnnotation;
	}

	public String getName() {
		return name;
	}

	/** @see java.lang.reflect.Modifier */
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public String toString() {
		return format("{%s:methodName=%s,beanAnnotation=%s,modifiers=%d}",
				       getClass().getSimpleName(), name, beanAnnotation, modifiers);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanAnnotation == null) ? 0 : beanAnnotation.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + modifiers;
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
		BeanMethod other = (BeanMethod) obj;
		if (beanAnnotation == null) {
			if (other.beanAnnotation != null)
				return false;
		}
		else if (!beanAnnotation.equals(other.beanAnnotation))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (modifiers != other.modifiers)
			return false;
		return true;
	}

}