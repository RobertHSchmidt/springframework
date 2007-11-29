package org.springframework.beans.factory;

import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * Exception thrown when a BeanFactory is asked for a bean by type for which
 * there exist more than one match.
 * 
 * <p/>Note: currently used only by {@link JavaConfigApplicationContext} - may
 * potentially be promoted and used more widely in the future
 * 
 * XXX: Review with Juergen
 * 
 * @see JavaConfigApplicationContext#getBean(Class)
 * @author Chris Beams
 */
@SuppressWarnings("serial")
public class AmbiguousBeanLookupException extends BeansException {

	public AmbiguousBeanLookupException(Class<?> type, Map<String, Object> beansOfType) {
		super(formatMessage(type, beansOfType));
	}

	public AmbiguousBeanLookupException(String message) {
		super(message);
	}

	private static String formatMessage(Class<?> type, Map<String, Object> beansOfType) {
		return String.format("%d beans match requested type [%s] - "
				+ "consider using getBean(T, String) to disambiguate. " //
				+ "Matching bean names are: [%s]", //
				beansOfType.size(), type, collectionToCommaDelimitedString(beansOfType.keySet()));
	}

}