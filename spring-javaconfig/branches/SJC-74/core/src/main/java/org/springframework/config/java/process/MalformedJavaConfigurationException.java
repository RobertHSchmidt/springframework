package org.springframework.config.java.process;

@SuppressWarnings("serial")
public class MalformedJavaConfigurationException extends RuntimeException {

	public MalformedJavaConfigurationException(String message) {
		super(message);
	}

}
