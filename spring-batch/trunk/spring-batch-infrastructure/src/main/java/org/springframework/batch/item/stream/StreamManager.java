/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.item.stream;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.exception.StreamException;
import org.springframework.transaction.TransactionStatus;

/**
 * Generalized stream management broadcast strategy. Clients register
 * {@link ItemStream} instances under a well-known key, and then when they ask
 * for {@link ItemStream} operations by that key, we call each one in turn that
 * is registered under the key.
 * 
 * @author Dave Syer
 * 
 */
public interface StreamManager {

	/**
	 * Register the {@link ItemStream} instance as one of possibly several that
	 * are associated with the given key.
	 * 
	 * @param key the key under which to add the provider
	 * @param stream an {@link ItemStream}
	 */
	void register(ItemStream stream);

	/**
	 * Extract and aggregate the {@link ExecutionContext} from all streams under
	 * this key.
	 * 
	 * @param key the key under which {@link ItemStream} instances might have
	 * been registered.
	 * @return {@link ExecutionContext} aggregating the contexts of all providers
	 * registered under this key, or empty otherwise.
	 */
	void update();

	/**
	 * If any resources are needed for the stream to operate they need to be
	 * destroyed here.
	 * 
	 * @param key the key under which {@link ItemStream} instances might have
	 * been registered.
	 */
	void close() throws StreamException;

	/**
	 * If any resources are needed for the stream to operate they need to be
	 * opened here.
	 * 
	 * @param key the key under which {@link ItemStream} instances might have
	 * been registered.
	 */
	void open(ExecutionContext executionContext) throws StreamException;
	
	TransactionStatus getTransaction();
	
	void commit(TransactionStatus transaction);
	
	void rollback(TransactionStatus transaction);

}
