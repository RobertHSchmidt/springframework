package org.springframework.batch.itemreader;

public interface RecoveryManager {

	void mark();
	
	void reset();
}
