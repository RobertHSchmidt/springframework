/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.beans.factory.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * @author Rod Johnson
 * @author Costin Leau
 */
public abstract class AbstractClassTestingTypeFilter implements TypeFilter {
	
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * ASM class visitor which looks only for the classname and implemented types. 
	 * Inner classes are not handled by the visitor but by the lookup class.
	 */
	public static class ClassNameAndTypesReadingVisitor extends EmptyVisitor {
		
		private String name;
		private String superName;
		private String[] interfaces;

		public void visit(int version, int access, String name, String signature, 
				String supername, String[] interfaces) {
//			if (log.isTraceEnabled())
//				log.trace("found classname " + name);
			this.name = AbstractClassScanningBeanDefinitionReader.convertInternalClassNameToLoadableClassName(name);
			this.superName = AbstractClassScanningBeanDefinitionReader.convertInternalClassNameToLoadableClassName(supername);
			this.interfaces = interfaces;
			for (int i = 0; i < interfaces.length; i++) {
				interfaces[i] = AbstractClassScanningBeanDefinitionReader.convertInternalClassNameToLoadableClassName(interfaces[i]);
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
		
		// TODO go through regular path
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
	
	public final boolean match(ClassReader cr) {
		ClassNameAndTypesReadingVisitor v = new ClassNameAndTypesReadingVisitor();
		cr.accept(v, false);
		return match(v);
	}

	/**
	 * @param v
	 * @return
	 */
	protected abstract boolean match(ClassNameAndTypesReadingVisitor v);
	
}
