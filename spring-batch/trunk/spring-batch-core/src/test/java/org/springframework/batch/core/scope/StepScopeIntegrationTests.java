package org.springframework.batch.core.scope;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class StepScopeIntegrationTests {
	
	@Autowired
	@Qualifier("vanilla")
	private Step vanilla;
	
	@Autowired
	@Qualifier("proxied")
	private Step proxied;

	@Autowired
	@Qualifier("enhanced")
	private Step enhanced;

	@Before
	@After
	public void start() {
		TestStep.reset();
	}
		
	@Test
	public void testScopeCreation() throws Exception {
		vanilla.execute(new StepExecution("foo",new JobExecution(11L)));
		assertNotNull(TestStep.getContext());
		assertNull(StepSynchronizationManager.getContext());
	}

	@Test
	public void testScopedProxy() throws Exception {
		proxied.execute(new StepExecution("foo",new JobExecution(11L)));
		assertTrue(TestStep.getContext().attributeNames().length>0);
		String collaborator = (String) TestStep.getContext().getAttribute("collaborator");
		assertNotNull(collaborator);
		assertEquals("bar", collaborator);
	}

	@Test
	public void testExecutionContext() throws Exception {
		StepExecution stepExecution = new StepExecution("foo",new JobExecution(11L));
		ExecutionContext executionContext = new ExecutionContext();
		executionContext.put("name", "spam");
		stepExecution.setExecutionContext(executionContext);
		enhanced.execute(stepExecution);
		assertTrue(TestStep.getContext().attributeNames().length>0);
		String collaborator = (String) TestStep.getContext().getAttribute("collaborator");
		assertNotNull(collaborator);
		assertEquals("bar", collaborator);
	}

}
