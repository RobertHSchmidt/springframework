/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.registry;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.FlowAssembler;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.ResourceHolder;

/**
 * A flow definition holder that is refreshable.
 * 
 * @author Keith Donald
 */
public class RefreshingFlowHolder implements FlowHolder {

	/**
	 * The flow definition assembled by this assembler.
	 */
	private Flow flow;

	/**
	 * A last modified date for the backing flow resource, used to support
	 * automatic reassembly on resource change.
	 */
	private long lastModified;

	/**
	 * A flag indicating whether or not this assembler is in the middle of the
	 * assembly process.
	 */
	private boolean assembling;

	/**
	 * The flow assembler.
	 */
	private FlowAssembler assembler;

	public RefreshingFlowHolder(FlowAssembler assembler) {
		this.assembler = assembler;
	}

	public String getId() {
		return assembler.getFlowId();
	}

	public Flow getFlow() {
		if (assembling) {
			// must return early assembly result
			return getFlowBuilder().getResult();
		}
		if (!isAssembled()) {
			assembleFlow();
		}
		else {
			refreshIfChanged();
		}
		return flow;
	}

	/**
	 * Returns the flow builder that actually builds the Flow definition.
	 */
	public FlowBuilder getFlowBuilder() {
		return assembler.getFlowBuilder();
	}

	/**
	 * Returns a flag indicating if this assembler has performed and completed
	 * Flow assembly.
	 */
	protected boolean isAssembled() {
		return flow != null;
	}

	/**
	 * Assemble the held flow definition, delegating to the configured
	 * FlowAssembler (director).
	 */
	protected void assembleFlow() {
		try {
			assembling = true;
			assembler.assembleFlow();
			flow = getFlowBuilder().getResult();
		}
		finally {
			assembling = false;
		}
	}

	public void refresh() {
		assembler.assembleFlow();
	}

	/**
	 * Reassemble the flow if its underlying resource has changed.
	 */
	protected void refreshIfChanged() {
		if (this.lastModified == -1) {
			// just ignore, tracking last modified date not supported
			return;
		}
		if (this.lastModified < getLastModified()) {
			refresh();
		}
	}

	/**
	 * Helper that retrieves the last modified date by querying the backing flow
	 * resource.
	 * @return the last modified date, or -1 if it could not be retrieved
	 */
	protected long getLastModified() {
		if (getFlowBuilder() instanceof ResourceHolder) {
			Resource resource = ((ResourceHolder)getFlowBuilder()).getResource();
			try {
				return resource.getFile().lastModified();
			}
			catch (IOException e) {
				// ignore, last modified checks not supported
			}
		}
		return -1;
	}
}