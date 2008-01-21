package org.springframework.batch.chunkreader;

import junit.framework.TestCase;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.itemreader.MockItemReader;
import org.springframework.batch.reader.ItemReadingChunkReader;

public class ItemReadingChunkReaderTests extends TestCase {

	public void testSizeNegative() {
		try {
			MockItemReader itemReader = new MockItemReader(10);
			ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
			chunkReader.read(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSizeZero() {
		try {
			MockItemReader itemReader = new MockItemReader(10);
			ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
			chunkReader.read(0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSizePositive() {
		MockItemReader itemReader = new MockItemReader(10);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		Chunk chunk = chunkReader.read(10);
		assertEquals(10, chunk.getItems().size());
	}

	public void testIncompleteChunk() {
		MockItemReader itemReader = new MockItemReader(5);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		Chunk chunk = chunkReader.read(10);
		assertEquals(5, chunk.getItems().size());
	}
}
