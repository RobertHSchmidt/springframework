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
package org.springframework.webflow.samples.phonebook.web;

import org.springframework.binding.support.Mapping;
import org.springframework.webflow.Transition;
import org.springframework.webflow.config.AbstractFlowBuilder;
import org.springframework.webflow.config.AutowireMode;
import org.springframework.webflow.config.FlowBuilderException;
import org.springframework.webflow.support.FlowScopeExpression;
import org.springframework.webflow.support.ParameterizableFlowAttributeMapper;

/**
 * Java-based flow builder that searches for people in the phonebook. The flow
 * defined by this class is exactly the same as that defined in the
 * "search-flow.xml" XML flow definition.
 * <p>
 * This encapsulates the page flow of searching for some people, selecting a
 * person you care about, and viewing their person's details and those of their
 * collegues in a reusable, self-contained module.
 * 
 * @author Keith Donald
 */
public class SearchPersonFlowBuilder extends AbstractFlowBuilder {

	private static final String DISPLAY_CRITERIA = "displayCriteria";

	private static final String EXECUTE_SEARCH = "executeSearch";

	private static final String DISPLAY_RESULTS = "displayResults";

	private static final String BROWSE_DETAILS = "browseDetails";

	protected String flowId() {
		return "searchFlow";
	}

	public void buildStates() throws FlowBuilderException {
		// view search criteria
		addViewState(DISPLAY_CRITERIA, "searchCriteria", on("search", EXECUTE_SEARCH,
				beforeExecute(method("bindAndValidate", action("searchFormAction")))));

		// execute query
		addActionState(EXECUTE_SEARCH, action(SearchPhoneBookAction.class, AutowireMode.CONSTRUCTOR), new Transition[] {
				on(error(), DISPLAY_CRITERIA), on(success(), DISPLAY_RESULTS) });

		// view results
		addViewState(DISPLAY_RESULTS, "searchResults", new Transition[] { on("newSearch", DISPLAY_CRITERIA),
				on(select(), BROWSE_DETAILS) });

		// view details for selected user id
		ParameterizableFlowAttributeMapper idMapper = new ParameterizableFlowAttributeMapper();
		idMapper.setInputMapping(new Mapping("sourceEvent.parameters.id", "id", fromStringTo(Long.class)));
		addSubFlowState(BROWSE_DETAILS, flow("detailFlow"), idMapper,
				new Transition[] { on(finish(), EXECUTE_SEARCH), on(error(), "error") });

		// end - an error occured
		addEndState(error(), "error");
	}
}