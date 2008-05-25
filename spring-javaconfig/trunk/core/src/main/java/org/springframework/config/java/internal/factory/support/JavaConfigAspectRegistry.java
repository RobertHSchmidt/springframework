package org.springframework.config.java.internal.factory.support;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.BeanFactoryAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.HotSwappable;
import org.springframework.config.java.internal.factory.JavaConfigBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * @author Chris Beams
 */
public class JavaConfigAspectRegistry {

	private static final String BEAN_NAME = JavaConfigAspectRegistry.class.getName();

	private static final Log logger = LogFactory.getLog(JavaConfigAspectRegistry.class);

	private final Map<String, Pointcut> pointcuts = new HashMap<String, Pointcut>();

	private final Map<String, Advice> advices = new HashMap<String, Advice>();

	private final AspectJAdvisorFactory advisorFactory = new ReflectiveAspectJAdvisorFactory();

	private final JavaConfigBeanFactory beanFactory;

	public JavaConfigAspectRegistry(JavaConfigBeanFactory beanFactory) {
		registerSelfWithBeanFactory(beanFactory);
		this.beanFactory = beanFactory;
	}

	private void registerSelfWithBeanFactory(JavaConfigBeanFactory beanFactory) {
		String aspectRegistryBeanName = BEAN_NAME;
		if(beanFactory.containsSingleton(aspectRegistryBeanName))
			throw new IllegalStateException("aspect registry has already been registered with bean factory");
		beanFactory.registerSingleton(aspectRegistryBeanName, this);
	}

	public Object proxyIfAnyPointcutsApply(Object bean, Method method, Bean metadata) {
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

		boolean isHotSwappable = method.getAnnotation(HotSwappable.class) != null;

		// if no pointcuts apply and the bean is not hot-swappable
		// then return the original object, unadorned
		if(pf.getAdvisors().length == 0 && !isHotSwappable)
			return bean;

		// See JavaDoc on ExposeInvocationInterceptor for details
		pf.addAdvice(0, ExposeInvocationInterceptor.INSTANCE);

		determineProxyStrategy(pf, method, metadata);

		if(isHotSwappable) {
			HotSwappableTargetSource hotSwapTS = new HotSwappableTargetSource(bean);
			pf.setTargetSource(hotSwapTS);
		}

		if(logger.isInfoEnabled())
			logger.info(format("Wrapping object [%s] for @Bean method %s.%s in AOP proxy",
					bean, method.getDeclaringClass().getSimpleName(), method.getName()));
		return pf.getProxy();
	}

	/**
	 * Configure <var>pf</var> to use class proxies only if the return type isn't an interface or if
	 * autowire is required (as an interface based proxy excludes the setters).
	 */
	private void determineProxyStrategy(ProxyFactory pf, Method method, Bean metadata) {
		Class<?> returnType = method.getReturnType();
		boolean beanIsAutowired = metadata.autowire().isAutowire();

		if(returnType.isInterface() && !beanIsAutowired) {
			pf.setInterfaces(new Class[] { returnType });
			pf.setProxyTargetClass(false);
		} else {
			pf.setProxyTargetClass(true);
		}
	}

	public void registerAspect(final Class<?> atAspectClass) {
		advisorFactory.validate(atAspectClass);

		ReflectionUtils.doWithMethods(atAspectClass,
			new MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
    				registerPointcutAndAdviceIfMethodIsPointcutAdvisor(atAspectClass, method);
    			}
    		},
    		// exclude all Object.* methods
    		new MethodFilter() {
    			public boolean matches(Method method) {
    				if(method.getDeclaringClass().equals(Object.class))
    					return false;
    				return true;
    			}
    		}
    	);
	}

	private void registerPointcutAndAdviceIfMethodIsPointcutAdvisor(final Class<?> atAspectClass, Method method) {
		String aspectName = getAspectName(atAspectClass);
		MetadataAwareAspectInstanceFactory aif = new BeanFactoryAspectInstanceFactory(beanFactory, aspectName, atAspectClass);
		Advisor pa = advisorFactory.getAdvisor(method, aif, 0, aspectName);
		if (pa != null && (pa instanceof PointcutAdvisor)) {
			String adviceName = method.getName();
			Advice advice = pa.getAdvice();
			// advice may return null in the case of named pointcuts (@Pointcut)
			if (advice != null) {
				Pointcut pointcut = ((PointcutAdvisor) pa).getPointcut();
				pointcuts.put(adviceName, pointcut);
				advices.put(adviceName, advice);
			}
		}
	}

	private String getAspectName(Class<?> atAspectClass) {
		return atAspectClass.getName();
	}

	public static JavaConfigAspectRegistry retrieveFrom(JavaConfigBeanFactory beanFactory) {
		String aspectRegistryBeanName = BEAN_NAME;

		if(!beanFactory.containsBean(aspectRegistryBeanName))
			throw new IllegalStateException("aspect registry bean is not present as expected in bean factory");

		return (JavaConfigAspectRegistry) beanFactory.getBean(aspectRegistryBeanName);
	}

}
