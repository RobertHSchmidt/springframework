package org.springframework.binding.format.support;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.binding.format.Formatter;

import junit.framework.TestCase;

/**
 * Unit tests for {@link NumberFormatterTests}.
 * 
 * @author Erwin Vervaet
 */
public class NumberFormatterTests extends TestCase {
	
	private Locale systemDefaultLocale;
	
	protected void setUp() throws Exception {
		systemDefaultLocale = Locale.getDefault();
	}
	
	protected void tearDown() throws Exception {
		// restore default
		Locale.setDefault(systemDefaultLocale);
	}
	
	public void testParseBigDecimalInUs() {
		Locale.setDefault(Locale.US);
		Formatter formatter = new SimpleFormatterFactory().getNumberFormatter(BigDecimal.class);
		assertEquals(new BigDecimal("123.45"), formatter.parseValue("123.45", BigDecimal.class));
	}

	public void testParseBigDecimalInGermany() {
		Locale.setDefault(Locale.GERMANY);
		Formatter formatter = new SimpleFormatterFactory().getNumberFormatter(BigDecimal.class);
		assertEquals(new BigDecimal("123.45"), formatter.parseValue("123.45", BigDecimal.class));
	}
}
