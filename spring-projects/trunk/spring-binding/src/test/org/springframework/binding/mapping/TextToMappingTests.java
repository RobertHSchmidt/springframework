package org.springframework.binding.mapping;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.support.DefaultConversionService;

public class TextToMappingTests extends TestCase {
	public void testValidMappingConversion() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter(new TextToMapping(service));
		ConversionExecutor executor = service.getConversionExecutor(String.class, Mapping.class);
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
}