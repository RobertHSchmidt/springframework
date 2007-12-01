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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.AmbiguousBeanLookupException;
import org.springframework.beans.factory.MultiplePrimaryBeanDefinitionException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.TypeSafeBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.util.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.util.Assert;

/**
 * TODO: Document
 * 
 * Annotation-aware application context that looks for classes annotated with
 * the Configuration annotation and registers the beans they define.
 * 
 * @author Chris Beams
 */
public class JavaConfigApplicationContext extends AbstractRefreshableApplicationContext implements TypeSafeBeanFactory {

	protected final List<Class<?>> configClasses = new ArrayList<Class<?>>();

	/**
	 * The base packages for configurations from Strings. These use the same
	 * conventions as the component scanning introduced in Spring 2.5.
	 */
	protected final Set<String> basePackages = new HashSet<String>();

	protected JavaConfigBeanDefinitionLoader beanDefinitionLoader;

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

	/**
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

		if (!isEmpty(classes))
			setConfigClasses(classes);

		if (!isEmpty(basePackages))
			setBasePackages(basePackages);

		refresh();
	}

	private Map<Class<?>, ? extends JavaConfigApplicationContext> contextRegistry;

	public Map<Class<?>, ? extends JavaConfigApplicationContext> getContextRegistry() {
		return contextRegistry;
	}

	private static Class<?>[] reverse(Class<?>[] array) {
		int size = array.length;
		Class<?>[] reversed = new Class<?>[size];

		for (int i = 0; i < size; i++)
			reversed[size - i - 1] = array[i];

		return reversed;
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

	public void setBasePackages(String... basePackages) {
		Assert.notEmpty(basePackages, "must supply at least one base package");
		if (closedForConfiguration)
			throw new IllegalStateException("setBasePackages() must be called before refresh()");
		this.basePackages.addAll(Arrays.asList(basePackages));
	}

	@Override
	public void setParent(ApplicationContext context) {
		if (closedForConfiguration)
			throw new IllegalStateException("setParent() must be called before refresh()");
		super.setParent(context);
	}

	@Override
	protected void prepareRefresh() {
		if (configClasses.isEmpty() && basePackages.isEmpty())
			throw new IllegalStateException("must supply at least one class or base package");

		initBeanDefinitionLoader();
		registerDefaultPostProcessors();
	}

	@Override
	protected void finishRefresh() {
		closedForConfiguration = true;
	}

	@Override
	protected final void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		beanDefinitionLoader.loadBeanDefinitions(beanFactory);
	}

	/**
	 * Register the default post processors used for parsing Spring classes.
	 * 
	 * @see JavaConfigBeanFactoryPostProcessorRegistry
	 */
	protected void registerDefaultPostProcessors() {
		new JavaConfigBeanFactoryPostProcessorRegistry().addAllPostProcessors(this);
	}

	/**
	 * Optionally override an implementation's internally-provided default bean
	 * definition loader. A custom bean definition loader would be used to
	 * implement special processing of configClasses and basePackages.
	 * 
	 * @param beanDefLoader - custom bean definition loader
	 * @see JavaConfigBeanDefinitionLoader
	 * @see DefaultJavaConfigBeanDefinitionLoader
	 * 
	 * TODO: Revise Documentation
	 */
	protected void initBeanDefinitionLoader() {
		beanDefinitionLoader = new DefaultJavaConfigBeanDefinitionLoader(this, configClasses, basePackages);
	}

}
