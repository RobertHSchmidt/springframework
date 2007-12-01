package org.springframework.config.java.util;

public final class ArrayUtils {

	private ArrayUtils() {
	}

	/**
	 * Reverse the contents of <var>array</var>.
	 * 
	 * <p/>This method is used for reversing the order of classes passed into
	 * constructors of
	 * {@link org.springframework.config.java.context.JavaConfigApplicationContext}
	 * or {@link org.springframework.config.java.annotation.Import}.
	 * 
	 * <p/>TODO: shouldn't actually be necessary. Root out the real issue with
	 * ordering
	 * 
	 * @param array - array to reverse
	 * @return reverse of <var>array</var>
	 */
	public static Class<?>[] reverse(Class<?>[] array) {
		int size = array.length;
		Class<?>[] reversed = new Class<?>[size];

		for (int i = 0; i < size; i++)
			reversed[size - i - 1] = array[i];

		return reversed;
	}

}
