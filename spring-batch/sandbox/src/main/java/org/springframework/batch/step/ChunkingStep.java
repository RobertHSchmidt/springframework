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
package org.springframework.batch.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class ChunkingStep implements Step {
	
	private final ItemWriter itemWriter;
	
	private final StreamManager streamManager;
	
	private final ChunkReader chunkReader;
	
	private final int chunkSize;
	
	private final TransactionTemplate txTemplate;
	
	private String name;
	
	private int startLimit;
	
	private boolean allowStartIfComplete;
	
	private boolean saveExecutionAttributes;
	
	private ChunkingStep(ChunkReader chunkReader, int chunkSize, ItemWriter itemWriter, StreamManager streamManager,
			PlatformTransactionManager txManager) {
		this.chunkReader = chunkReader;
		this.chunkSize = chunkSize;
		this.itemWriter = itemWriter;
		this.streamManager = streamManager;
		this.txTemplate = new TransactionTemplate(txManager);
	}

	public void setName(String name) {
	    this.name = name;
    }
	
	public String getName() {
	    return name;
    }
	
	public void setStartLimit(int startLimit) {
	    this.startLimit = startLimit;
    }

	public int getStartLimit() {
		return this.startLimit;
    }
	
	public void setAllowStartIfComplete(boolean allowStartIfComplete) {
	    this.allowStartIfComplete = allowStartIfComplete;
    }

	public boolean isAllowStartIfComplete() {
	    return allowStartIfComplete;
    }
	
	public void setSaveExecutionAttributes(boolean saveExecutionAttributes) {
	    this.saveExecutionAttributes = saveExecutionAttributes;
    }

	public boolean isSaveExecutionAttributes() {
	    return saveExecutionAttributes;
    }

	public void execute(StepExecution stepExecution) throws StepInterruptedException, BatchCriticalException {
		try {
			txTemplate.execute(new StreamOpener(streamManager));
			// while chunks are returned
				// open transaction
				// read chunk
				// process chunk (strategy)
				// commit transaction
				// if exception
					// rollback exception

		} finally {
			txTemplate.execute(new StreamCloser(streamManager));
		}
    }


	private class StreamOpener implements TransactionCallback {
		
		private final StreamManager streamManager;
		
		public StreamOpener(StreamManager streamManager) {
			this.streamManager = streamManager;
		}

		public Object doInTransaction(TransactionStatus status) {
			streamManager.open();
			return null;
        }

	}
	
	private class StreamCloser implements TransactionCallback {

		private final StreamManager streamManager;
		
		public StreamCloser(StreamManager streamManager) {
			this.streamManager = streamManager;
		}

		public Object doInTransaction(TransactionStatus status) {
			streamManager.close();
			return null;
        }
	}




/**	private final ChunkReader chunkReader;



	public void execute() {
		ReadContext readContext = new ReadContext();
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		RecoveryManagerSynchronization recoverySynchronization = new RecoveryManagerSynchronization(recoveryManager);
		ChunkReadingTransactionCallBack transactionCallBack = new ChunkReadingTransactionCallBack(chunkReader,
		        chunkSize, readContext, recoverySynchronization);
		while (txTemplate.execute(transactionCallBack) != null) {
		}
	}

	private class ChunkReadingTransactionCallBack implements TransactionCallback {

		private final ChunkReader chunkReader;

		private final int chunkSize;

		private final ReadContext readContext;

		private final RecoveryManagerSynchronization recoverySynchronization;

		public ChunkReadingTransactionCallBack(ChunkReader chunkReader, int chunkSize, ReadContext readContext,
		        RecoveryManagerSynchronization recoverySynchronization) {
			this.chunkReader = chunkReader;
			this.chunkSize = chunkSize;
			this.readContext = readContext;
			this.recoverySynchronization = recoverySynchronization;
		}

		public Object doInTransaction(TransactionStatus status) {
			TransactionSynchronizationManager.registerSynchronization(recoverySynchronization);
			Chunk chunk = chunkReader.read(chunkSize, readContext);
			chunkProcessor.process(chunk);
			return chunk;
		}
	}

	private class RecoveryManagerSynchronization extends TransactionSynchronizationAdapter {

		private RecoveryManager recoveryManager;

		public RecoveryManagerSynchronization(RecoveryManager recoveryManager) {
			this.recoveryManager = recoveryManager;
		}

		public void afterCompletion(int status) {
			if (status == TransactionSynchronization.STATUS_COMMITTED) {
				recoveryManager.mark();
			} else {
				recoveryManager.reset();
			}
		}
	} **/

}
