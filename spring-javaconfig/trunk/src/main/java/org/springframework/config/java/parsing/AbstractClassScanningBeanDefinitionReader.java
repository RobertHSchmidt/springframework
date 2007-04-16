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
package org.springframework.config.java.parsing;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Annotation based bean definition reader that will use the given
 * location/pattern to locate classes and analyze them. It will consider classes
 * given through:
 * <ul>
 * <li>name :(foo.bar.MyBean)</li>
 * <li>pattern :(/org/package/*.class)
 * </ul>
 * 
 * The underlying classLoader is used to load the classes from URL, jars or
 * folders.
 * 
 * 
 * @author Costin Leau
 * @author Rod Johnson
 */
// TODO: consider visibility restrictions (private and default could be excluded)
// TODO: annotation inheritance is considered according to jsr250
// classlevel annotation apply only on the declaring class).
public abstract class AbstractClassScanningBeanDefinitionReader extends AbstractBeanDefinitionReader {

	protected final Log log = LogFactory.getLog(getClass());

	public AbstractClassScanningBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		super(beanFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.config.java.testing.config.java.parsing.BeanDefinitionReader#loadBeanDefinitions(org.springframework.config.java.testing.config.java.core.io.Resource)
	 */
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {

		String name = resource.getFilename();

		if (log.isDebugEnabled())
			log.debug("loading definition from " + resource);

		try {
			// check if it's a class file
			if (name.endsWith(ClassUtils.CLASS_EXT)) {
				if (log.isDebugEnabled())
					log.debug(name + " ends w/ class extension - considered as class resources");
				return searchClass(resource);
			}
			else {
				// verify if it's a FQN
				if (isFQN(name)) {
					if (log.isDebugEnabled())
						log.debug(name + " considered as class FQN");

					Resource classResource = getResourceLoader().getResource(
						ClassUtils.classNameLoadableToInternal(name).concat(ClassUtils.CLASS_EXT));
					// if we find the resource, move on
					if (classResource.exists())
						return searchClass(classResource);
				}

			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("could not load " + resource.getDescription());
		}

		return 0;
	}

	/**
	 * Can't really judge if we have a class or not based on a name. It can be
	 * outside a package and have only one letter which is lowercase.
	 * 
	 * @param className
	 * @return
	 */
	protected boolean isFQN(String className) {
		boolean result = (StringUtils.hasText(className) && Character.isJavaIdentifierStart(className.codePointAt(0)));
		return result;
	}

	protected int searchClass(Resource resource) throws IOException {
		return searchClass(resource.getInputStream());
	}

	/**
	 * Search the class for any innerClasses.
	 * 
	 * @param resource
	 * @return
	 */
	protected abstract int searchClass(InputStream stream) throws IOException;

	/**
	 * Utility method which loads a class without initializing it and translates
	 * the CNF exception into BeanDefinitionStoreException.
	 * 
	 * @param className
	 * @return
	 */
	protected Class loadClass(String className) {
		try {
			// transform name to binary form
			return Class.forName(ClassUtils.classNameInternalToLoadable(className), false,
				getResourceLoader().getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new BeanDefinitionStoreException(className + " could not be loaded", ex);
		}
	}

	/**
	 * A component class or component factory class has been found. Process it,
	 * presumably by creating a bean definition
	 * @param clazz component class or component factory class
	 */
	protected abstract void processComponentOrFactoryClass(Class clazz);

}
