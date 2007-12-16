package org.springframework.config.java.context;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * A filter that matches nested classes (any type defined within another type)
 * 
 * @author Chris Beams
 */
class NestedClassTypeFilter implements TypeFilter {
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        // TODO: interim solution - final implementation is pending a change to Spring Core.  See SPR-4263
        // If the name of the class includes a '$', assume that it is an inner class.  This is not strictly true,
        // but close enough while waiting for the new feature in core.
		return metadataReader.getClassMetadata().getClassName().contains("$");

        // after the change to core, the above line read as follows

		// return metadataReader.getClassMetadata().hasDeclaringClass();
	}
}
