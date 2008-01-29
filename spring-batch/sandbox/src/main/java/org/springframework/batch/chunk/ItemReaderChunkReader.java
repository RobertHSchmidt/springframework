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

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.reader.ChunkReader;
import org.springframework.batch.reader.DefaultItemReaderExceptionHandler;
import org.springframework.batch.reader.ItemReaderExceptionHandler;
import org.springframework.batch.reader.SkipLimitExceededException;

/**
 * Implementation of the {@link ChunkReaer} interface that reads items from an
 * {@link ItemReader}.
 * 
 * @author Lucas Ward
 * @since 1.0
 */
public class ItemReaderChunkReader implements ChunkReader {

	private final ItemReader itemReader;
	private ItemReaderExceptionHandler exceptionHandler = new DefaultItemReaderExceptionHandler();
	private int skipLimit = 0;
	private int skippedItemCount = 0;
	private long chunkId;
	
	public ItemReaderChunkReader(ItemReader itemReader, int skipLimit) {
		this.itemReader = itemReader;
		this.skipLimit = skipLimit;
		this.chunkId = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.execution.chunk.ChunkReader#read(int)
	 */
	public Chunk read(int chunkSize) {
		
		List items = new ArrayList(chunkSize);
		int counter = 0;
		Object item;
		
		while(counter++ < chunkSize && (item = readInternal()) != null){
				items.add(item);
		}
		
		if(items.size() == 0){
			return null;
		}
		
		return new Chunk(new Long(chunkId++), items);
	}
	
	/*
	 * Read from the ItemReader and continue doing so until either the skip limit
	 * has been exceeded, the exceptionHandler throws an exception, or the 
	 * reader returns successfully.
	 */
	private Object readInternal(){
		
		try{
			return itemReader.read();
		}
		catch(Exception ex){
			exceptionHandler.onException(ex);				
			skippedItemCount++;
			if(skippedItemCount >= skipLimit){
				throw new SkipLimitExceededException("The Skip limit has been exceeded for this step.");
			}
			
			return readInternal();
		}
			
	}
	
	
	public void setExceptionHandler(ItemReaderExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public void close() {
		//Need to add this to actual ItemReader interface.
	}

}
