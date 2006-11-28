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
package org.springframework.beans.factory.java;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.AssignableTypeFilter;
import org.springframework.beans.factory.support.BeanFindingClassScanningBeanDefinitionReader;
import org.springframework.beans.factory.support.ClassListBuilder;
import org.springframework.beans.factory.support.DependencyAnalyzer;
import org.springframework.beans.factory.support.TypeFilter;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.samples.petclinic.hibernate.HibernateClinic;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.Controller;

/**
 * TODO - 
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class BeanFindingClassScanningBeanDefinitionReaderTests extends TestCase {
	
	protected BeanFindingClassScanningBeanDefinitionReader reader;

	protected GenericApplicationContext registry;

	protected void setUp() throws Exception {
		super.setUp();
		registry = new GenericApplicationContext();
		registry.setResourceLoader(new PathMatchingResourcePatternResolver());
		reader = new BeanFindingClassScanningBeanDefinitionReader(registry, null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		reader = null;
		try {
			// just in case some test fails during loading and refresh is not
			// called
			registry.refresh();
			registry.close();
		}
		catch (Exception e) {
			// ignore - it's cleanup time
		}
		registry = null;
	}

	public void testReadClass() throws Exception {
		//reader.addEntryPoint(TestBean.class);
		//reader.addEntryPoint(HibernateClinic.class);
		
		String resourcePath = "classpath*:/org/springframework/samples/petclinic/**";
		
		//reader.addTypeFilter(new AssignableTypeFilter(Controller.class));
		
		ClassListBuilder allClassFinder = new ClassListBuilder(registry);
		allClassFinder.addTypeFilter(TypeFilter.TRUE);
		allClassFinder.loadBeanDefinitions(resourcePath);
		
		ClassListBuilder entryPointFinder = new ClassListBuilder(registry);
		entryPointFinder.addTypeFilter(new AssignableTypeFilter(Controller.class));
		entryPointFinder.loadBeanDefinitions(resourcePath);
		
		DependencyAnalyzer da = new DependencyAnalyzer(registry, allClassFinder.getClassesFound());
		
		for (Class cls : entryPointFinder.getClassesFound()) {
			System.out.println("Entry point " + cls);
			da.analyze(cls);
		}
		
		for (Class c : da.getDependencies()) {
			System.out.println("Dep=" + c.getName());
		}
		
		for (Class c : da.getUnsatisfiedDependencies()) {
			System.out.println("UNSATISFIED Dep=" + c.getName());
		}
		
//		for (Class cls : allClassFinder.getClassesFound()) {
//			System.out.println("All class " + cls);
//			da.analyze(cls);
//		}
		
		// Now need to find the implementations of the interfaces
		// required
		
		// TODO need to be able to split out, to see all classes implementing Controller for example,
		// in one go
		reader.addTypeFilter(da);
		
		
		//System.out.println("Dependencies ="+ 
			//	StringUtils.collectionToCommaDelimitedString(reader.getDependencyAnalyzer().getDependencies()));
		assertEquals(
				3,
				reader.loadBeanDefinitions(resourcePath));
		// registry.refresh();
	}

}
