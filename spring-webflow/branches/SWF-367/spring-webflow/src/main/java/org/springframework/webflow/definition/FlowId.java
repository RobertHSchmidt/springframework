package org.springframework.webflow.definition;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * The unique, fully qualified identifier of a flow definition. Consists of a namespace id plus a short name.
 * <p>
 * Example flow ids:
 * <ul>
 * <li>/booking/main - the 'main' flow in the 'booking' namespace
 * <li>/booking/chooseSeat - the 'chooseSeat' flow in the 'booking' namespace
 * <li>/user/list - the 'list' flow in the user namespace
 * <li>/user/create - the 'create' flow in the user namespace
 * <li>main - the 'main' flow in the default namespace.
 * </ul>
 * </p>
 * @author Keith Donald
 */
public class FlowId implements Serializable {

	private String namespace;

	private String shortName;

	/**
	 * Creates a new flow identifier.
	 * @param namespace the namespace
	 * @param shortName the short name
	 */
	public FlowId(String namespace, String shortName) {
		Assert.notNull(namespace, "The namespace is required");
		Assert.hasText(shortName, "The short name must have text");
		this.namespace = namespace;
		this.shortName = shortName;
	}

	/**
	 * Returns the namespace.
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the short name.
	 * @return the short name.
	 */
	public String getShortName() {
		return shortName;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowId)) {
			return false;
		}
		FlowId id = (FlowId) o;
		return namespace.equals(id.namespace) && shortName.equals(id.shortName);
	}

	public int hashCode() {
		return namespace.hashCode() + shortName.hashCode();
	}

	/**
	 * Factory method for creating a new FlowId from a string.
	 * <p>
	 * Example flow ids:
	 * <ul>
	 * <li>/booking/main - the 'main' flow in the 'booking' namespace
	 * <li>/booking/chooseSeat - the 'chooseSeat' flow in the 'booking' namespace
	 * <li>/user/list - the 'list' flow in the user namespace
	 * <li>/user/create - the 'create' flow in the user namespace
	 * <li>/global - the 'global' flow in the default namespace.
	 * <li>main - the 'main' flow in the default namespace.
	 * </ul>
	 * </p>
	 * @param id the string-encoded flow id
	 * @return the flow id
	 */
	public static FlowId valueOf(String id) {
		int firstSlash = id.indexOf('/');
		if (firstSlash == -1) {
			return new FlowId("", id);
		} else {
			int lastSlash = id.lastIndexOf("/");
			if (lastSlash == firstSlash) {
				return new FlowId("", id.substring(firstSlash + 1));
			} else {
				String namespace = id.substring(firstSlash + 1, lastSlash);
				return new FlowId(namespace, id.substring(lastSlash + 1));
			}
		}
	}

	public String toString() {
		if (namespace.length() == 0) {
			return shortName;
		} else {
			return namespace + "/" + shortName;
		}
	}
}