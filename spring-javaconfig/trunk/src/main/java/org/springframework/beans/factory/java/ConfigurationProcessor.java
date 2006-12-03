/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.java;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.annotation.DependencyCheck;
import org.springframework.beans.factory.annotation.ExternalBean;
import org.springframework.beans.factory.annotation.Lazy;
import org.springframework.beans.factory.annotation.Scope;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.CompositePropertySource;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * Class that processes Configuration beans.
 * <p>
 * A Configuration bean contains bean definition methods annotated with the Bean
 * annotationName. The Configuration class itself may optionally be annotated
 * with a Configuration annotationName setting global defaults.
 * <p>
 * Bean creation methods may be public or protected.
 * <p>
 * Typically used for only one configuration class in a single
 * BeanDefinitionRegistry.
 * <p>
 * Most of the actual work is performed by ConfigurationListeners, which makes
 * the processing of this class extensible. ConfigurationListeners react to
 * configuration methods and classes.
 * 
 * @author Rod Johnson
 * @see org.springframework.beans.factory.java.ConfigurationListener
 */
public class ConfigurationProcessor {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Bean factory that this post processor runs in
	 */
	private ConfigurableListableBeanFactory owningBeanFactory;

	/**
	 * Used to hold Spring AOP advisors and other internal objects while
	 * processing configuration. Object added to this factory can still benefit
	 * from autowiring and other IoC container features, but are not visible
	 * externally.
	 */
	private BeanNameTrackingDefaultListableBeanFactory childFactory;

	private ConfigurationListenerRegistry configurationListenerRegistry;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	// TODO finish strategy for using this
	private CompositePropertySource propertySource = new CompositePropertySource();

	/**
	 * Subclass of DefaultListableBeanFactory that keeps track of calls to
	 * getBean() to allow for context-sensitive behaviour in
	 * BeanMethodMethodInterceptor.
	 */
	private static class BeanNameTrackingDefaultListableBeanFactory extends DefaultListableBeanFactory {

		private static ThreadLocal<Stack<String>> namesHolder = new ThreadLocal<Stack<String>>() {
			@Override
			protected Stack<String> initialValue() {
				return new Stack<String>();
			}
		};

		private static Stack<String> names() {
			return (Stack<String>) namesHolder.get();
		}

		public BeanNameTrackingDefaultListableBeanFactory(BeanFactory parent) {
			super(parent);
		}

		@Override
		public Object getBean(String name) throws BeansException {
			recordRequestForBeanName(name);
			try {
				Object result = super.getBean(name);
				return result;
			}
			finally {
				pop();
			}
		}

		public void recordRequestForBeanName(String name) {
			names().push(name);
		}

		private String pop() {
			return names().pop();
		}

		public String lastRequestedBeanName() {
			return names().empty() ? null : names().peek();
		}
	} 	// class BeanNameTrackingDefaultListableBeanFactory
	

