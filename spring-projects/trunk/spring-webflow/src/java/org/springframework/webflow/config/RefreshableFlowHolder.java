package org.springframework.webflow.config;

/**
 * A holder managing a refreshable reference to a Flow definition. The
 * definition may be refreshed by calling the <code>refresh</code> operation.
 * @author Keith Donald
 */
public interface RefreshableFlowHolder extends FlowHolder {

	/**
	 * Refresh the Flow definition held by this holder.
	 */
	public void refresh();
}