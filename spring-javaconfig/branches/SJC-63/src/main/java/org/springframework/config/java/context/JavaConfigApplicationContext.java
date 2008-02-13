/*
 * Copyright 2002-2008 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.process.ConfigurationPostProcessor;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.process.ConfigurationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

/**
 * <p/>Application context that looks for classes annotated with the
 * {@link org.springframework.config.java.annotation.Configuration} annotation
 * and registers the {@link org.springframework.config.java.annotation.Bean}s
 * they define; is the primary programmatic resource for using Spring
 * JavaConfig. Note that it is not strictly required that a configuration class
 * be annotated with &#064Configuration, but rather that it exposes at least one
 * non-private method annotated with &#064Bean.
 * 
 * <h3>Example</h3>
 * 
 * <pre class="code">
 * JavaConfigApplicationContext context = new JavaConfigApplicationContext(AppConfig.class, DataConfig.class);
 * 
 * AccountService accountService = (AccountService) context.getBean(&quot;accountService&quot;);
 * </pre>
 * 
 * Where <code>AppConfig</code> and <code>DataConfig</code> are defined as
 * follows:
 * 
 * <pre class="code">
 * &#064;Configuration
 * public abstract class AppConfig {
 *     &#064;Bean
 *     public AccountService accountService() {
 *         return new AccountService(dataSource());
 *     }
 *     &#064;ExternalBean
 *     public abstract DataSource dataSource();
 * }
 * &#064;Configuration
 * public abstract class DataConfig {
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         return new DataSource(...);
 *     }
 * }
 * </pre>
 * 
 * <h2>Construction</h2>
 * 
 * <p/> The eight available constructors for
 * <code>JavaConfigApplicationContext</code> are divided into two categories:
 * <ol>
 * <li>Those that leave the context open for further configuration, and require
 * the user to call {@link #refresh() refresh}
 * <ul>
 * <li>{@link #JavaConfigApplicationContext()}</li>
 * <li>{@link #JavaConfigApplicationContext(ApplicationContext)}</li>
 * </ul>
 * </li>
 * <li>Those that leave the context closed for further configuration and do not
 * require {@link #refresh() refresh}
 * <ul>
 * <li>{@link #JavaConfigApplicationContext(Class...)}</li>
 * <li>{@link #JavaConfigApplicationContext(String...)}</li>
 * <li>{@link #JavaConfigApplicationContext(Class[], String[])}</li>
 * <li>{@link #JavaConfigApplicationContext(ApplicationContext, Class...)}</li>
 * <li>{@link #JavaConfigApplicationContext(ApplicationContext, String...)}</li>
 * <li>{@link #JavaConfigApplicationContext(ApplicationContext, Class[], String[])}</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * In the former set, the following code would be valid:
 * 
 * <pre class="code">
 * JavaConfigApplicationContext ctx = new JavaConfigApplicationContext();
 * ctx.setConfigClasses(Config1.class, Config2.class);
 * ctx.setBaseClasses(&quot;com.foo.myapp.config.*&quot;);
 * ctx.setParent(anotherContext);
 * ctx.refresh();
 * Service myService = (Service) ctx.getBean(&quot;service&quot;);
 * </pre>
 * 
 * Note that the caller must manually invoke {@link #refresh() refresh} to
 * advise the context that configuration is complete. In most cases, users will
 * not want to be burdened with having to remember to do this, so the latter six
 * constructors are provided as conveniences. They each internally call
 * {@link #refresh() refresh}, which means that any subsequent calls to
 * {@link #setConfigClasses(Class...) setConfigClasses},
 * {@link #setBasePackages(String...) setBasePackages} or
 * {@link #setParent(ApplicationContext) setParent} are invalid and will result
 * in an exception. Simply said, after instantiation with one of the convenience
 * constructors, the context is 'closed for configuration':
 * 
 * <pre class="code">
 * JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(Config1.class, Config2.class);
 * 
 * Service myService = (Service) ctx.getBean(&quot;service&quot;);
 * </pre>
 * 
 * 
 * <h2>Type-safe access to beans</h2>
 * 
 * To ensure refactorability and avoid string-based bean lookups, it is
 * recommended that users take advantage of
 * <code>JavaConfigApplicationContext</code>'s type-safe <code>getBean</code>
 * methods:
 * <ul>
 * <li>{@link #getBean(Class)}</li>
 * <li>{@link #getBean(Class, String)}</li>
 * </ul>
 * 
 * The examples above become more elegant using these methods:
 * 
 * <pre class="code">
 * JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(Config1.class, Config2.class);
 * 
 * Service myService = ctx.getBean(Service.class); // no casting required!
 * </pre>
 * 
 * Of course, if multiple beans of type <code>Service</code> exist in the
 * context, the call above becomes ambiguous. Disambiguation can happen in one
 * of two ways:
 * <ol>
 * <li>Declare one bean as
 * {@link org.springframework.config.java.annotation.Primary}
 * 
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *     &#064;Bean(primary=Primary.TRUE)
 *     public Service service() {
 *         return new Service(...);
 *     }
 *     &#064;Bean
 *     public Service testService() {
 *         return new Service(...);
 *     }
 * }
 * </pre>
 * 
 * </li>
 * 
 * <li>Use the name-qualified {@link #getBean(Class, String)} variant
 * 
 * <pre class="code">
 * JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(Config1.class, Config2.class);
 * 
 * Service testService = ctx.getBean(Service.class, &quot;testService&quot;);
 * </pre>
 * 
 * </li>
 * </ol>
 * 
 * 
 * @see org.springframework.config.java.annotation.Configuration
 * @see org.springframework.config.java.annotation.Bean
 * 
 * @author Chris Beams
 */
