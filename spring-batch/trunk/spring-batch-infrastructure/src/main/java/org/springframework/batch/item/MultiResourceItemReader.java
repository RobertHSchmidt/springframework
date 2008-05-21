package org.springframework.batch.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

/**
 * Reads items from multiple resources sequentially - resource list is given by
 * {@link #setResourcePatternResolver(ResourcePatternResolver)}, the actual
 * reading is delegated to
 * {@link #setDelegate(ResourceAwareItemReaderItemStream)}.
 * 
 * Reset (rollback) capability is implemented by item buffering.
 * 
 * @author Robert Kasanicky
 */
public class MultiResourceItemReader extends ExecutionContextUserSupport implements ItemReader, ItemStream,
		InitializingBean {

	private static final String RESOURCE_INDEX = "resourceIndex";

	private ResourceAwareItemReaderItemStream delegate;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private String resourceLocationPattern;

	private Resource[] resources;

	private int currentResourceIndex;

	private List itemBuffer = new ArrayList();

	private Iterator itemBufferIterator = null;

	private boolean shouldReadBuffer = false;

	public MultiResourceItemReader() {
		setName(MultiResourceItemReader.class.getSimpleName());
	}

	/**
	 * Reads the next item, jumping to next resource if necessary.
	 */
	public Object read() throws Exception, UnexpectedInputException, NoWorkFoundException, ParseException {

		if (shouldReadBuffer) {
			if (itemBufferIterator.hasNext()) {
				return itemBufferIterator.next();
			}
			else {
				// buffer is exhausted, continue reading from file
				shouldReadBuffer = false;
				itemBufferIterator = null;
			}
		}

		Object item = delegate.read();

		while (item == null) {

			if (++currentResourceIndex >= resources.length) {
				return null;
			}
			delegate.close(new ExecutionContext());
			delegate.setResource(resources[currentResourceIndex]);
			delegate.open(new ExecutionContext());
			item = delegate.read();

		}

		itemBuffer.add(item);

		return item;
	}

	/**
	 * Clears the item buffer and cancels reading from buffer if it applies.
	 * 
	 * @see ItemReader#mark()
	 */
	public void mark() throws MarkFailedException {
		delegate.mark();
		itemBuffer.clear();
		shouldReadBuffer = false;
	}

	/**
	 * Switches to 'read from buffer' state.
	 * 
	 * @see ItemReader#reset()
	 */
	public void reset() throws ResetFailedException {
		shouldReadBuffer = true;
		itemBufferIterator = itemBuffer.listIterator();
	}

	/**
	 * Close the {@link #setDelegate(ResourceAwareItemReaderItemStream)} reader
	 * and reset instance variable values.
	 */
	public void close(ExecutionContext executionContext) throws ItemStreamException {
		shouldReadBuffer = false;
		itemBufferIterator = null;
		itemBuffer.clear();
		delegate.close(executionContext);
	}

	/**
	 * Figure out which resource to start with in case of restart and open the
	 * delegate.
	 */
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		try {
			resources = resourcePatternResolver.getResources(resourceLocationPattern);
		}
		catch (Exception e) {
			throw new ItemStreamException("Couldn't get resource", e);
		}

		if (executionContext.containsKey(getKey(RESOURCE_INDEX))) {
			int index = Long.valueOf(executionContext.getLong(getKey(RESOURCE_INDEX))).intValue();
			currentResourceIndex = index;
		}

		delegate.setResource(resources[currentResourceIndex]);

		delegate.open(executionContext);
	}

	/**
	 * Store the current resource index and delegate's data.
	 */
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		executionContext.putLong(getKey(RESOURCE_INDEX), currentResourceIndex);
		delegate.update(executionContext);
	}

	/**
	 * @param delegate reads items from single {@link Resource}.
	 */
	public void setDelegate(ResourceAwareItemReaderItemStream delegate) {
		this.delegate = delegate;
	}

	/**
	 * @param resourcePatternResolver provides the list of input
	 * {@link Resource}s given {@link #setResourceLocationPattern(String)}.
	 * {@link PathMatchingResourcePatternResolver} is used by default.
	 */
	public void setResourcePatternResolver(ResourcePatternResolver resourcePatternResolver) {
		this.resourcePatternResolver = resourcePatternResolver;
	}

	/**
	 * @param resourceLocationPattern identifies the input {@link Resource}s,
	 * parsed by {@link #setResourcePatternResolver(ResourcePatternResolver)}
	 */
	public void setResourceLocationPattern(String resourceLocationPattern) {
		this.resourceLocationPattern = resourceLocationPattern;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(resourcePatternResolver, "resourcePatternResolver property must be set");
		Assert.hasLength(resourceLocationPattern, "resourceLocationPattern property must be set");
	}

}
