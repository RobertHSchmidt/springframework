package org.springframework.config.java.model;

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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * @author Chris Beams
 */
public class ConfigurationModelAspectRegistry {
	private static final Log logger = LogFactory.getLog(ConfigurationModelAspectRegistry.class);

	public Map<String, Pointcut> pointcuts = new HashMap<String, Pointcut>();

	public Map<String, Advice> advices = new HashMap<String, Advice>();

	/**
	 * Finds any aspects specified within <var>model</var> and registers associated pointcuts
	 * and advice such that they can be used during runtime processing within enhanced {@link Bean @Bean}
	 * methods.
	 *
	 * @param model
	 * @param beanFactory
	 */
	public void registerAspects(ConfigurationModel model, final BeanFactory beanFactory) {
		logger.info("Registering aspects from " + model);

		for(AspectClass aspectClass : model.getAspectClasses()) {
			final Class<?> literalClass;
			try {
				literalClass = Class.forName(aspectClass.getName());
			}
			catch (ClassNotFoundException ex) { throw new RuntimeException(ex); }

			final AspectJAdvisorFactory advisorFactory = new ReflectiveAspectJAdvisorFactory();

    		ReflectionUtils.doWithMethods(literalClass,
    			new MethodCallback() {
    				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        				// examine this method to see if it's an advice method
        				String aspectName = literalClass.getName();
        				advisorFactory.validate(literalClass);
        				MetadataAwareAspectInstanceFactory aif = new BeanFactoryAspectInstanceFactory(beanFactory, aspectName, literalClass);
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
	}

}