public class JavaConfigApplicationContext extends AbstractRefreshableApplicationContext implements TypeSafeBeanFactory {

	private Class<?>[] configClasses;

	private String[] basePackages;

	private boolean closedForConfiguration = false;

	private final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningConfigurationProviderFactory()
			.getProvider(this);

	private final ConfigurationPostProcessor configurationPostProcessor = new ConfigurationPostProcessor();

	/**
	 * requires calling refresh()
	 * 
	 * TODO: finish doc
	 */
	public JavaConfigApplicationContext() {
		this((ApplicationContext) null);
	}

	/**
	 * requires calling refresh()
	 * 
	 * TODO: finish doc
	 * 
	 * @param parent
	 */
	public JavaConfigApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	public JavaConfigApplicationContext(String... basePackages) {
		this(null, null, basePackages);
	}

	public JavaConfigApplicationContext(Class<?>... classes) {
		this(null, classes, null);
	}

	public JavaConfigApplicationContext(ApplicationContext parent, Class<?>... classes) {
		this(parent, classes, null);
	}

	public JavaConfigApplicationContext(ApplicationContext parent, String... basePackages) {
		this(parent, null, basePackages);
	}

	public JavaConfigApplicationContext(Class<?>[] classes, String[] basePackages) {
		this(null, classes, basePackages);
	}

	/**
	 * TODO: Document
	 * 
	 * @see #prepareRefresh()
	 * @see #finishRefresh()
	 * 
	 * @param parent
	 * @param classes
	 * @param basePackages
	 */
	public JavaConfigApplicationContext(ApplicationContext parent, Class<?>[] classes, String[] basePackages) {
		super(parent);

		// TODO: what happens if a class is supplied in classes, but that same
		// class is also detected while processing baseClasses? I would assume
		// that if it is encountered explicitly in classes, that that position
		// should be preserved.

		setConfigClasses(classes);
		setBasePackages(basePackages);

		refresh();
	}

	@Override
	protected void prepareRefresh() {
		super.prepareRefresh();
		processAnyOuterClasses();
		this.addBeanFactoryPostProcessor(configurationPostProcessor);
	}

	public void setConfigClasses(Class<?>... classes) {
		if (closedForConfiguration)
			throw new IllegalStateException("setConfigClasses() may only be called before refresh()");

		// TODO: document why the reversal is necessary
		this.configClasses = reverse(classes);
	}

