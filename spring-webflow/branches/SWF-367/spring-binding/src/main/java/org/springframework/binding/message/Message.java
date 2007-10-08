package org.springframework.binding.message;

import java.io.Serializable;

public class Message implements Serializable {

	private Object source;

	private String text;

	private Severity severity;

	public Message(Object source, String text, Severity severity) {
		super();
		this.source = source;
		this.text = text;
		this.severity = severity;
	}

	public Object getSource() {
		return source;
	}

	public String getText() {
		return text;
	}

	public Severity getSeverity() {
		return severity;
	}

}