	public ConfigurationProcessor(ConfigurableApplicationContext ac, ConfigurationListenerRegistry clr) {
		init(ac.getBeanFactory(), clr);
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Create a configuration processor. This is tied to an owning factory.
	 * 
	 * @param bdr owning factory
	 */
	public ConfigurationProcessor(ConfigurableListableBeanFactory bdr, ConfigurationListenerRegistry clr) {
		init(bdr, clr);
	}

	private void init(ConfigurableListableBeanFactory bdr, ConfigurationListenerRegistry clr) {
		owningBeanFactory = bdr;
		childFactory = new BeanNameTrackingDefaultListableBeanFactory(owningBeanFactory);
		this.configurationListenerRegistry = clr;
	}

	/**
	 * Generate bean definitions
	 * 
	 * @param configObject
	 */
	public void process(Class<?> configClass) throws BeanDefinitionStoreException {
		if (!isConfigurationClass(configClass)) {
			throw new BeanDefinitionStoreException(configClass.getName()
					+ " contains no Bean creation methods or Aspect methods");
		}

		if (Modifier.isFinal(configClass.getModifiers())) {
			throw new BeanDefinitionStoreException("Configuration class " + configClass.getName() + " my not be final");
		}

		// TODO fix
		String configurerBeanName = configClass.getName();
		Class<?> configSubclass = createConfigurationSubclass(configClass);
		((DefaultListableBeanFactory) owningBeanFactory).registerBeanDefinition(configurerBeanName,
				new RootBeanDefinition(configSubclass));

		generateBeanDefinitions(configurerBeanName, configClass);
	}
	

	/**
	 * Modify metadata by emitting new bean definitions based on the bean
	 * creation methods in this Java file
	 * 
	 * @param configurerBeanName name of the bean containing the factory methods
	 * @param configurerClass class of the configurer bean instance
	 */
	public void generateBeanDefinitions(final String configurerBeanName, final Class<?> configurerClass) {

		// Callback listeners
		for (ConfigurationListener cl : configurationListenerRegistry.getConfigurationListeners()) {
			cl.configurationClass(owningBeanFactory, childFactory, configurerBeanName, configurerClass);
		}
		
		// Only want to consider most specific bean creation method, in the case of overrides
		final Set<String> noArgMethodsSeen = new HashSet<String>();

		ReflectionUtils.doWithMethods(configurerClass, new MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				Bean beanAnnotation = findBeanAnnotation(m, configurerClass);
				if (beanAnnotation != null && !noArgMethodsSeen.contains(m.getName())) {
					
					// If the bean already exists in the factory, don't emit a bean definition
					// This may or may not be legal, depending on whether the @Bean annotation
					// allows overriding
					if (owningBeanFactory.containsBean(m.getName())) {
						if (!beanAnnotation.allowOverriding()) {
							throw new IllegalStateException("Already have a bean with name '" + m.getName() + "'");
						}
						else {
							// Don't emit a bean definition
							return;
						}
					}					
					noArgMethodsSeen.add(m.getName());
					generateBeanDefinitionFromBeanCreationMethod(owningBeanFactory, configurerBeanName,
							configurerClass, m, beanAnnotation);// , cca);
				}
				else {
					for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners()) {
						cml.otherMethod(owningBeanFactory, childFactory, configurerBeanName, configurerClass, m);
					}
				}
			}
		});

		// TODO do we need this?
		// , ReflectionUtils.DECLARED_METHODS);

		// Now process fields.
		// Use ReflectionUtils2 to go up tree with private fields
		// ReflectionUtils2.doWithFields(configurerClass, new
		// ReflectionUtils2.FieldCallback() {
		// public void doWith(Field f) throws IllegalArgumentException,
		// IllegalAccessException {
		// if (f.getAnnotation(Bean.class) != null) {
		// generateBeanDefinitionFromBeanField(owningBeanFactory,
		// configurerBeanName, configurerClass, f);
		// }
		// };
		// });

		// Find inner aspect classes
		// TODO: need to go up tree? ReflectionUtils.doWithClasses
		for (Class innerClass : configurerClass.getDeclaredClasses()) {
			if (Modifier.isStatic(innerClass.getModifiers())) {
				process(innerClass);
			}
		}
	}

	/**
	 * Create a new subclass of the given configuration class
	 * 
	 * @param configurationClass class with Configuration attribute or otherwise
	 *            indicated as a configuration class
	 * @return subclass of this class that will behave correctly with AOP
	 *         weaving and singleton caching. For example, the original class
	 *         will return a new instance on every call to a bean() method that
	 *         has singleton scope. The subclass will cache that, and also
	 *         perform AOP weaving.
	 */
	public Class createConfigurationSubclass(Class<?> configurationClass) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(configurationClass);

		enhancer.setCallbackFilter(BEAN_CREATION_METHOD_CALLBACK_FILTER);
		enhancer.setCallbackTypes(new Class[] { 
				NoOp.class, 
				BeanMethodMethodInterceptor.class,
				ExternalBeanMethodMethodInterceptor.class
		});
		enhancer.setUseFactory(false);
		// TODO can we generate a method to expose each private bean field here?
		// Otherwise may need to generate a static or instance map, with
		// multiple get() methods
		// Listeners don't get callback on this also

		Class configurationSubclass = enhancer.createClass();
		Enhancer.registerCallbacks(configurationSubclass, new Callback[] { 
					NoOp.INSTANCE,
					new BeanMethodMethodInterceptor(),
					new ExternalBeanMethodMethodInterceptor()
				});

		return configurationSubclass;
	}

	/**
	 * Intercept only bean creation methods
	 */
	private static CallbackFilter BEAN_CREATION_METHOD_CALLBACK_FILTER = new CallbackFilter() {

		public int accept(Method m) {
			// We don't intercept non-public methods like finalize
			if (!Modifier.isPublic(m.getModifiers())) {
				return 0;
			}
			if (isBeanDefinitionMethod(m, m.getDeclaringClass())) {
				return 1;
			}
			if (AnnotationUtils.findAnnotation(m, ExternalBean.class) != null) {
				return 2;
			}
			return 0;
		}
	};

	/**
	 * CGLIB MethodInterceptor that applies to methods on the configuration
	 * instance. Purpose: subclass configuration to ensure that singleton
	 * methods return the same object on subsequent invocations, including
	 * self-invocation. Do need one of these per intercepted class.
	 */
	private class BeanMethodMethodInterceptor implements MethodInterceptor {
		private Map<Method, Object> singletons = new HashMap<Method, Object>();

		public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
			Bean ann = findBeanAnnotation(m, o.getClass());
			if (ann == null) {
				// Not a bean, don't change the method implementation
				return mp.invokeSuper(o, args);
			}
			else {
				return returnWrappedResultMayBeCached(o, m, args, mp, ann.scope() == Scope.SINGLETON);
			}
		}

		private Object returnWrappedResultMayBeCached(Object o, Method m, Object[] args, MethodProxy mp,
				boolean useCache) throws Throwable {
			if (!useCache) {
				return wrapResult(o, args, m, mp);
			}

			// Cache result, for singleton style behaviour,
			// where the bean creation method always returns the same value
			synchronized (o) {
				Object cached = singletons.get(m);
				if (cached == null) {
					cached = wrapResult(o, args, m, mp);
					singletons.put(m, cached);
				}
				return cached;
			}
		}

		/**
		 * Wrap the result of a bean definition method in a Spring AOP proxy if
		 * there are advisors in the current factory that would apply to it.
		 * Note that the advisors may have been added explicitly by the user or
		 * may have resulted from Advisor generation on this class processing a
		 * Pointcut annotationName
		 * 
		 * @param o
		 * @param args
		 * @param mp
		 * @return
		 * @throws Throwable
		 */
		private Object wrapResult(Object o, Object[] args, Method m, MethodProxy mp) throws Throwable {

			// If we are in our first call to getBean() with this name and were
			// not
			// called by the factory (which would have tracked the call), call
			// the factory
			// to get the bean. We need to do this to ensure that lifecycle
			// callbacks are invoked,
			// so that calls made within a factory method in otherBean() style
			// still
			// get fully configured objects.
			String lastRequestedBeanName = childFactory.lastRequestedBeanName();
			if (lastRequestedBeanName != null && !m.getName().equals(lastRequestedBeanName)) {
				return childFactory.getBean(m.getName());
			}

			try {
				if (lastRequestedBeanName == null) {
					// Remember the getBean() method we're now executing,
					// if we were invoked from within the factory method in the
					// configuration class
					// rather than through the BeanFactory
					childFactory.recordRequestForBeanName(m.getName());
				}
				
				// Get raw result of @Bean method or get bean from factory if it is overriden
				Object originallyCreatedBean = null;
				BeanDefinition beanDef = owningBeanFactory.getBeanDefinition(m.getName());
				if (beanDef instanceof RootBeanDefinition) {
					RootBeanDefinition rbdef = (RootBeanDefinition) beanDef;
					if (rbdef.getFactoryBeanName() == null) {
						// We have a regular bean definition already in the factory:
						// use that instead of the @Bean method
						originallyCreatedBean = owningBeanFactory.getBean(m.getName());
					}
				}
				if (originallyCreatedBean == null) {
					originallyCreatedBean = mp.invokeSuper(o, args);
				}
				
				if (!configurationListenerRegistry.getConfigurationListeners().isEmpty()) {
					// We know we have advisors that may affect this object
					// Prepare to proxy it
					ProxyFactory pf;
					if (shouldProxyBeanCreationMethod(m, o.getClass())) {
						pf = new ProxyFactory(originallyCreatedBean);
						pf.setProxyTargetClass(true);
					}
					else {
						pf = new ProxyFactory(new Class[] { m.getReturnType() });
						pf.setProxyTargetClass(false);
						pf.setTarget(originallyCreatedBean);
					}

					boolean customized = false;
					for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners()) {
						customized = customized
								|| cml.processBeanMethodReturnValue(owningBeanFactory, childFactory,
										originallyCreatedBean, m, pf);
					}

					// Only proxy if we know that advisors apply to this bean
					if (customized || pf.getAdvisors().length > 0) {
						// Ensure that AspectJ pointcuts will work
						pf.addAdvice(0, ExposeInvocationInterceptor.INSTANCE);
						if (pf.isProxyTargetClass() && Modifier.isFinal(m.getReturnType().getModifiers())) {
							throw new BeanDefinitionStoreException(m + " is eligible for proxying target class "
									+ "but return type " + m.getReturnType().getName() + " is final");
						}
						return pf.getProxy();
					}
					else {
						return originallyCreatedBean;
					}
				}
				else {
					// There can be no advisors
					return originallyCreatedBean;
				}
			}
			finally {
				if (lastRequestedBeanName == null) {
					childFactory.pop();
				}
			}
		}
	}
	
	/**
	 * MethodInterceptor that returns the result of a getBean() call for an external bean.
	 */
	private class ExternalBeanMethodMethodInterceptor implements MethodInterceptor {
		
		public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
			return owningBeanFactory.getBean(m.getName());
		}
	}

	/**
	 * Use CGLIB only if the return type isn't an interface
	 * 
	 * @param m
	 * @param c
	 * @return
	 */
	private boolean shouldProxyBeanCreationMethod(Method m, Class<?> c) {
		Bean bean = findBeanAnnotation(m, c);
		// TODO need to consider autowiring enabled at factory level
		return !m.getReturnType().isInterface() || bean.autowire().isAutowire();
	}

	/**
	 * Find all bean creation methods in the given configuration class
	 * 
	 * @param configurationClass
	 * @return
	 */
	private static Collection<Method> beanCreationMethods(Class<?> configurationClass) {
		Collection<Method> beanCreationMethods = new LinkedList<Method>();
		Method[] publicMethods = configurationClass.getMethods();
		for (int i = 0; i < publicMethods.length; i++) {
			if (isBeanDefinitionMethod(publicMethods[i], configurationClass)) {
				beanCreationMethods.add(publicMethods[i]);
			}
		}
		return beanCreationMethods;
	}

	protected static boolean isBeanDefinitionMethod(Method m, Class<?> c) {
		return findBeanAnnotation(m, c) != null;
	}

	/**
	 * Annotations on methods are not inherited by default, so we need to handle
	 * this explicitly
	 * 
	 * @param m
	 * @param c
	 * @return
	 */
	protected static Bean findBeanAnnotation(Method m, Class<?> c) {
		// TODO: what is C used for ?
		// previous method - findMethodAnnotation (refactored in AnnotationUtils
		// rev 1.3)
		return AnnotationUtils.findAnnotation(m, Bean.class);
	}

	protected boolean isConfigurationClass(Class<?> candidateConfigurationClass) {
		if (candidateConfigurationClass.isAnnotationPresent(Configuration.class)
				|| !beanCreationMethods(candidateConfigurationClass).isEmpty()) {
			return true;
		}
		for (ConfigurationListener cl : configurationListenerRegistry.getConfigurationListeners()) {
			if (cl.understands(candidateConfigurationClass)) {
				return true;
			}
		}
		return false;
	}

	private void generateBeanDefinitionFromBeanCreationMethod(ConfigurableListableBeanFactory beanFactory,
			String configurerBeanName, Class<?> configurerClass, Method beanCreationMethod, Bean beanAnnotation) {
		log.debug("Found bean creation method " + beanCreationMethod);

		validateBeanCreationMethod(beanCreationMethod);

		// Create a bean definition from the method
		RootBeanDefinition rbd = new RootBeanDefinition(beanCreationMethod.getReturnType());
		rbd.setFactoryBeanName(configurerBeanName);
		rbd.setFactoryMethodName(beanCreationMethod.getName());
		copyAttributes(beanCreationMethod.getName(), beanAnnotation,
				(Configuration) configurerClass.getAnnotation(Configuration.class), rbd, beanFactory);
		rbd.setResourceDescription("Bean creation method " + beanCreationMethod.getName() + " in class "
				+ beanCreationMethod.getDeclaringClass().getName());

		ConfigurationListener.BeanDefinitionRegistration beanDefinitionRegistration = new ConfigurationListener.BeanDefinitionRegistration(
				rbd, beanCreationMethod.getName());
		beanDefinitionRegistration.hide = !Modifier.isPublic(beanCreationMethod.getModifiers());

		for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners()) {
			cml.beanCreationMethod(beanDefinitionRegistration, beanFactory, childFactory, configurerBeanName,
					configurerClass, beanCreationMethod, beanAnnotation);
		}

		// Not currently used
		// addPropertiesIndicatedByGetterInvocations(configurerClass,
		// beanCreationMethod, rbd);

		// TODO allow use of null return value to suppress bean
		if (beanDefinitionRegistration.hide) {
			childFactory.registerBeanDefinition(beanDefinitionRegistration.name, beanDefinitionRegistration.rbd);
		}
		else {
			((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(beanDefinitionRegistration.name,
					beanDefinitionRegistration.rbd);
		}
	}

	/**
	 * Add properties indicated by getter invocations as found by ASM analysis
	 * 
	 * @param configurerClass
	 * @param beanCreationMethod
	 * @param rbd
	 */
	// private void addPropertiesIndicatedByGetterInvocations(Class<?>
	// configurerClass, Method beanCreationMethod, RootBeanDefinition rbd) {
	// try {
	// String pathString = getPathString(configurerClass);
	// System.out.println("pathString=" + pathString);
	// Resource r = resourceLoader.getResource(pathString);
	// System.out.println(r);
	// if (r.exists()) {
	// InputStream is = r.getInputStream();
	// if (is != null) {
	// ClassReader cr = new ClassReader(is);
	// GetterInvocationFindingClassVisitor gifcv = new
	// GetterInvocationFindingClassVisitor();
	// cr.accept(gifcv, false);
	//					
	// // Add additional properties to bean definition based
	// // on invoked getters
	// List<String> gettersInvoked =
	// gifcv.getGetterInvocations().get(beanCreationMethod.getName());
	// if (gettersInvoked != null) {
	// for (String nameOfGetterInvoked : gettersInvoked) {
	// System.err.println(nameOfGetterInvoked);
	// String propertyName =
	// getBeanPropertyNameForMethodName(nameOfGetterInvoked);
	// rbd.getPropertyValues().addPropertyValue(new PropertyValue(propertyName,
	// resolvePropertyValue(beanCreationMethod.getName(), propertyName)));
	// }
	// }
	// }
	// }
	// }
	// catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// }
	//	
	// protected Object resolvePropertyValue(String name, String propertyName) {
	// throw new UnsupportedOperationException("resolve " + name + "." +
	// propertyName);
	// }
	//
	// // TODO must have this somewhere
	// private static String getBeanPropertyNameForMethodName(String methodName)
	// {
	// return Character.toLowerCase(methodName.charAt(3)) +
	// methodName.substring(4);
	// }
	//
	// private String getPathString(Class<?> configurerClass) {
	// String className = configurerClass.getName();
	// className = "classpath:" + className;
	// className = StringUtils.replace(className, ".", "/");
	// return className + ".class";
	// }
	private void validateBeanCreationMethod(Method beanCreationMethod) throws BeanDefinitionStoreException {
		if (Modifier.isFinal(beanCreationMethod.getModifiers())) {
			throw new BeanDefinitionStoreException("Bean creation method " + beanCreationMethod.getName()
					+ " may not be final");
		}
		if (beanCreationMethod.getReturnType() == Void.TYPE) {
			throw new BeanDefinitionStoreException("Bean creation method " + beanCreationMethod.getName()
					+ " may not have void return");
		}
	}

	/**
	 * Copy from bean annotationName into bean definition
	 * 
	 * @param beanName name of the bean we're creating (not the factory bean)
	 * @param beanAnnotation bean annotationName
	 * @param configuration configuration annotationName on the configuration
	 *            class. Sets defaults. May be null as this annotationName is
	 *            not required.
	 * @param rbd bean definition, in Spring IoC container internal metadata
	 * @param beanFactory bean factory we are executing in
	 */
	private static void copyAttributes(String beanName, Bean beanAnnotation, Configuration configuration,
			RootBeanDefinition rbd, ConfigurableListableBeanFactory beanFactory) {
		rbd.setSingleton(beanAnnotation.scope() == Scope.SINGLETON);

		if (beanAnnotation.lazy() != Lazy.UNSPECIFIED) {
			rbd.setLazyInit(beanAnnotation.lazy().booleanValue());
		}
		else if (configuration != null && configuration.defaultLazy() != Lazy.UNSPECIFIED) {
			rbd.setLazyInit(configuration.defaultLazy().booleanValue());
		}

		if (beanAnnotation.autowire() != Autowire.INHERITED) {
			rbd.setAutowireMode(beanAnnotation.autowire().value());
		}
		else if (configuration != null && configuration.defaultAutowire() != Autowire.INHERITED) {
			rbd.setAutowireMode(configuration.defaultAutowire().value());
		}

		if (beanAnnotation.initMethodName().length() != 0) {
			rbd.setInitMethodName(beanAnnotation.initMethodName());
		}
		if (beanAnnotation.destroyMethodName().length() != 0) {
			rbd.setDestroyMethodName(beanAnnotation.destroyMethodName());
		}
		if (beanAnnotation.dependencyCheck() != DependencyCheck.UNSPECIFIED) {
			rbd.setDependencyCheck(beanAnnotation.dependencyCheck().value());
		}
		else if (configuration != null && configuration.defaultDependencyCheck() != DependencyCheck.UNSPECIFIED) {
			rbd.setDependencyCheck(configuration.defaultDependencyCheck().value());
		}
		rbd.setDependsOn(beanAnnotation.dependsOn());
		for (String alias : beanAnnotation.aliases()) {
			beanFactory.registerAlias(beanName, alias);
		}
	}

}
