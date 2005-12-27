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
 * Flow execution storage implementation that stores the flow execution in an
 * externally managed data store. The actual interface to the data store is
 * pluggable by using a {@link MapAccessor}.
 * 
 * @see org.springframework.webflow.execution.MapAccessor
 * 
 * @author Erwin Vervaet
 */
public class RepositoryFlowExecutionStorage implements FlowExecutionStorage {

	protected final Log logger = LogFactory.getLog(getClass());

	private MapAccessor repositoryMapAccessor = new SessionMapAccessor();

	private FlowExecutionRepositoryFactory repositoryFactory = new DefaultFlowExecutionRepositoryFactory();

	private Formatter keyFormatter = new FlowExecutionKeyFormatter();

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
		FlowExecutionKey key;
		if (id == null) {
			key = repository.generateKey();
		} else {
			key = repository.generateContinuationKey(parseKey(id).getConversationId());
		}
		repository.putFlowExecution(key, flowExecution);
		return keyFormatter.formatValue(key);
	}

	public void remove(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		getRepository(context).remove(parseKey(id));
	}

	// helpers

	private synchronized FlowExecutionRepository getRepository(ExternalContext context) {
		FlowExecutionRepository repository = (FlowExecutionRepository)getRepositoryMap(context).get(
				FlowExecutionRepository.class.getName());
		if (repository == null) {
			repository = repositoryFactory.createRepository();
			getRepositoryMap(context).put(FlowExecutionRepository.class.getName(), repository);
		}
		return repository;
	}

	/**
	 * Returns the data store attribute map
	 * 
	 * @param sourceEvent the event
	 * @return the data store
	 */
	protected Map getRepositoryMap(ExternalContext context) {
		return repositoryMapAccessor.getMap(context);
	}

	private FlowExecutionKey parseKey(Serializable id) {
		return (FlowExecutionKey)keyFormatter.parseValue((String)id, FlowExecutionKey.class);
	}

	// two-phase save support

	public boolean supportsTwoPhaseSave() {
		return true;
	}

	public Serializable generateId(Serializable previousId, ExternalContext context)
			throws FlowExecutionStorageException, UnsupportedOperationException {
		if (previousId == null) {
			return keyFormatter.formatValue(getRepository(context).generateKey());
		}
		else {
			Serializable conversationId = parseKey(previousId).getConversationId();
			return keyFormatter.formatValue(getRepository(context).generateContinuationKey(conversationId));
		}
	}

	public void saveWithGeneratedId(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException, UnsupportedOperationException {
		getRepository(context).putFlowExecution(parseKey(id), flowExecution);
	}

	public static class DefaultFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {
		public FlowExecutionRepository createRepository() {
			return new MapFlowExecutionRepository();
		}
	}
}