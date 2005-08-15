package org.springframework.webflow.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a field is transient and thus should not be saved or restored.
 * @author Keith Donald
 */
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}