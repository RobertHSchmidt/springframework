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
package org.springframework.batch.chunkreader;

import java.util.ArrayList;
import java.util.List;

import org.easymock.MockControl;
import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.chunk.ItemReaderChunkReader;
import org.springframework.batch.io.exception.FlatFileParsingException;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.reader.ListItemReader;
import org.springframework.batch.reader.ItemReaderExceptionHandler;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.handler.ExceptionHandler;

import junit.framework.TestCase;

/**
 * @author Lucas Ward
 *
 */
public class ItemReaderChunkReaderTests extends TestCase {

	ItemReaderChunkReader chunkReader;
	
	MockControl itemControl = MockControl.createControl(ItemReader.class);
	
	ItemReader itemReader;
	
	List testItems;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		itemReader = (ItemReader)itemControl.getMock();
		chunkReader = new ItemReaderChunkReader(itemReader, 5);
		chunkReader.setExceptionHandler(new FileExceptionHandler());
		
		testItems = new ArrayList();
		testItems.add("1");
		testItems.add("2");
	}
	
	public void testNormalCase() throws Exception{
		
		chunkReader = new ItemReaderChunkReader(new ListItemReader(testItems), 2);
	
		Chunk chunk = chunkReader.read(2);
		
		assertTrue(chunk.getItems().equals(testItems));
	}
	
	public void testNoExceptionHandler() throws Exception{
		
		RuntimeException exception = new RuntimeException();
		
		itemReader.read();
		itemControl.setThrowable(exception);
		itemControl.replay();
		
		try{
			chunkReader.read(3);
			fail();
		}
		catch(Exception ex){
			assertEquals(exception, ex);
		}
	}
	
	public void testExceptionHandler() throws Exception{
		
		ItemReaderExceptionHandler exceptionHandler = new FileExceptionHandler();
			
		chunkReader.setExceptionHandler(exceptionHandler);
		
		itemReader.read();
		itemControl.setThrowable(new Exception());
		itemControl.replay();
		
		try{
			chunkReader.read(3);
			fail();
		}
		catch(RuntimeException ex){
			//excpeted
		}
	}
	
	public void testExceptionHandlerSwallowException() throws Exception{
		
		ItemReaderExceptionHandler exceptionHandler = new FileExceptionHandler();
		
		chunkReader.setExceptionHandler(exceptionHandler);
		
		itemReader.read();
		itemControl.setThrowable(new FlatFileParsingException("", ""));
		itemReader.read();
		itemControl.setReturnValue("1");
		itemControl.replay();
	
		Chunk chunk = chunkReader.read(1);
		List chunkItems = chunk.getItems();
		assertEquals("1", chunkItems.get(0));
	}
	
	public void testChunkSizeGreaterThanThanRemainingItems() throws Exception{
		
		itemReader.read();
		itemControl.setReturnValue("1");
		itemReader.read();
		itemControl.setReturnValue(null);
		itemControl.replay();
		
		Chunk chunk = chunkReader.read(5);
		assertEquals(1, chunk.getItems().size());
		assertEquals("1", chunk.getItems().get(0));
	}
	
	public void testEndOfStream() throws Exception{
		
		itemReader.read();
		itemControl.setReturnValue(null);
		itemControl.replay();
		
		Chunk chunk = chunkReader.read(3);
		assertNull(chunk);
	}
	
	
	private class FileExceptionHandler implements ItemReaderExceptionHandler{

		public void onException(Throwable throwable)
				throws RuntimeException {
			
			if(throwable instanceof FlatFileParsingException){
				//log out the exception
			}
			else{
				if(throwable instanceof RuntimeException){
					throw (RuntimeException)throwable;
				}
				else{
					throw new RuntimeException(throwable);
				}
			}
			
		}
	}
}
