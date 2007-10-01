package org.springframework.binding.message;

import java.util.Locale;

import org.springframework.context.MessageSource;

public interface MessageResolver {
	public Message resolveMessage(MessageSource source, Locale locale);
}
