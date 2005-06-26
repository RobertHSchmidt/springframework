package org.springframework.binding.format.support;

import java.beans.PropertyEditorSupport;

import org.springframework.binding.format.Formatter;

/**
 * Adapts a formatter to the property editor interface.
 * @author Keith Donald
 */
public class FormatterPropertyEditorAdapter extends PropertyEditorSupport {
	
	/**
	 * The formatter 
	 */
	private Formatter formatter;
	
	/**
	 * The target value class (may be null).
	 */
	private Class targetClass;
	
	/**
	 * @param formatter
	 */
	public FormatterPropertyEditorAdapter(Formatter formatter) {
		this.formatter = formatter;
	}

	/**
	 * @param formatter
	 * @param targetClass
	 */
	public FormatterPropertyEditorAdapter(Formatter formatter, Class targetClass) {
		this.formatter = formatter;
		this.targetClass = targetClass;
	}

	public String getAsText() {
		return formatter.formatValue(getValue());
	}

	public void setAsText(String text) throws IllegalArgumentException {
		setValue(formatter.parseValue(text, targetClass));
	}
}