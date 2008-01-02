package org.springframework.config.java.annotation;

/**
 * Internal interface implemented by all 'three-way' enumerations. Exists solely
 * to represent the relationship between these types; has package-private
 * visibility as it should not be used externally.
 * 
 * @see Lazy
 * @see Primary
 * 
 * @author Chris Beams
 */
interface BooleanEnum {
	int value();

	boolean booleanValue();
}
