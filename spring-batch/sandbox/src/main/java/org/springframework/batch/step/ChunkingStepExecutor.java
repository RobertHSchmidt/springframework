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
