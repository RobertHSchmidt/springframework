package org.springframework.binding.mapping;

import java.util.HashMap;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.OgnlExpressionParser;

public class RequiredMappingTests extends TestCase {
	public void testRequired() {
		MappingBuilder builder = new MappingBuilder(new OgnlExpressionParser());
		Mapping mapping = builder.source("foo").target("bar").required().value();
		HashMap source = new HashMap();
		source.put("foo", "baz");
		HashMap target = new HashMap();
		mapping.map(source, target, null);
		assertEquals("baz", target.get("bar"));
	}
	
	public void testRequiredExceptionOnNull() {
		MappingBuilder builder = new MappingBuilder(new OgnlExpressionParser());
		Mapping mapping = builder.source("foo").target("bar").required().value();
		HashMap source = new HashMap();
		source.put("foo", null);
		HashMap target = new HashMap();
		try {
			mapping.map(source, target, null);
		} catch (RequiredMappingException e) {
			
		}
	}

	public void testRequiredExceptionOnNoKey() {
		MappingBuilder builder = new MappingBuilder(new OgnlExpressionParser());
		Mapping mapping = builder.source("foo").target("bar").required().value();
		HashMap source = new HashMap();
		HashMap target = new HashMap();
		try {
			mapping.map(source, target, null);
		} catch (RequiredMappingException e) {
			
		}
	}

}
