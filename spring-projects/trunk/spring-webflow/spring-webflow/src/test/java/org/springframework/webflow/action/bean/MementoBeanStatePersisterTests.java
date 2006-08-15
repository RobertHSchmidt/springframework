package org.springframework.webflow.action.bean;

import junit.framework.TestCase;

import org.springframework.webflow.TestBean;
import org.springframework.webflow.action.bean.Memento;
import org.springframework.webflow.action.bean.MementoBeanStatePersister;
import org.springframework.webflow.action.bean.MementoOriginator;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.test.engine.MockRequestContext;

public class MementoBeanStatePersisterTests extends TestCase {
	private MementoBeanStatePersister persister = new MementoBeanStatePersister();

	private MockRequestContext context = new MockRequestContext();

	public void testSaveState() {
		context.setAttribute("bean", "testBean");
		TestMementoOriginator bean = new TestMementoOriginator();
		bean.datum1 = "3";
		persister.saveState(bean, context);
		assertNotNull(context.getFlowScope().get("memento.testBean"));
	}

	public void testRestoreState() {
		testSaveState();
		TestMementoOriginator bean = new TestMementoOriginator();
		persister.restoreState(bean, context);
		assertEquals("3", bean.datum1);
	}

	public void testCustomScope() {
		context.setAttribute("bean", "testBean");
		persister.setScope(ScopeType.CONVERSATION);
		TestMementoOriginator bean = new TestMementoOriginator();
		persister.saveState(bean, context);
		assertNotNull(context.getConversationScope().get("memento.testBean"));
	}

	public void testNoBeanName() {
		try {
			persister.saveState(new TestMementoOriginator(), context);
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	class TestMementoOriginator extends TestBean implements MementoOriginator {
		public Memento createMemento() {
			return new DatumMemento();
		}

		public void setMemento(Memento memento) {
			datum1 = ((DatumMemento)memento).getDatum1();
		}

		class DatumMemento extends Memento {
			public String getDatum1() {
				return datum1;
			}
		}
	}
}