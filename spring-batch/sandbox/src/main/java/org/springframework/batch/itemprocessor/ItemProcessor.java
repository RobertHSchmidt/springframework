package org.springframework.batch.itemprocessor;

public interface ItemProcessor {

	void process(Object item) throws Exception;
}
