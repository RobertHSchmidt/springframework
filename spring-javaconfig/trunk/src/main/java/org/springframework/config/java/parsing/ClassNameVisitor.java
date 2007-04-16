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
package org.springframework.config.java.parsing;

import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.config.java.util.ClassUtils;

/**
 * ASM class visitor which looks only for the classname and implemented types.
 * Inner classes are not handled by the visitor but by the lookup class.
 * 
 * @author Costin Leau
 */
public class ClassNameVisitor extends EmptyVisitor {

	private String name;

	private String superName;

	private String[] interfaces;

	public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
		// if (log.isTraceEnabled())
		// log.trace("found classname " + name);
		this.name = ClassUtils.classNameInternalToLoadable(name);
		this.superName = ClassUtils.classNameInternalToLoadable(supername);
		this.interfaces = interfaces;
		for (int i = 0; i < interfaces.length; i++) {
			interfaces[i] = ClassUtils.classNameInternalToLoadable(interfaces[i]);
		}
	}

	/**
	 * @return the name
	 */
	public String getClassName() {
		return name;
	}

	/**
	 * @return the superName
	 */
	public String getSuperName() {
		return superName;
	}

	/**
	 * @return the interfaces
	 */
	public String[] getInterfaceNames() {
		return interfaces;
	}

	public Class loadClass() {
		try {
			Class theClass = Class.forName(getClassName(), false, getClass().getClassLoader());
			return theClass;
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Cannot load class with name '" + getClassName() + "'");
		}
	}
}
