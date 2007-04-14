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
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.config.java.util.ClassUtils;

/**
 * @author Costin Leau
 * @author Rod Johnson
 */
public abstract class AbstractAsmClassScanningBeanDefinitionReader extends AbstractClassScanningBeanDefinitionReader {

	private final List<TypeFilter> typeFilters = new ArrayList<TypeFilter>();

	private static final String DOLLAR = "$";

	/**
	 * @param beanFactory
	 */
	public AbstractAsmClassScanningBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		super(beanFactory);
	}

	public void addTypeFilter(TypeFilter tf) {
		this.typeFilters.add(tf);
	}

	/**
	 * 
	 * @param classReader ASM ClassReader for the class
	 * @return true if this class is a component or component factory class and
	 * thus of interest to us
	 */
	protected boolean isComponentOrFactoryClass(ClassReader classReader) {
		for (TypeFilter tf : typeFilters) {
			if (tf.match(classReader)) {
				return true;
			}
		}
		return false;
	}

	protected class InnerClasses extends EmptyVisitor {
		private List<String> innerClasses = createList();

		private String enclosingClass;

		public InnerClasses(String outerName) {
			// add nested classname separator
			this.enclosingClass = outerName.concat(DOLLAR);
		}

		// TODO: consider access also
		public void visitInnerClass(String name, String outerName, String innerName, int access) {

			if (name.contains(enclosingClass)) {
				innerClasses.add(name);
			}
		}

		protected List<String> createList() {
			return new ArrayList<String>();
		}

		public List<String> getInnerClassNames() {
			return innerClasses;
		}
	}

	/**
	 * Search the given stream for class information. ASM library is used to
	 * parse the stream and extract the classname. Using the stream allows
	 * consistent behavior when reading classes across network, from jars or
	 * just folders without creating a classloader implementation.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected int searchClass(InputStream stream) throws IOException {
		try {
			int count = 0;
			boolean debug = log.isDebugEnabled();

			ClassReader classReader = new ClassReader(stream);

			ClassNameVisitor nameReader = new ClassNameVisitor();
			classReader.accept(nameReader, true);
			if (isComponentOrFactoryClass(classReader)) {
				count++;
				if (debug)
					log.debug("found matching annotation; loading class + " + nameReader.getClassName());

				Class clazz = loadClass(nameReader.getClassName());
				processComponentOrFactoryClass(clazz);
			}
			// check inner classes
			InnerClasses classes = new InnerClasses(ClassUtils.classNameLoadableToInternal(nameReader.getClassName()));
			classReader.accept(classes, true);

			for (String innerClassName : classes.getInnerClassNames()) {
				if (debug)
					log.debug("loading inner class " + innerClassName);

				// transform to realName
				String realName = innerClassName.concat(ClassUtils.CLASS_EXT);
				count += searchClass(getResourceLoader().getClassLoader().getResourceAsStream(realName));
			}

			return count;
		}
		finally {
			try {
				if (stream != null)
					stream.close();
			}
			catch (IOException e) {
				// ignore - it's cleanup
			}
		}
	}

}