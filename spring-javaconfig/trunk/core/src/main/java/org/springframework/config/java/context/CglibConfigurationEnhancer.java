package org.springframework.config.java.context;

import static java.lang.String.format;
import static org.springframework.config.java.context.BeanVisibility.visibilityOf;
import static org.springframework.config.java.util.DefaultScopes.SINGLETON;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.hasLength;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.model.ConfigurationModelBeanDefinitionReader;
import org.springframework.config.java.model.JavaConfigAspectRegistry;
import org.springframework.config.java.model.ModelMethod;
import org.springframework.config.java.valuesource.ValueResolutionException;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

public class CglibConfigurationEnhancer implements ConfigurationEnhancer {
	private static final Log log = LogFactory.getLog(CglibConfigurationEnhancer.class);
	private final JavaConfigBeanFactory beanFactory;

	public CglibConfigurationEnhancer(JavaConfigBeanFactory beanFactory) {
		notNull(beanFactory, "beanFactory must be non-null");
		this.beanFactory = beanFactory;
	}

	public String enhance(String configClassName) {
		if(log.isInfoEnabled())
			log.info("Enhancing " + configClassName);

		Class<?> configClass = loadClassFromName(configClassName);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(configClass);
		enhancer.setUseFactory(false);
		enhancer.setCallbackFilter(new CallbackFilter() {
			public int accept(Method candidateMethod) {
				if(findAnnotation(candidateMethod, Bean.class) != null)
					return 1;
				if(findAnnotation(candidateMethod, ExternalBean.class) != null)
					return 2;
				if(findAnnotation(candidateMethod, AutoBean.class) != null)
					return 3;
				if(findAnnotation(candidateMethod, ExternalValue.class) != null)
					return 4;
				return 0;
			}
		});
		enhancer.setCallbackTypes(
			new Class<?>[] {
    			NoOp.class,
    			BeanMethodInterceptor.class,
    			ExternalBeanMethodInterceptor.class,
    			AutoBeanMethodInterceptor.class,
    			ExternalValueMethodInterceptor.class
			});

		Class<?> enhancedSubclass = enhancer.createClass();

		Enhancer.registerCallbacks(enhancedSubclass,
			new Callback[] {
				NoOp.INSTANCE,
				new BeanMethodInterceptor(beanFactory),
				new ExternalBeanMethodInterceptor(beanFactory),
				new AutoBeanMethodInterceptor(beanFactory),
				new ExternalValueMethodInterceptor(beanFactory)
			});

		if(log.isInfoEnabled())
			log.info(format("Successfully enhanced %s; enhanced class name is: %s",
							  configClassName, enhancedSubclass.getName()));

		return enhancedSubclass.getName();
	}

	private Class<?> loadClassFromName(String configClassName) {
		try {
			return Class.forName(configClassName);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("class must be loadable", ex);
		}
	}

	static abstract class AbstractMethodInterceptor implements MethodInterceptor {
		protected final Log log = LogFactory.getLog(this.getClass());
		protected final JavaConfigBeanFactory beanFactory;

		public AbstractMethodInterceptor(JavaConfigBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
			ModelMethod beanMethod = ModelMethod.forMethod(m);
			String beanName = beanFactory.getBeanNamingStrategy().getBeanName(beanMethod);
			return doIntercept(o, m, args, mp, beanName);
		}

		protected abstract Object doIntercept(Object o, Method m, Object[] args, MethodProxy mp, String beanName) throws Throwable;
	}

	static class ExternalBeanMethodInterceptor extends AbstractMethodInterceptor {

		public ExternalBeanMethodInterceptor(JavaConfigBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		public Object doIntercept(Object o, Method m, Object[] args, MethodProxy mp, String beanName) throws Throwable {
			ExternalBean extBean = AnnotationUtils.findAnnotation(m, ExternalBean.class);
			Assert.notNull(extBean, "ExternalBean methods must be annotated with @ExternalBean");

			String alternateName = extBean.value();

			return beanFactory.getBean(hasLength(alternateName) ? alternateName : beanName);
		}
	}

	static class ExternalValueMethodInterceptor extends AbstractMethodInterceptor {
		private ValueSource valueSource;

