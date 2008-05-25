package org.springframework.config.java.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Required;

/**
 * Aspect that weaves tracking logic into methods annotated with Spring's
 * {@link Required @Required} annotation. Requires AspectJ bytecode (load-time
 * or build-time) weaving to function.
 * 
 * @see org.springframework.config.java.annotation.Configuration#checkRequired()
 * @see META-INF/aop.xml
 * 
 * @author Chris Beams
 */
@Aspect
public class RequiredMethodInvocationTracker {

	private static final RequiredMethodInvocationRegistry invocationRegistry = new RequiredMethodInvocationRegistry();

	@Before("execution(@org.springframework.beans.factory.annotation.Required * *(..))")
	public void logRequiredMethodInvocation(JoinPoint jp) {
		Signature signature = jp.getSignature();
		getInvocationRegistry().registerMethodInvocation(jp.getTarget(), signature.getDeclaringTypeName(),
				signature.getName());
	}

	public static RequiredMethodInvocationRegistry getInvocationRegistry() {
		return invocationRegistry;
	}

}
