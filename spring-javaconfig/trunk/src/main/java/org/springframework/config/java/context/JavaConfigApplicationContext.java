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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.AmbiguousBeanLookupException;
import org.springframework.beans.factory.MultiplePrimaryBeanDefinitionException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.TypeSafeBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.util.ArrayUtils;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.util.Assert;

/**
 * <p/>Application context that looks for classes annotated with the
 * {@link org.springframework.config.java.annotation.Configuration} annotation
 * and registers the {@link org.springframework.config.java.annotation.Bean}s
 * they define; is the primary programmatic resource for using Spring
 * JavaConfig.
 * 
 * <p/>TODO: Document: refine the example below. use type-safe getBean()? Extend
 * ConfigurationSupport?
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
 * @see Configuration
 * @see org.springframework.config.java.annotation.Bean
 * 
 * @author Chris Beams
 */
public class JavaConfigApplicationContext extends AbstractRefreshableApplicationContext implements TypeSafeBeanFactory {

	protected final List<Class<?>> configClasses = new ArrayList<Class<?>>();

	protected boolean closedForConfiguration = false;

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

	private ConfigurationScanner scanner = new ConfigurationScanner(this);

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

		ArrayList<Class<?>> allClasses = new ArrayList<Class<?>>();

		if (!isEmpty(classes))
			allClasses.addAll(Arrays.asList(classes));

		if (!isEmpty(basePackages))
			for (String basePackage : basePackages)
				allClasses.addAll(scanner.scanPackage(basePackage));

		if (!allClasses.isEmpty())
			setConfigClasses(allClasses.toArray(new Class<?>[] {}));

		refresh();
	}

	/*
	 * XXX: Review
	 * @see org.springframework.beans.factory.TypeSafeBeanFactory#getBean(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> type) {
		Map<String, Object> beansOfType = getBeansOfType(type);
		int matchingBeanCount = beansOfType.size();

		// happy path -- there is exactly one matching bean: return it.
		if (matchingBeanCount == 1)
			return (T) beansOfType.values().iterator().next();

		// no matches: throw.
		if (matchingBeanCount == 0)
			throw new NoSuchBeanDefinitionException(type, "");

		// there is more than one instance: attempt to find a primary bean
		ArrayList<String> primaryCandidates = new ArrayList<String>();
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		for (String beanName : beansOfType.keySet()) {
			// XXX: Review - having to cast here is odd; there's probably a
			// better way
			AbstractBeanDefinition beanDef = (AbstractBeanDefinition) beanFactory.getBeanDefinition(beanName);
			if (beanDef.isPrimary()) {
				primaryCandidates.add(beanName);
			}
		}

		int primaryCandidateCount = primaryCandidates.size();

		if (primaryCandidateCount == 0) {
			throw new AmbiguousBeanLookupException(type, beansOfType);
		}
		if (primaryCandidateCount > 1) {
			throw new MultiplePrimaryBeanDefinitionException(type, primaryCandidates);
		}
		// exactly one primary candidate found
		return (T) beanFactory.getBean(primaryCandidates.get(0));
	}

	/*
	 * XXX: Review
	 * @see org.springframework.beans.factory.TypeSafeBeanFactory#getBean(Class,
	 * String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> type, String beanName) {
		return (T) getBean(beanName, type);
	}

	public void setConfigClasses(Class<?>... classes) {
		Assert.notEmpty(classes, "must supply at least one configuration class");
		if (closedForConfiguration)
			throw new IllegalStateException("setConfigClasses() must be called before refresh()");
		this.configClasses.addAll(Arrays.asList(ArrayUtils.reverse(classes)));
	}

	/**
	 * The base packages for configurations from Strings. These use the same
	 * conventions as the component scanning introduced in Spring 2.5.
	 */
	public void setBasePackages(String... basePackages) {
		Assert.notEmpty(basePackages, "must supply at least one base package");
		if (closedForConfiguration)
			throw new IllegalStateException("setBasePackages() must be called before refresh()");

		ArrayList<Class<?>> allClasses = new ArrayList<Class<?>>();
		for (String basePackage : basePackages)
			allClasses.addAll(scanner.scanPackage(basePackage));
		Collections.reverse(allClasses);
		this.configClasses.addAll(allClasses);
	}

	@Override
	public void setParent(ApplicationContext context) {
		if (closedForConfiguration)
			throw new IllegalStateException("setParent() must be called before refresh()");
		super.setParent(context);
	}

	@Override
	protected void prepareRefresh() {
		if (configClasses.isEmpty())
			throw new IllegalStateException("must supply at least one class or base package");

		processAnyOuterClasses();

		registerDefaultPostProcessors();
	}

	private void processAnyOuterClasses() {
		Class<?> outerConfig = null;
		for (Class<?> configClass : configClasses) {
			Class<?> candidate = configClass.getDeclaringClass();
			if (candidate != null && ClassUtils.isConfigurationClass(candidate)) {
				if (outerConfig != null) {
					// TODO: throw a better exception
					throw new RuntimeException("cannot specify more than one inner configuration class");
				}
				outerConfig = candidate;
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
	protected final void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		for (Class<?> cz : configClasses)
			if (ClassUtils.isConfigurationClass(cz))
				beanFactory.registerBeanDefinition(cz.getName(), new RootBeanDefinition(cz, true));
	}

	/**
	 * Register the default post processors used for parsing Spring classes.
	 * 
	 * @see JavaConfigBeanFactoryPostProcessorRegistry
	 */
	protected void registerDefaultPostProcessors() {
		new JavaConfigBeanFactoryPostProcessorRegistry().addAllPostProcessors(this);
	}

}
