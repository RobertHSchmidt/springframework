package org.springframework.batch.item.processor;

import java.util.List;

import org.springframework.batch.io.sample.domain.Foo;
import org.springframework.batch.io.sample.domain.FooService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests for {@link PropertyExtractingDelegatingItemProcessor}
 * 
 * @author Robert Kasanicky
 */
public class PropertyExtractingDelegatingItemProccessorIntegrationTests 
	extends AbstractDependencyInjectionSpringContextTests {
	
	private PropertyExtractingDelegatingItemProcessor processor;
	
	private FooService fooService;
	
	protected String getConfigPath() {
		return "pe-delegating-item-processor.xml";
	}
	
	/**
	 * Regular usage scenario - input object should be passed to
	 * the service the injected invoker points to.
	 */
	public void testProcess() throws Exception {
		Foo foo;
		while ((foo = fooService.generateFoo()) != null) {
			processor.process(foo);
		}
		
		List input = fooService.getGeneratedFoos();
		List processed = fooService.getProcessedFooNameValuePairs();
		assertEquals(input.size(), processed.size());
		assertFalse(fooService.getProcessedFooNameValuePairs().isEmpty());
		
		for (int i = 0; i < input.size(); i++) {
			Foo inputFoo = (Foo) input.get(i);
			Foo outputFoo = (Foo) processed.get(i);
			assertEquals(inputFoo.getName(), outputFoo.getName());
			assertEquals(inputFoo.getValue(), outputFoo.getValue());
			assertEquals(0, outputFoo.getId());
		}
		
	}

	public void setProcessor(PropertyExtractingDelegatingItemProcessor processor) {
		this.processor = processor;
	}

	public void setFooService(FooService fooService) {
		this.fooService = fooService;
	}

}