	// TODO: implement addBasePackage(String pkg)

	/**
	 * Allows for incrementally building up the configuration classes to be
	 * processed by this context. May only be called on a context still 'open
	 * for configuration' meaning that the user will need to manually call
	 * refresh() after all classes have been added.
	 * 
	 * @param cls
	 */
	public void addConfigClass(Class<?> cls) {
		if (closedForConfiguration)
			throw new IllegalStateException("setConfigClasses() may only be called before refresh()");
		List<Class<?>> classes = new ArrayList<Class<?>>();

		if (configClasses != null)
			for (Class<?> existingClass : configClasses)
				classes.add(existingClass);

		classes.add(cls);

		this.configClasses = classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * The base packages for configurations from Strings. These use the same
	 * conventions as the component scanning introduced in Spring 2.5.
	 */
	public void setBasePackages(String... basePackages) {
		if (closedForConfiguration)
			throw new IllegalStateException("setBasePackages() may only be called before refresh()");

		this.basePackages = basePackages;
	}

	@Override
	public void setParent(ApplicationContext context) {
		if (closedForConfiguration)
			throw new IllegalStateException("setParent() may only be called before refresh()");
		super.setParent(context);
	}

	protected String[] getBasePackages() {
		return basePackages;
	}

	protected Class<?>[] getConfigClasses() {
		return configClasses;
	}

	private void processAnyOuterClasses() {
		Class<?> outerConfig = null;
		if (configClasses != null && configClasses.length > 0) {
			for (Class<?> configClass : configClasses) {
				Class<?> candidate = configClass.getDeclaringClass();
				if (candidate != null && configurationPostProcessor.isConfigurationClass(candidate)) {
					if (outerConfig != null) {
						// TODO: throw a better exception
						throw new RuntimeException("cannot specify more than one inner configuration class");
					}
					outerConfig = candidate;
				}
			}
		}

		if (outerConfig != null)
			this.setParent(new JavaConfigApplicationContext(outerConfig));
	}

	@Override
	protected void finishRefresh() {
		closedForConfiguration = true;
	}

	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		if (configClasses != null && configClasses.length > 0)
			for (Class<?> cz : configClasses) {
				// TODO: ideally we'd use
				// configurationPostProcessor.isConfigurationClass() here,
				// but we cannot currently, because it's too permissive.
				// Abstract classes will get picked up just because they have a
				// single @Bean method (which is invalid). We need to do some
				// kind of policy object for what constitutes a 'valid'
				// configuration class. Something pluggable where we can
				// aggregate all the rules.
				if (ConfigurationUtils.isConfigurationClass(cz))
					beanFactory.registerBeanDefinition(cz.getName(), new RootBeanDefinition(cz, true));
			}

		if (basePackages != null && basePackages.length > 0)
			for (String location : basePackages)
				for (BeanDefinition bd : scanner.findCandidateComponents(location))
					beanFactory.registerBeanDefinition(bd.getBeanClassName(), bd);
	}

	public <T> T getBean(Class<T> type) {
		return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type);
	}

	public <T> T getBean(Class<T> type, String beanName) {
		return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type, beanName);
	}

	/**
	 * Reverse the contents of <var>array</var>.
	 * 
	 * <p/>This method is used for reversing the order of classes passed into
	 * constructors of
	 * {@link org.springframework.config.java.context.JavaConfigApplicationContext}
	 * or {@link org.springframework.config.java.annotation.Import}.
	 * 
	 * <p/>TODO: shouldn't actually be necessary. Root out the real issue with
	 * ordering
	 * 
	 * @see ConfigurationProcessor#reverse()
	 * @param array - array to reverse
	 * @return reverse of <var>array</var>, null if <var>array</var> is null.
	 */
	private static Class<?>[] reverse(Class<?>[] array) {
		if (array == null)
			return array;

		int size = array.length;
		Class<?>[] reversed = new Class<?>[size];

		for (int i = 0; i < size; i++)
			reversed[size - i - 1] = array[i];

		return reversed;
	}

}
