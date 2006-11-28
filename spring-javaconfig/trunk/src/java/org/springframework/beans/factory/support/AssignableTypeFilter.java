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

import java.util.HashSet;
import java.util.Set;


/**
 * @author Rod Johnson
 */
public class AssignableTypeFilter extends AbstractClassTestingTypeFilter {
	
	public final Set<Class<?>> types = new HashSet<Class<?>>();
	
	/**
	 * @param intf
	 */
	public AssignableTypeFilter(Class<?> ... types) {
		for (Class<?> type : types) {
			addType(type);
		}
	}
	
	public void addType(Class<?> type) {
		this.types.add(type);
	}
	
	// TODO probably insufficient
	@Override
	protected boolean match(ClassNameAndTypesReadingVisitor v) {
//		for (String intfName : v.getInterfaceNames()) {
//			System.out.println("want " + intf.getName() + "; have " + intfName);
//			if (intfName.equals(intf.getName())) {
//				return true;
//			}
//		}
//		return false;
		
		// TODO go through standard path
		
		Class theClass = v.loadClass();
		for (Class<?> type : types) {
			if (type.isAssignableFrom(theClass)) {
				return true;
			}
		}
		return false;
	}


}
