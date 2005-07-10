/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.binding.convert.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.format.support.SimpleFormatterLocator;
import org.springframework.binding.support.Mapping;
import org.springframework.core.enums.ShortCodedLabeledEnum;

/**
 * Test case for the default conversion service.
 * 
 * @author Keith Donald
 */
public class DefaultConversionServiceTests extends TestCase {
	
	public void testNoConvertersRegistered() {
		DefaultConversionService service = new DefaultConversionService(false);
		try {
			service.conversionExecutorFor(String.class, Integer.class);
			fail("Should have thrown an ise");
		}
		catch (IllegalStateException e) {

		}
	}

	public void testTargetClassNotSupported() {
		DefaultConversionService service = new DefaultConversionService();
		try {
			service.conversionExecutorFor(String.class, HashMap.class);
			fail("Should have thrown an ise");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testValidConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.conversionExecutorFor(String.class, Integer.class);
		Integer three = (Integer)executor.execute("3");
		assertEquals(3, three.intValue());
	}

	public void testLabeledEnumConversionNoSuchEnum() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter(new TextToLabeledEnum(MyEnum.class, new SimpleFormatterLocator()));
		ConversionExecutor executor = service.conversionExecutorFor(String.class, MyEnum.class);
		try {
			executor.execute("My Invalid Label");
			fail("Should have failed");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testValidLabeledEnumConversion() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter(new TextToLabeledEnum(MyEnum.class, new SimpleFormatterLocator()));
		ConversionExecutor executor = service.conversionExecutorFor(String.class, MyEnum.class);
		MyEnum myEnum = (MyEnum)executor.execute("My Label 1");
		assertEquals(MyEnum.ONE, myEnum);
	}

	public void testValidMappingConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.conversionExecutorFor(String.class, Mapping.class);
		Mapping mapping = (Mapping)executor.execute("id");
		Map source = new HashMap(1);
		source.put("id", "5");	
		Map target = new HashMap(1);
		mapping = (Mapping)executor.execute("id,java.lang.Long");
		mapping.map(source, target, null);
		assertEquals(new Long(5), target.get("id"));
		
		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id->id");
		mapping.map(source, target, null);
		assertEquals("5", target.get("id"));

		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id->colleagueId,java.lang.Long");
		mapping.map(source, target, null);
		assertEquals(new Long(5), target.get("colleagueId"));

		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id,java.lang.String->colleagueId");
		mapping.map(source, target, null);
		assertEquals("5", target.get("colleagueId"));

		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id,java.lang.String->colleagueId,java.lang.Long");
		mapping.map(source, target, null);
		assertEquals(new Long(5), target.get("colleagueId"));

	}

	public static class MyEnum extends ShortCodedLabeledEnum {
		public static MyEnum ONE = new MyEnum(0, "My Label 1");

		public static MyEnum TWO = new MyEnum(1, "My Label 2");

		private MyEnum(int code, String label) {
			super(code, label);
		}
	}
}