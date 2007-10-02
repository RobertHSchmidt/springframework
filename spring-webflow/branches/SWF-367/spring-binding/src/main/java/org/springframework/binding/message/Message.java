package org.springframework.binding.message;

public class Message {

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
