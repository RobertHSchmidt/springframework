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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Matches classes with a given annotation.
 * @author Rod Johnson
 */
public class AnnotationTypeFilter implements TypeFilter {
	
	private final Class<? extends Annotation> annotationClass;
	
	/**
	 * @param annotationClass
	 */
	public AnnotationTypeFilter(final Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	/**
	 * ASM annotation visitor used for reading innerClasses without loading the
	 * class. The reader is simplified at considers only the innerClasses
	 * description and not their value.
	 */
	public static class AnnotationReader extends EmptyVisitor {

		private List<String> annotations = createList();

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			annotations.add(Type.getType(desc).getClassName());
			return super.visitAnnotation(desc, visible);
		}

		public AnnotationVisitor visitAnnotation(String name, String desc) {
			annotations.add(Type.getType(desc).getClassName());
			return super.visitAnnotation(name, desc);
		}

		protected List<String> createList() {
			return new ArrayList<String>();
		}
		
		/**
		 * @return the annotations
		 */
		public List<String> getAnnotationNames() {
			return annotations;
		}
	}
	
	public boolean match(ClassReader classReader) {
		AnnotationReader annotationReader = new AnnotationReader();
		// track innerClasses
		classReader.accept(annotationReader, true);
		for (String annotationType : annotationReader.annotations) {
			if (annotationClass.getName().equals(annotationType)) {
				return true;
			}
		}
		return false;
	}

}
