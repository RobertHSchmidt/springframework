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

package org.springframework.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * ASM ClassVistor interested only in methods.
 * Also useful as a base class. 
 * @author Rod Johnson
 *
 */
public class ClassVisitorSupport implements ClassVisitor {

	public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
		
	}

	public void visitSource(String arg0, String arg1) {
		
	}

	public void visitOuterClass(String arg0, String arg1, String arg2) {
		
	}

	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// ASM tends to throw NPEs if this returns null
		return new AnnotationVisitorSupport();
	}

	public void visitAttribute(Attribute arg0) {
		
	}

	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		
	}

	public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {
		return null;
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return null;
	}

	public void visitEnd() {		
	}
	
}