package org.springframework.binding.message;

import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * A factory for messages. Allows messages to be internationalized and decorated with attributes.
 * 
 * @author Keith Donald
 */
public interface MessageResolver {

	/**
	 * Resolve the message from the source using the current locale.
	 * @param messageSource the message source, an abstraction for a resource bundle
	 * @param locale the current locale of this request
	 * @return the resolved messaged
	 */
	public Message resolveMessage(MessageSource messageSource, Locale locale);
}
