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
package org.springframework.config.java.context;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * A filter that matches nested classes (any type defined within another type)
 * 
 * @author Chris Beams
 */
public class NestedClassTypeFilter implements TypeFilter {
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
		// TODO: interim solution - final implementation is pending a change to
		// Spring Core. See SPR-4263
		// If the name of the class includes a '$', assume that it is an inner
		// class. This is not strictly true,
		// but close enough while waiting for the new feature in core.
		return metadataReader.getClassMetadata().getClassName().contains("$");

		// after the change to core, the above line will read as follows:
		// return metadataReader.getClassMetadata().hasDeclaringClass();
	}
}
