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
package org.springframework.webflow.execution;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.format.Formatter;
import org.springframework.webflow.ExternalContext;

/**
 * Flow execution storage implementation that stores flow executions
 * representing user conversations in a managed repository.
 * <p>
 * The type of repository that is created by this object is is configurable by
 * setting a custom {@link FlowExecutionRepositoryFactory repositoryFactory}.
 * <p>
 * The source of a managed repository is pluggable by setting a custom
 * {@link MapAccessor repositoryMapAccessor}.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionRepository
 * @see org.springframework.webflow.execution.MapAccessor
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class RepositoryFlowExecutionStorage implements FlowExecutionStorage {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The map accessor that returns a <code>java.util.Map</code> that allows
	 * this storage implementation to access a FlowExecutionRepository by a
	 * unique key.
	 * <p>
	 * The default is the {@link SessionMapAccessor}, which returns a map
	 * backed by the {@link ExternalContext#getSessionMap}.
	 */
	private MapAccessor repositoryMapAccessor = new SessionMapAccessor();

	/**
	 * The factory that will create FlowExecutionRepository instances as needed
	 * for storage in the repositoryMap.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory = new DefaultFlowExecutionRepositoryFactory();

	/**
	 * The formatter that will parse encoded _flowExecutionId strings into
	 * {@link FlowExecutionContinuationKey} objects.
	 */
	private Formatter keyFormatter = new FlowExecutionContinuationnKeyFormatter();

	public MapAccessor getRepositoryMapAccessor() {
		return repositoryMapAccessor;
	}

	public void setRepositoryMapAccessor(MapAccessor repositoryMapAccessor) {
		this.repositoryMapAccessor = repositoryMapAccessor;
	}

	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	public Formatter getKeyFormatter() {
		return keyFormatter;
	}

	public void setKeyFormatter(Formatter keyFormatter) {
		this.keyFormatter = keyFormatter;
	}

	public FlowExecution load(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		return getRepository(context).getFlowExecution(parseKey(id));
	}

	public Serializable save(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionContinuationKey key = generateContinuationKey(id, repository);
		repository.putFlowExecution(key, flowExecution);
		return keyFormatter.formatValue(key);
	}

	public void remove(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		getRepository(context).invalidateConversation(parseKey(id).getConversationId());
	}

	// helpers

	private synchronized FlowExecutionRepository getRepository(ExternalContext context) {
		FlowExecutionRepository repository = (FlowExecutionRepository)getRepositoryMap(context).get(getRepositoryKey());
		if (repository == null) {
			repository = repositoryFactory.createRepository();
			getRepositoryMap(context).put(getRepositoryKey(), repository);
		}
		return repository;
	}
	
	protected Object getRepositoryKey() {
		return FlowExecutionRepository.class.getName();
	}

	protected Map getRepositoryMap(ExternalContext context) {
		return repositoryMapAccessor.getMap(context);
	}

	private FlowExecutionContinuationKey generateContinuationKey(Serializable previousId,
			FlowExecutionRepository repository) {
		if (previousId == null) {
			// this is a save request for a new conversation: generate a new key
			// consisting of a new conversationId and continuationId
			return repository.generateContinuationKey();
		}
		else {
			// this is a save request for an existing managed conversation:
			// generate a new key consisting of an existing conversationId and a
			// new continuationId
			return repository.generateContinuationKey(parseKey(previousId).getConversationId());
		}
	}

	private FlowExecutionContinuationKey parseKey(Serializable id) {
		return (FlowExecutionContinuationKey)keyFormatter.parseValue((String)id, FlowExecutionContinuationKey.class);
	}

	// two-phase save support

	public boolean supportsTwoPhaseSave() {
		return true;
	}

	public Serializable generateId(Serializable previousId, ExternalContext context)
			throws FlowExecutionStorageException, UnsupportedOperationException {
		FlowExecutionContinuationKey key = generateContinuationKey(previousId, getRepository(context));
		return keyFormatter.formatValue(key);
	}

	public void saveWithGeneratedId(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException, UnsupportedOperationException {
		getRepository(context).putFlowExecution(parseKey(id), flowExecution);
	}

	/**
	 * Trivial repository factory that simply returns a new
	 * {@link MapFlowExecutionRepository} on each invocation.
	 * @author Keith Donald
	 */
	public static class DefaultFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {
		public FlowExecutionRepository createRepository() {
			return new MapFlowExecutionRepository();
		}
	}
}