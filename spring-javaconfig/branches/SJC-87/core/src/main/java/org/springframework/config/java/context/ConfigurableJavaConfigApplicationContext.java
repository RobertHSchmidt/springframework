package org.springframework.config.java.context;

import org.springframework.context.ConfigurableApplicationContext;

public interface ConfigurableJavaConfigApplicationContext extends ConfigurableApplicationContext, TypeSafeBeanFactory {

	void setConfigClasses(Class<?>... classes);

	/**
	 * Allows for incrementally building up the configuration classes to be
	 * processed by this context. May only be called on a context still 'open
	 * for configuration' meaning that the user will need to manually call
	 * refresh() after all classes have been added.
	 *
	 * @param cls
	 */
	@Deprecated // favor addConfigClasses
	void addConfigClass(Class<?> cls);

	/**
	 * The base packages for configurations from Strings. These use the same
	 * conventions as the component scanning introduced in Spring 2.5.
	 */
	@Deprecated // favor addBasePackages
	void setBasePackages(String... basePackages);

}