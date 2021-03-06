/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.binding.format.formatters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.util.StringUtils;

/**
 * A formatter for {@link Date} types. Allows the configuration of an explicit date pattern and locale.
 * @see SimpleDateFormat
 * @author Keith Donald
 */
public class DateFormatter implements Formatter {

	/**
	 * The default date pattern.
	 */
	private static final String DEFAULT_PATTERN = "yyyy-MM-dd";

	private String pattern;

	private Locale locale;

	/**
	 * The pattern to use to format date values.
	 * @return the date formatting pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Sets the pattern to use to format date values.
	 * @param pattern the date formatting pattern
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * The locale to use in formatting date values. If null, the default locale is used.
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the locale to use in formatting date values.
	 * @param locale the locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String format(Object date) {
		if (date == null) {
			return "";
		}
		return getDateFormat().format((Date) date);
	}

	public Object parse(String formattedString) throws InvalidFormatException {
		if (!StringUtils.hasText(formattedString)) {
			return null;
		}
		DateFormat dateFormat = getDateFormat();
		try {
			return dateFormat.parse(formattedString);
		} catch (ParseException e) {
			throw new InvalidFormatException(formattedString, determinePattern(pattern), e);
		}
	}

	// subclassing hookings

	protected DateFormat getDateFormat() {
		String pattern = determinePattern(this.pattern);
		Locale locale = determineLocale(this.locale);
		return new SimpleDateFormat(pattern, locale);
	}

	// internal helpers

	private String determinePattern(String pattern) {
		return pattern != null ? pattern : DEFAULT_PATTERN;
	}

	private Locale determineLocale(Locale locale) {
		return locale != null ? locale : Locale.getDefault();
	}

}