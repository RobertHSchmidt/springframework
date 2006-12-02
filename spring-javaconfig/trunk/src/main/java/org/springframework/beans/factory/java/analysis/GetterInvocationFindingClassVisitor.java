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

package org.springframework.beans.factory.java.analysis;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.asm.ClassVisitorSupport;
import org.springframework.asm.MethodVisitorSupport;
import org.springframework.util.StringUtils;

/**
 * ASM ClassVistor implementation that saves getter methods invoked
 * @author Rod Johnson
 *
 */
public class GetterInvocationFindingClassVisitor extends ClassVisitorSupport {
	
	/**
	 * Key = bean creation method name
	 * value = list of getters invoked on the target object. 
	 * Value may be an empty list, but there will be a value for every getter invoked
	 */
	private Map<String, List<String>> getterInvocations = new HashMap<String, List<String>>();

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		//System.out.println(arg0);
		System.out.println("visiting method " + name);
		//System.out.println(signature);
		//System.out.println(returnType);
		
		MethodVisitor mv = new GetterFindingMethodVisitor(name);
		
		// For debugging
//		ProxyFactory pf = new org.springframework.aop.framework.ProxyFactory(mv);
//		pf.addAdvice(new MethodBeforeAdvice() {
//			public void before(Method method, Object[] args, Object target) throws Throwable {
//				System.out.println("Call to "  + method + " with args=" + StringUtils.arrayToCommaDelimitedString(args));
//			}
//		});
//		mv = (MethodVisitor) pf.getProxy();
		
		return mv;
	}
	
	
	private class GetterFindingMethodVisitor extends MethodVisitorSupport {
		
		private final String methodName;
		
		private List<String> gettersInvoked = new LinkedList<String>();
		
		public GetterFindingMethodVisitor(String methodName) {
			this.methodName = methodName;
		}
		
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {			
			if (name.startsWith("get")) {
				System.out.println("***** gotcha: ");
				System.out.println("opcode=" + opcode + "; owner=" + owner + "; name=" + name + "; desc=" + desc);
				gettersInvoked.add(name);
			}
		}
		
		@Override
		public void visitInsn(int opCode) {
			System.out.println("opCode=" + opCode + ", return=" + Opcodes.RETURN);
		}
		
		@Override
		public void visitEnd() {
			// Add to superclass map
			getterInvocations.put(methodName, gettersInvoked);
		}
		
		
	}


	public Map<String, List<String>> getGetterInvocations() {
		return getterInvocations;
	}

}