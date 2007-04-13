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

package org.springframework.config.java.parsing.asm;

import org.objectweb.asm.AnnotationVisitor;

/**
 * Convenient no op superclass for AnnotationVisitor
 * @author Rod Johnson
 *
 */
public class AnnotationVisitorSupport implements AnnotationVisitor {
	
	public void visit(String name, Object value) {					
	}

	public void visitEnum(String name, String desc, String value) {					
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return null;
	}

	public AnnotationVisitor visitArray(String name) {
		return null;
	}

	public void visitEnd() {					
	}
}