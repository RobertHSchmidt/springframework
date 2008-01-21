package org.springframework.batch.itemreader;

public interface ItemReader {

	Object read();

	void close();
}
