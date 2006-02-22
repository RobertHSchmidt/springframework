/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.samples.phonebook.webflow;

import java.io.File;

import org.springframework.binding.attribute.AttributeMap;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Action;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowArtifactFactoryAdapter;
import org.springframework.webflow.builder.FlowArtifactParameters;
import org.springframework.webflow.registry.ExternalizedFlowDefinition;
import org.springframework.webflow.samples.phonebook.domain.ArrayListPhoneBook;
import org.springframework.webflow.samples.phonebook.domain.PhoneBook;
import org.springframework.webflow.test.AbstractXmlFlowExecutionTests;

public class SearchFlowExecutionTests extends AbstractXmlFlowExecutionTests {

	public void testStartFlow() {
		ViewSelection view = startFlow();
		assertCurrentStateEquals("enterCriteria");
		assertViewNameEquals("searchCriteria", view);
		assertModelAttributeNotNull("searchCriteria", view);
	}

	public void testCriteriaSubmitSuccess() {
		startFlow();
		AttributeMap parameters = new AttributeMap();
		parameters.setAttribute("firstName", "Keith");
		parameters.setAttribute("lastName", "Donald");
		ViewSelection view = signalEvent("search", parameters);
		assertCurrentStateEquals("displayResults");
		assertViewNameEquals("searchResults", view);
		assertModelAttributeCollectionSize(1, "results", view);
	}

	public void testCriteriaSubmitError() {
		startFlow();
		signalEvent("search");
		assertCurrentStateEquals("enterCriteria");
	}

	public void testNewSearch() {
		testCriteriaSubmitSuccess();
		ViewSelection view = signalEvent("newSearch");
		assertCurrentStateEquals("enterCriteria");
		assertViewNameEquals("searchCriteria", view);
	}

	public void testSelectValidResult() {
		testCriteriaSubmitSuccess();
		AttributeMap parameters = new AttributeMap();
		parameters.setAttribute("id", "1");
		ViewSelection view = signalEvent("select", parameters);
		assertCurrentStateEquals("displayResults");
		assertViewNameEquals("searchResults", view);
		assertModelAttributeCollectionSize(1, "results", view);
	}

	/**
	 * A stub for testing.
	 */
	private PhoneBook phonebook = new ArrayListPhoneBook();

	protected ExternalizedFlowDefinition getFlowDefinition() {
		File flowDir = new File("src/webapp/WEB-INF/flows");
		Resource resource = new FileSystemResource(new File(flowDir, "search-flow.xml"));
		return new ExternalizedFlowDefinition(resource);
	}

	protected FlowArtifactFactory createFlowArtifactFactory() {
		return new TestFlowArtifactFactory();
	}

	/**
	 * Used to wire in test implementations of artifacts used by the flow, such
	 * as the phonebook and the detail subflow
	 */
	protected class TestFlowArtifactFactory extends FlowArtifactFactoryAdapter {
		public Action getAction(FlowArtifactParameters parameters) throws FlowArtifactException {
			// there is only one global action in this flow and its always the
			// same
			return new LocalBeanInvokingAction(phonebook);
		}

		public Flow getSubflow(String id) throws FlowArtifactException {
			Flow detail = new Flow(id);
			// test responding to finish result
			EndState finish = new EndState(detail, "finish");
			finish.addEntryAction(new AbstractAction() {
				public Event doExecute(RequestContext context) throws Exception {
					// test attribute mapping
					assertEquals(new Long(1), context.getFlowScope().getAttribute("id"));
					return success();
				}
			});
			return detail;
		}
	}
}