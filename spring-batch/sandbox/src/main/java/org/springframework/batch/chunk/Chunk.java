package org.springframework.batch.chunk;

import java.util.List;

public class Chunk {

	private final Long id;

	private final List items;

	public Chunk(Long id, List items) {
		this.id = id;
		this.items = items;
	}

	public Long getId() {
		return id;
	}

	public List getItems() {
		return items;
	}

}
