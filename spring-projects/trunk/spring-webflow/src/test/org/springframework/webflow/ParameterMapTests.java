package org.springframework.webflow;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.web.multipart.MultipartFile;

public class ParameterMapTests extends TestCase {
	private ParameterMap parameterMap;

	public void setUp() {
		Map map = new HashMap();
		map.put("string", "A string");
		map.put("integer", "12345");
		map.put("stringArray", new String[] { "1", "2", "3" });
		MockControl control = MockControl.createControl(MultipartFile.class);
		map.put("multipartFile", control.getMock());
		parameterMap = new ParameterMap(map);
	}

	public void testGet() {
		String value = parameterMap.get("string");
		assertEquals("A string", value);
	}

	public void testGetNull() {
		String value = parameterMap.get("bogus");
		assertNull(value);
	}

	public void testGetRequired() {
		String value = parameterMap.getRequired("string");
		assertEquals("A string", value);
	}

	public void testGetRequiredNotPresent() {
		try {
			parameterMap.getRequired("bogus");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetWithDefaultOption() {
		String value = parameterMap.get("string", "default");
		assertEquals("A string", value);
	}

	public void testGetWithDefault() {
		String value = parameterMap.get("bogus", "default");
		assertEquals("default", value);
	}

	public void testGetArray() {
		String[] value = parameterMap.getArray("stringArray");
		assertEquals(3, value.length);
	}

	public void testGetArrayNull() {
		String[] value = parameterMap.getArray("bogus");
		assertNull(value);
	}

	public void testGetArrayRequired() {
		String[] value = parameterMap.getRequiredArray("stringArray");
		assertEquals(3, value.length);
	}

	public void testGetRequiredArrayNotPresent() {
		try {
			parameterMap.getRequiredArray("bogus");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetSingleValueAsArray() {
		String[] value = parameterMap.getArray("string");
		assertEquals(1, value.length);
		assertEquals("A string", value[0]);
	}

	public void testGetMultipart() {
		MultipartFile file = parameterMap.getMultipartFile("multipartFile");
		assertNotNull(file);
	}

	public void testGetRequiredMultipart() {
		MultipartFile file = parameterMap.getRequiredMultipartFile("multipartFile");
		assertNotNull(file);
	}

	public void testGetRequiredMultipartNotPresent() {
		try {
			parameterMap.getRequiredMultipartFile("bogus");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetConversion() {
		Integer i = parameterMap.getInteger("integer");
		assertEquals(new Integer(12345), i);
	}

	public void testGetArrayConversion() {
		Integer[] i = (Integer[])parameterMap.getArray("stringArray", Integer.class);
		assertEquals(i.length, 3);
		assertEquals(new Integer(1), i[0]);
		assertEquals(new Integer(2), i[1]);
		assertEquals(new Integer(3), i[2]);
	}
}