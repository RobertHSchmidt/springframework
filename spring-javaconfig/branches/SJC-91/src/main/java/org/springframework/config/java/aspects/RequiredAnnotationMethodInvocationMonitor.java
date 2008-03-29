package org.springframework.config.java.aspects;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * Not thread-safe!
 * 
 * @author Chris Beams
 */
@Aspect
public class RequiredAnnotationMethodInvocationMonitor {
	private static final Set<String> methodsInvoked = new HashSet<String>();

	private static boolean instantiatedByLoadTimeWeaver = false;

	public RequiredAnnotationMethodInvocationMonitor() {
		if (instantiatedByLoadTimeWeaver)
			throw new IllegalStateException("should be instantiated once and only once by aspectj weaver");

		System.out.println("what thread is calling me? " + Thread.currentThread().getName());

		instantiatedByLoadTimeWeaver = true;
	}

	@Before("execution(@org.springframework.beans.factory.annotation.Required * *(..))")
	public void logRequiredMethodInvocation(JoinPoint jp) {
		System.out.println("logging method invocation: " + jp);
		String methodName = fqMethodName(jp.getSignature());
		methodsInvoked.add(methodName);
	}

	private static String fqMethodName(Signature signature) {
		return fqMethodName(signature.getDeclaringTypeName(), signature.getName());
	}

	private static String fqMethodName(Method method) {
		return fqMethodName(method.getDeclaringClass().getName(), method.getName());
	}

	private static String fqMethodName(String className, String methodName) {
		return className + "." + methodName;
	}

	private static boolean hasMethodBeenInvoked(Method requiredMethod) {
		return methodsInvoked.contains(fqMethodName(requiredMethod));
	}

	public static void interrogateRequiredMethods(Object bean) {
		System.out.println("interrogating " + bean);

		ReflectionUtils.doWithMethods(bean.getClass(),

		new MethodCallback() {
			public void doWith(Method requiredMethod) throws IllegalArgumentException, IllegalAccessException {
				if (!hasMethodBeenInvoked(requiredMethod))
					throw new RuntimeException(format("required method %s has not been set!", requiredMethod.getName()));
			}
		},

		new ReflectionUtils.MethodFilter() {
			public boolean matches(Method candidate) {
				return !candidate.getDeclaringClass().equals(Object.class)
						&& (AnnotationUtils.findAnnotation(candidate, Required.class) != null);
			}
		});
	}

	public static class PostProcessor implements BeanPostProcessor, ApplicationContextAware, ApplicationListener {
		private boolean doInterrogate = false;

		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			if (doInterrogate) {
				if (!instantiatedByLoadTimeWeaver)
					throw new IllegalStateException("load time weaving has not been enabled!");
				interrogateRequiredMethods(bean);
			}

			return bean;
		}

		public void setInterrogateRequiredMethods(boolean b) {
			this.doInterrogate = b;
		}

		public void onApplicationEvent(ApplicationEvent event) {
			methodsInvoked.clear();
		}

		public void setApplicationContext(ApplicationContext ctx) throws BeansException {
			((ConfigurableApplicationContext) ctx).addApplicationListener(this);
		}
	}

}
