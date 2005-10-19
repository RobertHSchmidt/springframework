package org.springframework.webflow.execution;

import java.io.Serializable;

import org.springframework.webflow.util.RandomGuid;

/**
 * @author Keith Donald
 */
public class RandomGuidKeyGenerator implements KeyGenerator {

	/**
	 * 
	 */
	private boolean secure = true;

	/**
	 * @return
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * @param secure
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public Serializable generate() {
		return new RandomGuid(secure).toString();
	}
}