		public ExternalValueMethodInterceptor(JavaConfigBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		public Object doIntercept(Object o, Method m, Object[] args, MethodProxy mp, String beanName) throws Throwable {
			ExternalValue metadata = AnnotationUtils.findAnnotation(m, ExternalValue.class);
			Assert.notNull(metadata, "ExternalValue methods must be annotated with @ExternalValue");

    		String name = metadata.value();
    		if (!hasLength(name)) {
    			// no explicit name provided -> use method name
    			// TODO: {naming strategy} plug in naming strategy
    			name = m.getName();
    			// Strip property name if needed
    			if (name.startsWith("get") && Character.isUpperCase(name.charAt(3)))
    				name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
    		}

			Class<?> requiredType = m.getReturnType();

			if(valueSource == null)
				initializeValueSource(m);

			try {
				return valueSource.resolve(name, requiredType);
			}
			catch(ValueResolutionException ex) {
				// value was not found in properties -> default to the body of the method (if any exists)
				if(Modifier.isAbstract(m.getModifiers()))
					throw ex; // cannot call super implementation if it's abstract.

				return mp.invokeSuper(o, args);
			}
		}

		/**
		 * Lazily initializes value source by retrieving it from the beanFactory. This design
		 * allows this method interceptor to be wired up by {@link CglibConfigurationEnhancer}
		 * eagerly and then only cause a failure if a user actually tries to access an
		 * {@link ExternalValue @ExternalValue} method without having provided a
		 * {@link ResourceBundles @ResourceBundles} annotation.
		 */
		private void initializeValueSource(Method m) {
    		if(beanFactory.containsBean("valueSource")) {
    			valueSource = (ValueSource) beanFactory.getBean("valueSource");
    		}
    		else {
    			String className = m.getDeclaringClass().getSimpleName();
    			// TODO: incorporate BeanNamingStrategy here
    			String methodName = m.getName();
    			throw new IllegalStateException(format("No ValueSource bean could be found in " +
    					"beanFactory while trying to resolve @ExternalValue method %s.%s. " +
    					"Perhaps no @ResourceBundles annotation was provided on %s?",
    					className, methodName, className));
    		}
		}
	}

	static class AutoBeanMethodInterceptor extends AbstractMethodInterceptor {

		public AutoBeanMethodInterceptor(JavaConfigBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		public Object doIntercept(Object o, Method m, Object[] args, MethodProxy mp, String beanName) throws Throwable {
			AutoBean metadata = AnnotationUtils.findAnnotation(m, AutoBean.class);
			Assert.notNull(metadata, "AutoBean methods must be annotated with @AutoBean");

			return beanFactory.getBean(beanName);
		}
	}

	/**
	 * Intercepts calls to {@link Bean @Bean} methods delegating to
	 * {@link #intercept(Object, Method, Object[], MethodProxy) intercept()} in order
	 * to ensure proper bean functionality: singleton, AOP proxying, etc.
	 *
	 * @author Chris Beams
	 */
	static class BeanMethodInterceptor extends AbstractMethodInterceptor {
		private final JavaConfigAspectRegistry aspectRegistry;

		public BeanMethodInterceptor(JavaConfigBeanFactory beanFactory) {
			super(beanFactory);
			this.aspectRegistry = JavaConfigAspectRegistry.retrieveFrom(beanFactory);
		}

		/**
		 * Enhances a {@link Bean @Bean} method to check the supplied BeanFactory for the existence
		 * of this bean object
		 */
		@Override
		public Object doIntercept(Object o, Method m, Object[] args, MethodProxy mp, String beanName) throws Throwable {
			boolean isScopedProxy = (AnnotationUtils.findAnnotation(m, ScopedProxy.class) != null);
			String scopedBeanName = ConfigurationModelBeanDefinitionReader.resolveHiddenScopedProxyBeanName(beanName);
			if(isScopedProxy && beanFactory.isCurrentlyInCreation(scopedBeanName))
				beanName = scopedBeanName;

			if(factoryContainsBean(beanName)) {
				// we have an already existing cached instance of this bean -> retrieve it
				Object cachedBean = beanFactory.getBean(beanName);
				if(log.isInfoEnabled())
					log.info(format("Returning cached singleton object [%s] for @Bean method %s.%s",
							cachedBean, m.getDeclaringClass().getSimpleName(), beanName));
				return cachedBean;
			}

			// no instance exists yet -> create a new one
			Object bean = mp.invokeSuper(o, args);

			Bean metadata = AnnotationUtils.findAnnotation(m, Bean.class);
			bean = aspectRegistry.proxyIfAnyPointcutsApply(bean, m, metadata);

			if(metadata.scope().equals(SINGLETON)) {
				BeanVisibility visibility = visibilityOf(m.getModifiers());
				if(log.isInfoEnabled())
					log.info(format("Registering new %s singleton object [%s] for @Bean method %s.%s",
						visibility, bean, m.getDeclaringClass().getSimpleName(), beanName));

				beanFactory.registerSingleton(beanName, bean, visibility);
			}

			return bean;
		}

		/**
		 * Check the beanFactory to see whether the bean named <var>beanName</var> already exists.
		 * Accounts for the fact that the requested bean may be "in creation", i.e.: we're in the
		 * middle of servicing the initial request for this bean.  From JavaConfig's perspective,
		 * this means that the bean does not actually yet exist, and that it is now our job to create
		 * it for the first time by executing the logic in the corresponding Bean method.
		 *
		 * @param beanName name of bean to check for
		 * @return true if <var>beanName</var> already exists in beanFactory
		 */
		private boolean factoryContainsBean(String beanName) {
		 	return beanFactory.containsBean(beanName)
				&& !beanFactory.isCurrentlyInCreation(beanName);
		}

	}

}
