/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.support;

import org.springframework.webflow.context.SharedAttributeMap;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * A proxy that rebinds a target flow execution repository to its shared map on
 * change. This is used to support notifying shared map's that their underlying
 * entries changed, for purposes of clustering for example.
 * 
 * @author Keith Donald
 */
class RebindingFlowExecutionRepository implements FlowExecutionRepository {

	private FlowExecutionRepository targetRepository;

	private String repositoryAttributeName;

	private SharedAttributeMap repositoryMap;

	public RebindingFlowExecutionRepository(FlowExecutionRepository targetRepository, String repositoryAttributeName,
			SharedAttributeMap repositoryMap) {
		this.targetRepository = targetRepository;
		this.repositoryAttributeName = repositoryAttributeName;
		this.repositoryMap = repositoryMap;
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution) throws FlowExecutionRepositoryException {
		return targetRepository.generateKey(flowExecution);
	}

	public FlowExecutionKey getNextKey(FlowExecution flowExecution, FlowExecutionKey key)
			throws FlowExecutionRepositoryException {
		return targetRepository.getNextKey(flowExecution, key);
	}

	public FlowExecutionLock getLock(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		return targetRepository.getLock(key);
	}

	public FlowExecution getFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		return targetRepository.getFlowExecution(key);
	}

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution)
			throws FlowExecutionRepositoryException {
		targetRepository.putFlowExecution(key, flowExecution);
		rebind();
	}

	public void removeFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		targetRepository.removeFlowExecution(key);
		rebind();
	}

	public FlowExecutionKey parseFlowExecutionKey(String encodedKey) {
		return targetRepository.parseFlowExecutionKey(encodedKey);
	}

	private void rebind() {
		synchronized (repositoryMap.getMutex()) {
			repositoryMap.put(repositoryAttributeName, targetRepository);
		}
	}
}