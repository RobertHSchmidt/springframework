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

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.chunkprocessor.ChunkProcessor;
import org.springframework.batch.itemreader.RecoveryManager;
import org.springframework.batch.reader.ChunkReader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

public class ChunkingStepExecutor implements StepExecutor {

	private final ChunkReader chunkReader;

	private final ChunkProcessor chunkProcessor;

	private final RecoveryManager recoveryManager;

	private final PlatformTransactionManager transactionManager;

	private int chunkSize = 1;

	public ChunkingStepExecutor(ChunkReader chunkReader, ChunkProcessor chunkProcessor,
	        PlatformTransactionManager transactionManager, RecoveryManager recoveryManager) {
		this.chunkReader = chunkReader;
		this.chunkProcessor = chunkProcessor;
		this.transactionManager = transactionManager;
		this.recoveryManager = recoveryManager;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public void execute() {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		RecoveryManagerSynchronization recoverySynchronization = new RecoveryManagerSynchronization(recoveryManager);
		ChunkReadingTransactionCallBack transactionCallBack = new ChunkReadingTransactionCallBack(chunkReader,
		        chunkSize, recoverySynchronization);
		while (txTemplate.execute(transactionCallBack) != null) {
		}
	}

	private class ChunkReadingTransactionCallBack implements TransactionCallback {

		private final ChunkReader chunkReader;

		private final int chunkSize;

		private final RecoveryManagerSynchronization recoverySynchronization;

		public ChunkReadingTransactionCallBack(ChunkReader chunkReader, int chunkSize,
		        RecoveryManagerSynchronization recoverySynchronization) {
			this.chunkReader = chunkReader;
			this.chunkSize = chunkSize;
			this.recoverySynchronization = recoverySynchronization;
		}

		public Object doInTransaction(TransactionStatus status) {
			TransactionSynchronizationManager.registerSynchronization(recoverySynchronization);
			Chunk chunk = chunkReader.read(chunkSize);
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
	}

}
