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

import org.springframework.binding.mapping.Mapping;
import org.springframework.webflow.Action;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.builder.AbstractFlowBuilder;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowBuilderException;
import org.springframework.webflow.samples.phonebook.domain.SearchCriteria;
import org.springframework.webflow.samples.phonebook.domain.SearchCriteriaValidator;
import org.springframework.webflow.support.ParameterizableFlowAttributeMapper;

/**
 * Java-based flow builder that searches for people in the phonebook. The flow
 * defined by this class is exactly the same as that defined in the
 * <code>search.xml</code> XML flow definition.
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

	public SearchPersonFlowBuilder(FlowArtifactFactory flowArtifactFactory) {
		super(flowArtifactFactory);
	}

	public void buildStates() throws FlowBuilderException {
		// view search criteria
		Action searchFormAction = createSearchFormAction();
		ViewState displayCriteria = addViewState(DISPLAY_CRITERIA, "searchCriteria", on("search", EXECUTE_SEARCH,
				beforeExecute(method("bindAndValidate", searchFormAction))));
		displayCriteria.addEntryAction(method("setupForm", searchFormAction));

		// execute query
		addActionState(EXECUTE_SEARCH, method("search(${flowScope.searchCriteria})", action("phonebook")),
				new Transition[] { on(success(), DISPLAY_RESULTS) });

		// view results
		addViewState(DISPLAY_RESULTS, "searchResults", new Transition[] { on("newSearch", DISPLAY_CRITERIA),
				on(select(), BROWSE_DETAILS) });

		// view details for selected user id
		ParameterizableFlowAttributeMapper idMapper = new ParameterizableFlowAttributeMapper();
		idMapper.setInputMapping(new Mapping("externalContext.requestParameterMap.id", "id", fromStringTo(Long.class)));
		addSubflowState(BROWSE_DETAILS, flow("detail"), idMapper, new Transition[] { on(finish(), EXECUTE_SEARCH), });

		// end - an error occured
		addEndState(error(), "error");
	}

	protected FormAction createSearchFormAction() {
		FormAction action = new FormAction(SearchCriteria.class);
		action.setFormObjectScope(ScopeType.FLOW);
		action.setValidator(new SearchCriteriaValidator());
		return action;
	}
}