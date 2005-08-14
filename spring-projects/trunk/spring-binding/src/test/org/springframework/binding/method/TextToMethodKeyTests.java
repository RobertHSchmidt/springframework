package org.springframework.binding.method;

import junit.framework.TestCase;

import org.springframework.binding.convert.support.DefaultConversionService;

public class TextToMethodKeyTests extends TestCase {

	public void testMethodKeyConversionNoArg() {
		TextToMethodKey converter = new TextToMethodKey(new DefaultConversionService());
		MethodKey key = (MethodKey)converter.convert("execute");
		assertEquals("Method key wrong", "execute", key.getMethodName());
	}

	public void testMethodKeyConversionNoArg2() {
		TextToMethodKey converter = new TextToMethodKey(new DefaultConversionService());
		MethodKey key = (MethodKey)converter.convert("execute()");
		assertEquals("Method key wrong", "execute", key.getMethodName());
	}

	public void testMethodKeyConversionWithArgs() {
		TextToMethodKey converter = new TextToMethodKey(new DefaultConversionService());
		MethodKey key = (MethodKey)converter.convert("execute(string foo, int bar)");
		assertEquals("Method key wrong", "execute", key.getMethodName());
		assertEquals("Arguments size wrong", 2, key.getArguments().size());
		assertEquals("Argument 1 name wrong", "foo", key.getArguments().getArgument(0).getName());
		assertEquals("Argument 1 type wrong", String.class, key.getArguments().getArgument(0).getType());
		assertEquals("Argument 2 name wrong", "bar", key.getArguments().getArgument(1).getName());
		assertEquals("Argument 2 type wrong", int.class, key.getArguments().getArgument(1).getType());
	}
	
	public void testMethodKeyConversionWithArgsButNoTypes() {
		TextToMethodKey converter = new TextToMethodKey(new DefaultConversionService());
		MethodKey key = (MethodKey)converter.convert("execute(foo)");
		assertEquals("Method key wrong", "execute", key.getMethodName());
		assertEquals("Arguments size wrong", 1, key.getArguments().size());
		assertEquals("Argument 1 name wrong", "foo", key.getArguments().getArgument(0).getName());
		assertEquals("Argument 1 type wrong", null, key.getArguments().getArgument(0).getType());
	}
}
