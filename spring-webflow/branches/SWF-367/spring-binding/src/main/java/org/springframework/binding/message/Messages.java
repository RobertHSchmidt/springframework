package org.springframework.binding.message;

import java.util.Locale;

import org.springframework.context.MessageSource;

public class Messages implements MessageResolver {

	private Object source;

	private String code;

	private Severity severity;

	private Object[] args;

	private String defaultText;

	private Messages(Object source, String code, Severity severity, Object[] args, String defaultText) {
		this.source = source;
		this.code = code;
		this.severity = severity;
		this.args = args;
		this.defaultText = defaultText;
	}

	public Message resolveMessage(MessageSource messageSource, Locale locale) {
		if (messageSource != null && (code != null && code.length() > 0)) {
			return new Message(source, getMessageText(messageSource, locale), severity);
		} else {
			return new Message(source, defaultText, severity);
		}
	}

	public static Messages text(String text) {
		return new Messages(null, null, Severity.INFO, null, text);
	}

	public static Messages text(String text, Severity severity) {
		return new Messages(null, null, severity, null, text);
	}

	public static Messages info(String code) {
		return new Messages(null, code, Severity.INFO, null, null);
	}

	public static Messages warning(String code) {
		return new Messages(null, code, Severity.WARNING, null, null);
	}

	public static Messages error(String code) {
		return new Messages(null, code, Severity.ERROR, null, null);
	}

	public static Messages info(String code, Object[] args) {
		return new Messages(null, code, Severity.INFO, args, null);
	}

	public static Messages warning(String code, Object[] args) {
		return new Messages(null, code, Severity.WARNING, args, null);
	}

	public static Messages error(String code, Object[] args) {
		return new Messages(null, code, Severity.ERROR, args, null);
	}

	public static Messages text(Object source, String text) {
		return new Messages(source, null, Severity.INFO, null, text);
	}

	public static Messages text(Object source, String text, Severity severity) {
		return new Messages(source, null, severity, null, text);
	}

	public static Messages info(Object source, String code) {
		return new Messages(source, code, Severity.INFO, null, null);
	}

	public static Messages warning(Object source, String code) {
		return new Messages(source, code, Severity.WARNING, null, null);
	}

	public static Messages error(Object source, String code) {
		return new Messages(source, code, Severity.ERROR, null, null);
	}

	public static Messages info(Object source, String code, Object[] args) {
		return new Messages(source, code, Severity.INFO, args, null);
	}

	public static Messages warning(Object source, String code, Object[] args) {
		return new Messages(source, code, Severity.WARNING, args, null);
	}

	public static Messages error(Object source, String code, Object[] args) {
		return new Messages(source, code, Severity.ERROR, args, null);
	}

	private String getMessageText(MessageSource source, Locale locale) {
		if (defaultText == null) {
			return source.getMessage(code, args, locale);
		} else {
			return source.getMessage(code, args, defaultText, locale);
		}
	}

}
