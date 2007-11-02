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

package org.springframework.config.java;

import junit.framework.TestCase;

import org.springframework.config.java.complex.ComplexConfiguration;
import org.springframework.config.java.context.AnnotationApplicationContext;
import org.springframework.config.java.simple.EmptySimpleConfiguration;

/**
 * @author Costin Leau
 * @author Rod Johnson
 */
public class AnnotationApplicationContextTests extends TestCase {

	protected AnnotationApplicationContext ctx;

	protected void setUp() throws Exception {
		super.setUp();
		ctx = new AnnotationApplicationContext();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		ctx = null;
		try {
			// just in case some test fails during loading and refresh is not
			// called
			ctx.refresh();
			ctx.close();
		}
		catch (Exception ex) {
			// ignore - it's cleanup time
		}
		ctx = null;
	}

	public void testReadSimplePackage() throws Exception {
		ctx.setBasePackages("/org/springframework/config/java/simple");
		ctx.refresh();
		
		int classesInPackage = 2;
		int beansInClasses = 2;
		
		assertEquals(
				classesInPackage + beansInClasses,
				ctx.getBeanDefinitionCount());		
	}

	public void testReadInnerClassesInPackage() throws Exception {
		ctx.setBasePackages("/org/springframework/config/java/complex");
		ctx.refresh();
		//for (String s: ctx.getBeanDefinitionNames()) System.out.println(s);
		
		assertEquals(
				6,
				ctx.getBeanDefinitionCount());
	}


	public void testReadClassesByName() throws Exception {
		ctx.setConfigClasses(new Class<?>[] {
				ComplexConfiguration.class,
				EmptySimpleConfiguration.class 
		});
		ctx.refresh();
		
		int classesInPackage = 4;
		int beansInClasses = 3;
		
		//for (String s: ctx.getBeanDefinitionNames()) System.out.println(s);
		
		assertEquals(classesInPackage + beansInClasses, ctx.getBeanDefinitionCount());
	}

}
