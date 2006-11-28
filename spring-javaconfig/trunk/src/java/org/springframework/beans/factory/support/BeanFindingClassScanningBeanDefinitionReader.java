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

import java.lang.reflect.Modifier;


/**
 * Finds beans and creates definitions.
 * 
 * @author Rod Johnson
 */
public class BeanFindingClassScanningBeanDefinitionReader extends AbstractAsmClassScanningBeanDefinitionReader {
	
	/**
	 * @param bdr
	 */
	public BeanFindingClassScanningBeanDefinitionReader(BeanDefinitionRegistry bdr, TypeFilter classFilter) {
		super(bdr);
	}
	

//	@Override
//	protected boolean isComponentOrFactoryClass(ClassReader classReader) {
//		ClassNameAndTypesReadingVisitor cnr = new ClassNameAndTypesReadingVisitor();
//		classReader.accept(cnr, false);
//		System.out.println(cnr.getName());
//		System.out.println(StringUtils.arrayToCommaDelimitedString(cnr.getInterfaces()));
//		return false;
//	}

	// TODO where does dependency checking go?
	
	// Can we call our own load on dependent classes?
	// Or do we do dep checking late?
	// can call processComponentOrFactoryClass, but then that eagerly instantiates...
	// or do we just rely on finding them all and using reflection?
	// or an annotation!?
	
	@Override
	protected void processComponentOrFactoryClass(Class clazz) {
		// TODO need naming strategy
		// TODO need wiring strategy
		if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
			return;
		}
		if (clazz.isMemberClass()) {
			return;
		}
		if (clazz.getName().indexOf("$") != -1) {
			return;
		}
		throw new UnsupportedOperationException("F" + clazz.getName());
	}

}
