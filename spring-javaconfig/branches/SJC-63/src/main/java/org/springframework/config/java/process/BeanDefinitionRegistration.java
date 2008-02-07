package org.springframework.config.java.process;

import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Class to hold BeanDefinition, name and any other information, to allow
 * configuration listeners to customize the registration, change its name, etc.
 */
class BeanDefinitionRegistration {
	public RootBeanDefinition rbd;

	public String name;

	/**
	 * Should the bean definition be hidden or not. When hidden, the bean
	 * definition resides only in the child context.
	 */
	public boolean hide;

	public BeanDefinitionRegistration(RootBeanDefinition rbd, String name) {
		this.rbd = rbd;
		this.name = name;
	}
}