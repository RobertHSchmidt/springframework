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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.FatalBeanException;
import org.springframework.util.StringUtils;

/**
 * @author Rod Johnson
 */
public class DependencyAnalyzer extends AbstractClassTestingTypeFilter {
	
	private final Set<Class> dependencies = new HashSet<Class>();
	
	private final Set<Class> unsatisfiedDependencies = new HashSet<Class>();
	
	private final Set<String> excludeNamespaces = new HashSet<String>();
	
	private final BeanDefinitionRegistry beanDefinitionRegistry;

	private Set<Class> allClasses;
	
	public DependencyAnalyzer(BeanDefinitionRegistry beanDefinitionRegistry,
			Set<Class> allClasses,
			Class ...entryPoints) {
		this.beanDefinitionRegistry = beanDefinitionRegistry;
		this.allClasses = allClasses;
		for (Class what : entryPoints) {
			analyze(what);
		}
		
		excludeNamespaces.add("oracle");
		excludeNamespaces.add("java");
	}
	
	@Override
	protected boolean match(ClassNameAndTypesReadingVisitor v) {
		// We have actual classes
		Class theClass = v.loadClass();
		for (Class dependency : dependencies) {
			if (dependency.isAssignableFrom(theClass)) {
				return true;
			}
		}
		return false;
	}
	
	
	public void analyze(Class what) {
		//System.out.println("Analyzing " + what);
		
		PropertyDescriptor[] pds = getPropertyDescriptors(what);
		
		// TODO use Cachedintrospections results?
		for (PropertyDescriptor pd : pds) {			
			process(pd);
		}
	}

	/**
	 * @return
	 */
	private PropertyDescriptor[] getPropertyDescriptors(Class what) {
//		 TODO use Cachedintrospections results?
		try {
			return Introspector.getBeanInfo(what).getPropertyDescriptors();
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("", ex);
		}
	}

	/**
	 * @param pd
	 */
	private void process(PropertyDescriptor pd) {
		//System.out.println(pd.getName());
		// TODO if it contains such a bean.
		// Compose with an environmental scanner for session factory
		// etc.
		
		// TODO how to resolve ambiguity? THROW helpful exception,
		// generating both BDs?
		
		// Really need a @Scope annotation, at least it's rarer
		// OR in external metadata: scope?
		
		// Use aspectj type expression?
		
		if (
				pd.getPropertyType() == null ||
				dependencies.contains(pd.getPropertyType()) || 
				containsBeanOfType(pd.getPropertyType(), this.beanDefinitionRegistry) ||
				pd.getPropertyType().isPrimitive() ||
				isExcluded(pd.getPropertyType())
			) {
			return;
		}
		else if (pd.getPropertyType().isInterface()) {
			// TODO or is abstract
			dependencies.add(pd.getPropertyType());
			Class uniqueImpl = findUniqueImplementation(pd.getPropertyType());
			if (uniqueImpl != null) {
				analyze(uniqueImpl);
			}
			else {
				unsatisfiedDependencies.add(pd.getPropertyType());
			}
		}
		else {
			dependencies.add(pd.getPropertyType());
			analyze(pd.getPropertyType());
		}
	}
	
	// TODO need to combine with test/other config to help in resolution
	
	private Class findUniqueImplementation(Class intf) {
		Set<Class> implClasses = new HashSet<Class>();
		for (Class c : allClasses) {
			if (!c.isInterface() && intf.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
				implClasses.add(c);
			}
		}
		if (implClasses.size() == 1) {
			return implClasses.iterator().next();
		}
		System.out.println("Not unique: " + intf.getName() + " resolves to: " + 
				StringUtils.collectionToCommaDelimitedString(implClasses));
		return null;
	}
	
	/**
	 * @param propertyType
	 * @return
	 */
	protected boolean isExcluded(Class<?> propertyType) {
		//return propertyType.getName().startsWith("org.springframework");
		return isInExcludedNamespace(propertyType) ||
			(propertyType.getName().startsWith("org.springframework") &&
					!propertyType.getName().startsWith("org.springframework.samp")) ||
					
			propertyType.isArray();
	}

	/**
	 * @param propertyType
	 * @return
	 */
	private boolean isInExcludedNamespace(Class<?> propertyType) {
		for (String ns : excludeNamespaces) {
			if (propertyType.getName().startsWith(ns)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsBeanOfType(Class typeRequired, BeanDefinitionRegistry bdr) {
		// TODO needs to check interfaces also
		//return bdr.;
		return false;
	}
	
	// TODO needs explicit configuration: how to know if there
	// is a default? Log warning?

	/**
	 * @return the dependencies
	 */
	public Set<Class> getDependencies() {
		return dependencies;
	}
	
	// TODO probably return component name and property
	public Set<Class> getUnsatisfiedDependencies() {
		return unsatisfiedDependencies;
	}
}
