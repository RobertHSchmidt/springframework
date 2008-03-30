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
import org.springframework.beans.factory.BeanInitializationException;
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
 * @author Chris Beams
 */
@Aspect
public class RequiredAnnotationMethodInvocationMonitor {

	public static class InvokedRequiredMethods extends ThreadLocal<Set<String>> {

		@Override
		protected Set<String> initialValue() {
			System.out.println("THREAD calling initialValue(): " + Thread.currentThread().getName());
			return new HashSet<String>();
		}

		public void registerMethodInvocation(Object bean, String className, String methodName) {
			get().add(qualifyMethodName(bean, className, methodName));
		}

		public void interrogateRequiredMethods(final Object bean, final String beanName) {
			System.out.println("interrogating " + bean);

			ReflectionUtils.doWithMethods(bean.getClass(),

			new MethodCallback() {
				public void doWith(Method requiredMethod) throws IllegalArgumentException, IllegalAccessException {
					if (!invokedRequiredMethods.hasMethodBeenInvoked(bean, requiredMethod))
						throw new BeanInitializationException(format("Method '%s' is required for bean '%s'",
								requiredMethod.getName(), beanName));
				}
			},

			new ReflectionUtils.MethodFilter() {
				public boolean matches(Method candidate) {
					return !candidate.getDeclaringClass().equals(Object.class)
							&& (AnnotationUtils.findAnnotation(candidate, Required.class) != null);
				}
			});
		}

		public void clear() {
			get().clear();
		}

		private boolean hasMethodBeenInvoked(Object bean, Method requiredMethod) {
			return get().contains(
					qualifyMethodName(bean, requiredMethod.getDeclaringClass().getName(), requiredMethod.getName()));
		}

		private String qualifyMethodName(Object bean, String className, String methodName) {
			return System.identityHashCode(bean) + ":" + className + "." + methodName;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}

	}

	private static final InvokedRequiredMethods invokedRequiredMethods = new InvokedRequiredMethods();

	@Before("execution(@org.springframework.beans.factory.annotation.Required * *(..))")
	public void logRequiredMethodInvocation(JoinPoint jp) {
		System.out.println("logging method invocation: " + jp);
		Signature signature = jp.getSignature();
		invokedRequiredMethods.registerMethodInvocation(jp.getTarget(), signature.getDeclaringTypeName(), signature
				.getName());
	}

	public static class PostProcessor implements BeanPostProcessor, ApplicationContextAware, ApplicationListener {
		private boolean doInterrogate = false;

		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			if (doInterrogate)
				invokedRequiredMethods.interrogateRequiredMethods(bean, beanName);

			return bean;
		}

		public void setInterrogateRequiredMethods(boolean b) {
			this.doInterrogate = b;
		}

		public void onApplicationEvent(ApplicationEvent event) {
			invokedRequiredMethods.clear();
		}

		public void setApplicationContext(ApplicationContext ctx) throws BeansException {
			((ConfigurableApplicationContext) ctx).addApplicationListener(this);
		}
	}

}
