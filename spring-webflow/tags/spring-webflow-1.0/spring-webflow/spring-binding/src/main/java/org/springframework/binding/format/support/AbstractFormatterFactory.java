/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.binding.format.support;

import java.util.Locale;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.FormatterFactory;
import org.springframework.binding.format.Style;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;

/**
 * Base class for formatter factories.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFormatterFactory implements FormatterFactory {

	private LocaleContext localeContext = new SimpleLocaleContext(Locale.getDefault());

	private Style defaultDateStyle = Style.MEDIUM;

	private Style defaultTimeStyle = Style.MEDIUM;

	public void setLocaleContext(LocaleContext localeContext) {
		this.localeContext = localeContext;
	}

	public void setDefaultDateStyle(Style defaultDateStyle) {
		this.defaultDateStyle = defaultDateStyle;
	}

	public void setDefaultTimeStyle(Style defaultTimeStyle) {
		this.defaultTimeStyle = defaultTimeStyle;
	}

	protected Style getDefaultDateStyle() {
		return defaultDateStyle;
	}

	protected Style getDefaultTimeStyle() {
		return defaultTimeStyle;
	}

	protected Locale getLocale() {
		return localeContext.getLocale();
	}

	public Formatter getDateFormatter() {
		return getDateFormatter(getDefaultDateStyle());
	}

	public Formatter getDateTimeFormatter() {
		return getDateTimeFormatter(getDefaultDateStyle(), getDefaultTimeStyle());
	}

	public Formatter getTimeFormatter() {
		return getTimeFormatter(getDefaultTimeStyle());
	}
}