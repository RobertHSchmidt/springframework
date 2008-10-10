package org.springframework.batch.core.step.item;

/**
 * Wrapper for an item and its exception if it failed processing.
 * 
 * @author Dave Syer
 * 
 */
public class ItemWrapper<T> {

	final private Exception exception;

	final private T item;

	/**
	 * @param item
	 */
	public ItemWrapper(T item) {
		this(item, null);
	}


	public ItemWrapper(T item, Exception e) {
		this.item = item;
		this.exception = e;
	}

	/**
	 * Public getter for the exception.
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Public getter for the item.
	 * @return the item
	 */
	public T getItem() {
		return item;
	}

	@Override
	public String toString() {
		return String.format("[exception=%s, item=%s]", exception, item);
	}

}