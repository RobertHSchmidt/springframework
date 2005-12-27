package org.springframework.webflow.util;

import java.io.Serializable;


/**
 * A key generator that uses the RandomGuid support class. The default
 * implementation used by the webflow system.
 * 
 * @author Keith Donald
 */
public class RandomGuidKeyGenerator implements KeyGenerator, Serializable {

	/**
	 * Should the random GUID generated be secure?
	 */
	private boolean secure;

	/**
	 * Returns the secure flag.
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * Sets the secure flag.
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public Serializable generate() {
		return new RandomGuid(secure).toString();
	}
}