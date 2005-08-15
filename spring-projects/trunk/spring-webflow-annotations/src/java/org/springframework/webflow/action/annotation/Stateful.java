package org.springframework.webflow.action.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a bean has state that should be managed by Web Flow. Beans
 * marked stateful will automatically have all non
 * @Transient fields saved out to flow scope after each invocation. In addition,
 * persistent fields will be restored from flow scope before any subsequent
 * invocations.
 * 
 * @author Keith Donald
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Stateful {

	/**
	 * Returns the name of the stateful bean, typically used to index the
	 * memento responisble for holding the bean's state in flow scope.
	 * @return the name of the bean
	 */
	String name();
}
