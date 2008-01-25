package org.springframework.batch.reader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.itemreader.ItemReader;
import org.springframework.util.Assert;

public class ItemReadingChunkReader implements ChunkReader {

	private final ItemReader itemReader;
	
	private long chunkCounter = 0;

	public ItemReadingChunkReader(ItemReader itemReader) {
		this.itemReader = itemReader;
	}

	public void close() {
		itemReader.close();
	}

	public Chunk read(int size) {
		Assert.isTrue(size > 0, "Chunk size must be greater than 0");

		int counter = 0;
		List items = new ArrayList(size);

		Object item;
		while ((item = itemReader.read()) != null && counter++ < size) {
			items.add(item);
		}

		if (items.size() == 0) {
			return null;
		}

		return new Chunk(getChunkId(), items);
	}

	private synchronized Long getChunkId() {
		return new Long(chunkCounter++);
	}
}
