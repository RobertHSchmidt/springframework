package org.springframework.ldap.samples.person.web;

import java.util.Collections;

import org.easymock.MockControl;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.MappingContext;
import org.springframework.ldap.samples.person.dao.PersonDaoImpl;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;
import org.springframework.ldap.samples.person.service.PersonService;
import org.springframework.ldap.samples.person.service.PersonServiceImpl;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.test.MockFlowServiceLocator;
import org.springframework.webflow.test.MockParameterMap;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

public class SearchFlowExecutionTest extends AbstractXmlFlowExecutionTests {

    private MockControl personServiceControl;

    private PersonService personServiceMock;

    protected void setUp() throws Exception {
        super.setUp();

        personServiceControl = MockControl.createControl(PersonService.class);
        personServiceMock = (PersonService) personServiceControl.getMock();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        personServiceControl = null;
        personServiceMock = null;
    }

    protected void replay() {
        personServiceControl.replay();
    }

    protected void verify() {
        personServiceControl.verify();
    }

    public void testStartFlow() {
        ApplicationView view = applicationView(startFlow());
        assertCurrentStateEquals("enterCriteria");
        assertViewNameEquals("searchForm", view);
        assertModelAttributeNotNull("searchCriteria", view);
    }

    public void testCriteriaSubmitSuccess() {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setName("Keith");
        personServiceControl.expectAndReturn(personServiceMock
                .find(searchCriteria), Collections.singletonList(new Person()));
        replay();

        startFlow();
        MockParameterMap parameters = new MockParameterMap();
        parameters.put("name", "Keith");
        ApplicationView view = applicationView(signalEvent("search", parameters));

        verify();

        assertCurrentStateEquals("displayResults");
        assertViewNameEquals("searchResult", view);
        assertModelAttributeCollectionSize(1, "results", view);
    }

    public void testCriteriaSubmitError() {
        startFlow();
        signalEvent("search");
        assertCurrentStateEquals("enterCriteria");
    }

    public void testNewSearch() {
        testCriteriaSubmitSuccess();
        ApplicationView view = applicationView(signalEvent("newSearch"));
        assertCurrentStateEquals("enterCriteria");
        assertViewNameEquals("searchForm", view);
    }

    public void testSelectValidResult() {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setName("Keith");
        personServiceControl.expectAndReturn(personServiceMock
                .find(searchCriteria), Collections.singletonList(new Person()),
                2);
        replay();

        startFlow();
        MockParameterMap parameters = new MockParameterMap();
        parameters.put("name", "Keith");
        ApplicationView view = applicationView(signalEvent("search", parameters));

        assertCurrentStateEquals("displayResults");
        assertViewNameEquals("searchResult", view);
        assertModelAttributeCollectionSize(1, "results", view);

        parameters = new MockParameterMap();
        parameters.put("name", "Keith Donald");
        parameters.put("company", "company1");
        parameters.put("country", "Sweden");
        view = applicationView(signalEvent("select", parameters));

        verify();

        assertCurrentStateEquals("displayResults");
        assertViewNameEquals("searchResult", view);
        assertModelAttributeCollectionSize(1, "results", view);
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#getFlowDefinitionResource()
     */
    protected FlowDefinitionResource getFlowDefinitionResource() {
        return createFlowDefinitionResource("src/main/webapp/WEB-INF/flows/search-flow.xml");
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#registerMockServices(org.springframework.webflow.test.MockFlowServiceLocator)
     */
    protected void registerMockServices(MockFlowServiceLocator serviceRegistry) {
        Flow mockDetailFlow = new Flow("detail-flow");
        mockDetailFlow.setInputMapper(new AttributeMapper() {
            public void map(Object source, Object target, MappingContext context) {
                String name = "Keith Donald";
                String company = "company1";
                String country = "Sweden";
                assertEquals("name '" + name
                        + "' not provided as input by calling search flow",
                        name, ((AttributeMap) source).get("name"));
                assertEquals("company '" + company
                        + "' not provided as input by calling search flow",
                        company, ((AttributeMap) source).get("company"));
                assertEquals("company '" + country
                        + "' not provided as input by calling search flow",
                        country, ((AttributeMap) source).get("country"));
            }
        });
        // test responding to finish result
        new EndState(mockDetailFlow, "finish");

        serviceRegistry.registerSubflow(mockDetailFlow);
        serviceRegistry.registerBean("personService", personServiceMock);
    }
}