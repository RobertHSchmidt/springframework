package org.springframework.beans.factory;

import static java.lang.String.format;

import java.util.Collection;

/**
 * Exception representing an illegal configuration where more than one bean of
 * the same type is found within a given {@link BeanFactory}.
 * 
 * @author Chris Beams
 */
@SuppressWarnings("serial")
public class MultiplePrimaryBeanDefinitionException extends AmbiguousBeanLookupException {

	/**
	 * Create a new instance.
	 * @param type the type that was being searched for
	 * @param beanNames names of all beans marked as primary. Size of this array
	 * must be > 1
	 */
	public MultiplePrimaryBeanDefinitionException(Class<?> type, Collection<String> beanNames) {
		super(format("expected single bean of type [%s] but found %d: %s", type, beanNames.size(), beanNames));
	}

}
