/*
 * Copyright 2006-2008 the original author or authors.
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
package org.springframework.batch.chunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.chunkprocessor.ChunkProcessor;
import org.springframework.util.Assert;

/**
 * A 'chunk' of items, that will be committed together.  It is expected
 * that a chunk may be serialized, especially if using a queue to dispatch
 * chunks to various {@link ChunkProcessor}s.
 * 
 * @author Lucas Ward
 * @since 1.0
 */
public class Chunk implements Serializable{
		
	private final Long id;
	
	private final List items;
	
	public Chunk(Long id, List items) {
		validateSerializable(items);
		this.items = items;
		this.id = id;
	}
	
	/*
	 * In order for the whole chunk to be serializable, every item
	 * in the chunk must be serializable.
	 */
	private void validateSerializable(List items){
		
		for(java.util.Iterator it = items.iterator(); it.hasNext();){
			Object item = it.next();
			Assert.isInstanceOf(Serializable.class, item, "All items in a chunk must be serialiable");
		}
	}
	
	/**
	 * Get the list of items for this chunk.
	 * 
	 * @return items.
	 */
	public List getItems() {
		return new ArrayList(items);
	}
	
	/**
	 * Get the chunk id.
	 * 
	 * @return id of this chunk.
	 */
	public Long getId() {
		return id;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in)
	      throws IOException, ClassNotFoundException {
		
		in.defaultReadObject();
	}

}

