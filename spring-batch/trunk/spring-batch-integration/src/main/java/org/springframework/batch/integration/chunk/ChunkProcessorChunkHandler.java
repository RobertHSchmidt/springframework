package org.springframework.batch.integration.chunk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.annotation.Handler;
import org.springframework.util.Assert;

public class ChunkProcessorChunkHandler<S> implements ChunkHandler<S>, InitializingBean {

	private static final Log logger = LogFactory.getLog(ChunkProcessorChunkHandler.class);

	private ChunkProcessor<S> chunkProcessor;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(chunkProcessor, "A ChunkProcessor must be provided");
	}
	
	/**
	 * Public setter for the {@link ChunkProcessor}.
	 * @param chunkProcessor the chunkProcessor to set
	 */
	public void setChunkProcessor(ChunkProcessor<S> chunkProcessor) {
		this.chunkProcessor = chunkProcessor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.integration.batch.slave.ChunkHandler#handleChunk(java.util.Collection)
	 */
	@Handler
	public ChunkResponse handleChunk(ChunkRequest<S> chunkRequest) {

		logger.debug("Handling chunk: " + chunkRequest);

		int skipCount = 0;
		try {
			skipCount = chunkProcessor.process(chunkRequest.getItems(), chunkRequest.getSkipCount());
		}
		catch (Exception e) {
			logger.debug("Failed chunk", e);
			return new ChunkResponse(ExitStatus.FAILED.addExitDescription(e.getClass().getName() + ": "
					+ e.getMessage()), chunkRequest.getJobId(), skipCount);
		}

		logger.debug("Completed chunk handling with " + skipCount + " skips");
		return new ChunkResponse(ExitStatus.CONTINUABLE, chunkRequest.getJobId(), skipCount);

	}
}
