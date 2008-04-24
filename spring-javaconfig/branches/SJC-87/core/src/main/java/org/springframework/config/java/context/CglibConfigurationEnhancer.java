package org.springframework.config.java.context;

import static java.lang.String.format;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.Assert.notNull;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
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
				return 0;
			}
		});
		enhancer.setCallbackTypes(new Class<?>[] { NoOp.class, BeanMethodInterceptor.class, ExternalBeanMethodInterceptor.class });

		Class<?> enhancedSubclass = enhancer.createClass();

		Enhancer.registerCallbacks(enhancedSubclass, new Callback[] { NoOp.INSTANCE, new BeanMethodInterceptor(beanFactory), new ExternalBeanMethodInterceptor(beanFactory) });

		if(log.isDebugEnabled())
			log.debug(format("Successfully enhanced %s; enhanced class name is: %s",
							  configClassName, enhancedSubclass.getName()));

		return enhancedSubclass.getName();
	}

	// TODO: should probably be in a util class
	private Class<?> loadClassFromName(String configClassName) {
		// TODO: handle exception more gracefully
		try {
			return Class.forName(configClassName);
		}
		catch (ClassNotFoundException ex) { throw new RuntimeException(ex); }
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

			// the target bean instance, whether retrieved as a cached singleton or created newly below
			final Object bean;

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
					log.debug(format("Registering new cached singleton object [%s] for @Bean method %s.%s",
							bean, m.getDeclaringClass().getSimpleName(), m.getName()));
				((ConfigurableBeanFactory) ((ConfigurableApplicationContext)beanFactory).getBeanFactory()).registerSingleton(beanName, bean);
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
			// TODO: stopgap check; see todos above, this type wrangling needs to get worked out better
			Assert.isInstanceOf(ConfigurableApplicationContext.class, beanFactory,
					"beanFactory must be of type ConfigurableApplicationContext. Actual type: " +
					beanFactory.getClass().getSimpleName());

			return beanFactory.containsBean(beanName)
				&& !(((ConfigurableApplicationContext)beanFactory).getBeanFactory()).isCurrentlyInCreation(beanName);
		}
	}

}
