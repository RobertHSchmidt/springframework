package org.springframework.beans.factory;

/**
 * Makes obsolete {@link BeanFactory#getBean(String, Class)}
 * 
 * <p/> XXX: Review and Document
 * 
 * @author Chris Beams
 */
public interface TypeSafeBeanFactory {

	/**
	 * Return an instance of the given <var>type</var>. If multiple instances
	 * of the same type exist, instances are inspected to see if exactly one is
	 * marked as 'primary'.
	 * 
	 * @param type desired instance type
	 * @throws NoSuchBeanDefinitionException if no instance matches <var>type</var>
	 * @throws AmbiguousBeanLookupException if more than one instance is found
	 * and no single instance is marked as primary
	 * @throws MultiplePrimaryBeanDefinitionException if more than one instance
	 * is found and more than one instance is marked as primary
	 * @return instance matching <var>type</var>
	 */
	public <T> T getBean(Class<T> type);

	/**
	 * Return an instance named <var>beanName</var> and of type <var>type</var>.
	 * 
	 * This method is similar to its predecessor
	 * {@link BeanFactory#getBean(String, Class)}, but this variant takes
	 * advantages of generics and removes the casting burden from the caller.
	 * 
	 * @throws NoSuchBeanDefinitionException if <var>beanName</var> cannot be
	 * found
	 * @throws BeanNotOfRequiredTypeException if <var>beanName</var> is found
	 * but is not of type <var>type</var>
	 */
	public <T> T getBean(Class<T> type, String beanName);
}
