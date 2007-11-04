package org.springframework.config.java.valuesource;

import junit.framework.TestCase;

public class AbstractStringBasedPropertySourceTests extends TestCase {
	
	class Foo extends AbstractStringBasedPropertySource {
		@Override
		public String getString(String name) throws PropertyDefinitionException {
			// TODO Auto-generated method stub
			return "foo";
		}
	}
	
	public void testSimpleStringBasedProperty() {
		Foo f = new Foo();
		String s = f.resolve("frog", String.class);
		//Object o = f.resolve("woeirowieur", Object.class);
	}
	

}
