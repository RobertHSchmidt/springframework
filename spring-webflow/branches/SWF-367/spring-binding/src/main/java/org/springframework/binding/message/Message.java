package org.springframework.binding.message;

public class Message {

	private String text;

	private Severity severity;

	public Message(String text, Severity severity) {
		super();
		this.text = text;
		this.severity = severity;
	}

	public String getText() {
		return text;
	}

	public Severity getSeverity() {
		return severity;
	}

}
