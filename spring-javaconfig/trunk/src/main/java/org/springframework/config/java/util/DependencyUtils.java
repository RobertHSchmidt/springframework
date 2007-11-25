package org.springframework.config.java.util;

/**
 * Mechanisms commonly used when ensuring minimal external dependencies
 * throughout the codebase.
 * 
 * @author cbeams
 */
public final class DependencyUtils {

	private DependencyUtils() {
	}

	/**
	 * Inspects the classpath to determine whether key AOP resources are
	 * present.
	 */
	public static boolean isAopAvailable() {
		try {
			Class.forName("org.springframework.aop.TargetSource");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
}
