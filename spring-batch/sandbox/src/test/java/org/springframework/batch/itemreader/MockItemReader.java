package org.springframework.batch.itemreader;

public class MockItemReader implements ItemReader {

	private final int returnItemCount;

	private int returnedItemCount;
	
	private boolean markSupported = false;

	public MockItemReader() {
		this(-1);
	}

	public MockItemReader(int returnItemCount) {
		this.returnItemCount = returnItemCount;
	}
	
	public void setMarkSupported(boolean markSupported) {
	    this.markSupported = markSupported;
    }

	public void close() {
	}

	public void mark() {
		if(!markSupported) {
			throw new UnsupportedOperationException("The mark() method is not supported");
		}
	}

	public boolean markSupported() {
		return markSupported;
	}

	public Object read() {
		if (returnItemCount < 0 || returnedItemCount < returnItemCount) {
			return String.valueOf(returnedItemCount++);
		}
		return null;
	}

}
