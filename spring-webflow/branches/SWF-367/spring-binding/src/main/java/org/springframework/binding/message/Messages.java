package org.springframework.binding.message;

import java.util.Locale;

import org.springframework.context.MessageSource;

public class Messages implements MessageResolver {

	private String code;

	private Severity severity;

	private Object[] args;

	private String defaultText;

	private Messages(String code, Severity severity, Object[] args, String defaultText) {
		this.code = code;
		this.severity = severity;
		this.args = args;
		this.defaultText = defaultText;
	}

	public Message resolveMessage(MessageSource source, Locale locale) {
		if (source != null && (code != null && code.length() > 0)) {
			return new Message(getMessageText(source, locale), severity);
		} else {
			return new Message(defaultText, severity);
		}
	}

	public static Messages text(String text) {
		return new Messages(null, Severity.INFO, null, text);
	}

	public static Messages text(String text, Severity severity) {
		return new Messages(null, severity, null, text);
	}

	public static Messages info(String code) {
		return new Messages(code, Severity.INFO, null, null);
	}

	public static Messages warning(String code) {
		return new Messages(code, Severity.WARNING, null, null);
	}

	public static Messages error(String code) {
		return new Messages(code, Severity.ERROR, null, null);
	}

	public static Messages info(String code, Object[] args) {
		return new Messages(code, Severity.INFO, args, null);
	}

	public static Messages warning(String code, Object[] args) {
		return new Messages(code, Severity.WARNING, args, null);
	}

	public static Messages error(String code, Object[] args) {
		return new Messages(code, Severity.ERROR, args, null);
	}

	private String getMessageText(MessageSource source, Locale locale) {
		if (defaultText == null) {
			return source.getMessage(code, args, locale);
		} else {
			return source.getMessage(code, args, defaultText, locale);
		}
	}

}
