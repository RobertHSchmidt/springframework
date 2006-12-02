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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Convenient superclass for ASM MethodVisitor implementations
 * @author Rod Johnson
 *
 */
public class MethodVisitorSupport implements MethodVisitor {

	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	// TODO returning null here causes ASM to die with an NPE. Looks like an ASM bug
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return new AnnotationVisitorSupport();
	}

	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		return null;
	}

	public void visitAttribute(Attribute arg0) {
	}

	public void visitCode() {
		
	}

	public void visitInsn(int arg0) {
		
	}

	public void visitIntInsn(int arg0, int arg1) {
		
	}

	public void visitVarInsn(int arg0, int arg1) {
		
	}

	public void visitTypeInsn(int arg0, String arg1) {
		
	}

	public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
		
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {		
	}

	public void visitJumpInsn(int arg0, Label arg1) {
		
	}

	public void visitLabel(Label arg0) {
		
	}

	public void visitLdcInsn(Object arg0) {
		
	}

	public void visitIincInsn(int arg0, int arg1) {
		
	}

	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		
	}

	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		
	}

	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		
	}

	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
		
	}

	public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3, Label arg4, int arg5) {
		
	}

	public void visitLineNumber(int arg0, Label arg1) {
		
	}

	public void visitMaxs(int arg0, int arg1) {
	
	}

	public void visitEnd() {	
	}
	
}

