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
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

public class ConfigurationModelAspectProcessor {
	public static Map<String, Pointcut> pointcuts;
	public static Map<String, Advice> advices;
	public static final Log logger = LogFactory.getLog(ConfigurationModelAspectProcessor.class);

	public static void processAnyAspects(ConfigurationModel model, final BeanFactory beanFactory) {
		logger.info("Processing " + model + " for any aspects");
		pointcuts = new HashMap<String, Pointcut>();
		advices = new HashMap<String, Advice>();

		for(AspectClass aspectClass : model.getAspectClasses()) {
			final Class<?> literalClass;
			try {
				literalClass = Class.forName(aspectClass.getName());
			}
			catch (ClassNotFoundException ex) { throw new RuntimeException(ex); }

			final AspectJAdvisorFactory advisorFactory = new ReflectiveAspectJAdvisorFactory();

    		ReflectionUtils.doWithMethods(
    			// for each method in this class
    			literalClass,
    			// execute this callback
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
        		// but exclude all Object.* methods
        		new MethodFilter() {
        			public boolean matches(Method method) {
        				if(method.getDeclaringClass().equals(Object.class))
        					return false;
        				return true;
        			}
        		}
        	);
		}
		/*

		final AspectJAdvisorFactory advisorFactory = new ReflectiveAspectJAdvisorFactory();
		// note: when porting to an ASM-based implementation, using this reflective aj
		// factory is going to pose a problem.  There is no ASM-based impl, and even the
		// interface expects class literals.  Should get in touch w/ Adrian et al on this
		final boolean isAtAspectClass = advisorFactory.isAspect(literalClass);
		if(isAtAspectClass)
			advisorFactory.validate(literalClass); // TODO: catch exceptions

		ReflectionUtils.doWithMethods(
			// for each method in this class
			literalClass,
			// execute this callback
			new MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        			if(isAtAspectClass) {
        				// examine this method to see if it's an advice method
        				String aspectName = "aspectNamePlaceholder";
        				MetadataAwareAspectInstanceFactory aif = new SimpleMetadataAwareAspectInstanceFactory(literalClass, aspectName);
        				Advisor pa = advisorFactory.getAdvisor(method, aif, 0, aspectName);
                		if (pa != null && (pa instanceof PointcutAdvisor)) {
                			String adviceName = method.getName();
                			Advice advice = pa.getAdvice();
                			// advice may return null in the case of named pointcuts (@Pointcut)
                			if (advice != null) {
                				Pointcut pointcut = ((PointcutAdvisor) pa).getPointcut();
                				PointcutsAndAspectsHolder.pointcuts.put(adviceName, pointcut);
                				PointcutsAndAspectsHolder.advice.put(adviceName, advice);
                			}
                		}
        			}
    			}
    		},
    		// but exclude all Object.* methods
    		new MethodFilter() {
    			public boolean matches(Method method) {
    				if(method.getDeclaringClass().equals(Object.class))
    					return false;
    				return true;
    			}
    		}
    	);

		 */
	}

}
