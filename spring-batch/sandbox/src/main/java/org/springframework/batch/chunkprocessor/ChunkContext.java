package org.springframework.batch.chunkprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkContext {

	private final Map context = new HashMap();
	
	private final List skippedItems = new ArrayList();

	public void addSkippedItem(Object item) {
		skippedItems.add(item);
	}

	public boolean containsSkippedItem(Object item) {
		return skippedItems.contains(item);
	}

	public boolean containsKey(Object key) {
	    return context.containsKey(key);
    }

	public Object get(Object key) {
	    return context.get(key);
    }

	public Object put(Object arg0, Object arg1) {
	    return context.put(arg0, arg1);
    }

	public void putAll(Map arg0) {
	    context.putAll(arg0);
    }

	public Object remove(Object key) {
	    return context.remove(key);
    }

}
