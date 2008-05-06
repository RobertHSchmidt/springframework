package org.springframework.config.java.context;

import static java.lang.String.format;
import static org.springframework.config.java.core.ScopedProxyMethodProcessor.resolveHiddenScopedProxyBeanName;
import static org.springframework.config.java.util.DefaultScopes.SINGLETON;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.hasLength;

import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.model.ConfigurationModelAspectRegistry;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class CglibConfigurationEnhancer implements ConfigurationEnhancer {
	private static final Log log = LogFactory.getLog(CglibConfigurationEnhancer.class);
	private final BeanFactory beanFactory;

	public CglibConfigurationEnhancer(BeanFactory beanFactory) {
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

		if(log.isDebugEnabled())
			log.debug(format("Successfully enhanced %s; enhanced class name is: %s",
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

	static class ExternalBeanMethodInterceptor implements MethodInterceptor {
		private final BeanFactory beanFactory;

		public ExternalBeanMethodInterceptor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
			final String name;

			ExternalBean extBean = AnnotationUtils.findAnnotation(m, ExternalBean.class);
			Assert.notNull(extBean, "ExternalBean methods must be annotated with @ExternalBean");

			String alternateName = extBean.value();
			if(StringUtils.hasLength(alternateName))
				name = alternateName;
			else
				name = m.getName();

			return beanFactory.getBean(name);
		}
	}

	static class ExternalValueMethodInterceptor implements MethodInterceptor {
		private final BeanFactory beanFactory;
		private ValueSource valueSource;

		public ExternalValueMethodInterceptor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
			ExternalValue metadata = AnnotationUtils.findAnnotation(m, ExternalValue.class);
			Assert.notNull(metadata, "ExternalValue methods must be annotated with @ExternalValue");

    		String name = metadata.value();
    		if (!hasLength(name)) {
    			// no explicit name provided -> use method name
    			// TODO: plug in naming strategy
    			name = m.getName();
    			// Strip property name if needed
    			if (name.startsWith("get"))
    				name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
    		}

			Class<?> requiredType = m.getReturnType();

			if(valueSource == null)
				initializeValueSource(m);

			return valueSource.resolve(name, requiredType);
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
    			String methodName = m.getName();
    			throw new IllegalStateException(format("No ValueSource bean could be found in " +
    					"beanFactory while trying to resolve @ExternalValue method %s.%s. " +
    					"Perhaps no @ResourceBundles annotation was provided on %s?",
    					className, methodName, className));
    		}
		}
	}

	static class AutoBeanMethodInterceptor implements MethodInterceptor {
		private final BeanFactory beanFactory;

		public AutoBeanMethodInterceptor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
			final String name;

			AutoBean metadata = AnnotationUtils.findAnnotation(m, AutoBean.class);
			Assert.notNull(metadata, "AutoBean methods must be annotated with @AutoBean");

			name = m.getName();

			return beanFactory.getBean(name);
		}
	}

	/**
	 * Intercepts calls to {@link Bean @Bean} methods delegating to
	 * {@link #intercept(Object, Method, Object[], MethodProxy) intercept()} in order
	 * to ensure proper bean functionality: singleton, AOP proxying, etc.
	 *
	 * @author Chris Beams
	 */
	static class BeanMethodInterceptor implements MethodInterceptor {
		private final BeanFactory beanFactory;
		private final Log log = LogFactory.getLog(BeanMethodInterceptor.class);

		public BeanMethodInterceptor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		/**
		 * Enhances a {@link Bean @Bean} method to check the supplied BeanFactory for the existence
		 * of this bean object
		 */
		public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
			// TODO: casting here is atrocious... need to see about a) actually passing in a CAC, or b) providing
			// conditional logic to test for whether 'beanFactory' is a CAC or a CBF and then acting as appropriate.
			// should create a test that causes both types of objects to get injected to prove that ClassCastExceptions
			// don't occur

			// by default the bean will be named according to the name of the method
			// TODO: incorporate BeanNamingStrategy here
			String beanName = m.getName();

			boolean isScopedProxy = (AnnotationUtils.findAnnotation(m, ScopedProxy.class) != null);
			String scopedBeanName = resolveHiddenScopedProxyBeanName(beanName);
			if(isScopedProxy && ((ConfigurableApplicationContext)beanFactory).getBeanFactory().isCurrentlyInCreation(scopedBeanName))
				beanName = scopedBeanName;

			// the target bean instance, whether retrieved as a cached singleton or created newly below
			Object bean;

			if(factoryContainsBean(beanName)) {
				// we have an already existing cached instance of this bean -> retrieve it
				bean = beanFactory.getBean(beanName);
				if(log.isDebugEnabled())
					log.debug(format("Returning cached singleton object [%s] for @Bean method %s.%s",
							bean, m.getDeclaringClass().getSimpleName(), m.getName()));
			} else {
				// no instance exists yet -> create a new one
				bean = mp.invokeSuper(o, args);


				if(log.isDebugEnabled())
					log.debug(format("Wrapping singleton object [%s] for @Bean method %s.%s in AOP proxy",
							bean, m.getDeclaringClass().getSimpleName(), m.getName()));
				bean = proxyIfAnyPointcutsApply(bean, m.getReturnType());

				// TODO: replace with static call to BeanMethod
				Bean metadata = AnnotationUtils.findAnnotation(m, Bean.class);
				if(metadata.scope().equals(SINGLETON)) {
					if(log.isDebugEnabled())
						log.debug(format("Registering new singleton object [%s] for @Bean method %s.%s",
							bean, m.getDeclaringClass().getSimpleName(), m.getName()));
					((ConfigurableBeanFactory) ((ConfigurableApplicationContext)beanFactory).getBeanFactory()).registerSingleton(beanName, bean);
				}
			}

			return bean;
		}

		private Object proxyIfAnyPointcutsApply(Object bean, Class<?> returnType) {
			ConfigurationModelAspectRegistry cmap = (ConfigurationModelAspectRegistry) beanFactory.getBean(ConfigurationModelAspectRegistry.class.getName());
			Map<String, Pointcut> pointcuts = cmap.pointcuts;
			Map<String, Advice> advices = cmap.advices;

			ProxyFactory pf = new ProxyFactory(bean);

			for(String adviceName : pointcuts.keySet()) {
				Pointcut pc = pointcuts.get(adviceName);
				if(!AopUtils.canApply(pc, bean.getClass()))
					continue;

				Advice advice = advices.get(adviceName);
				DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pc, advice);

				// TODO: [aop] respect Ordering

				pf.addAdvisor(advisor);
			}

			if(pf.getAdvisors().length == 0)
				// no pointcuts apply -> return the unadorned target object
				return bean;

			pf.addAdvice(0, ExposeInvocationInterceptor.INSTANCE);

			if(returnType.isInterface()) {
    			pf.setInterfaces(new Class[] { returnType });
    			pf.setProxyTargetClass(false);
			} else {
				pf.setProxyTargetClass(true);
			}

			return pf.getProxy();
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
			// TODO: stopgap check; see todos above, this type wrangling needs to get worked out better
			Assert.isInstanceOf(ConfigurableApplicationContext.class, beanFactory,
					"beanFactory must be of type ConfigurableApplicationContext. Actual type: " +
					beanFactory.getClass().getSimpleName());

			return beanFactory.containsBean(beanName)
				&& !(((ConfigurableApplicationContext)beanFactory).getBeanFactory()).isCurrentlyInCreation(beanName);
		}
	}

}
