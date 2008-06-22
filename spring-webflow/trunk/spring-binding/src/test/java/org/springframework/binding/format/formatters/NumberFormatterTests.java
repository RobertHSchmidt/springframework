package org.springframework.binding.format.formatters;

import java.math.BigDecimal;
import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.binding.format.InvalidFormatException;

public class NumberFormatterTests extends TestCase {

	private NumberFormatter formatter;

	public void testFormatIntegerDefaultPattern() {
		formatter = new NumberFormatter(Integer.class);
		formatter.setLocale(Locale.US);
		String value = formatter.format(new Integer(12345));
		assertEquals("12,345", value);
	}

	public void testFormatBigDecimalCustomPattern() {
		formatter = new NumberFormatter(BigDecimal.class);
		formatter.setPattern("000.00");
		formatter.setLocale(Locale.US);
		BigDecimal dec = new BigDecimal("123.45");
		String value = formatter.format(dec);
		assertEquals("123.45", value);
	}

	public void testFormatNull() {
		formatter = new NumberFormatter(Integer.class);
		assertEquals("", formatter.format(null));
	}

	public void testParseIntegerDefaultPattern() {
		formatter = new NumberFormatter(Integer.class);
		formatter.setLocale(Locale.US);
		Integer integer = (Integer) formatter.parse("123,450");
		assertEquals(Integer.valueOf(123450), integer);
	}

	public void testParseBigDecimalCustomPattern() {
		formatter = new NumberFormatter(BigDecimal.class);
		formatter.setPattern("000.00");
		formatter.setLocale(Locale.US);
		BigDecimal dec = (BigDecimal) formatter.parse("123.45");
		assertEquals(new BigDecimal("123.45"), dec);
	}

	public void testParseInvalidFormatPatternTruncation() {
		try {
			formatter = new NumberFormatter(Integer.class);
			formatter.setLocale(Locale.US);
			formatter.parse("123,450b");
			fail("Should have failed");
		} catch (InvalidFormatException e) {
		}
	}

	public void testParseInvalidFormatPatternTruncationInteger() {
		try {
			formatter = new IntegerFormatter(Integer.class);
			formatter.setLocale(Locale.US);
			formatter.parse("123,450.00");
			fail("Should have failed");
		} catch (InvalidFormatException e) {

		}
	}

	public void testParseInvalidFormatPatternLenient() {
		formatter = new NumberFormatter(Integer.class);
		formatter.setLocale(Locale.US);
		formatter.setLenient(true);
		Integer integer = (Integer) formatter.parse("123,450b");
		assertEquals(Integer.valueOf(123450), integer);
	}

	public void testParseInvalidFormatPattern() {
		try {
			formatter = new NumberFormatter(BigDecimal.class);
			formatter.setPattern("000.00");
			formatter.parse("bogus");
			fail("Should have failed");
		} catch (InvalidFormatException e) {
		}
	}

	public void testParseNull() {
		formatter = new NumberFormatter(Integer.class);
		assertNull(formatter.parse(null));
	}

	public void testParseEmptyString() {
		formatter = new NumberFormatter(Integer.class);
		assertNull(formatter.parse(""));
	}

}
