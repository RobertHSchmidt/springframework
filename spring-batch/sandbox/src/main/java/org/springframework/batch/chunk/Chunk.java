package org.springframework.batch.chunk;

import java.io.Serializable;
import java.util.List;

public class Chunk {

	private final Serializable id;

	private final List items;

	public Chunk(Serializable id, List items) {
		this.id = id;
		this.items = items;
	}
	
	public Serializable getId() {
	    return id;
    }

	public List getItems() {
		return items;
	}

}
