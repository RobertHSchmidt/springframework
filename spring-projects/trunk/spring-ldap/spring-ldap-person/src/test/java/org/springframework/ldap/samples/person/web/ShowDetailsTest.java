/*
 * Copyright 2005-2006 the original author or authors.
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
package org.springframework.ldap.samples.person.web;


import org.easymock.MockControl;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.service.PersonService;
import org.springframework.ldap.samples.person.web.ShowDetails;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import junit.framework.TestCase;

/**
 * Unit tests for the ShowDetails class.
 * 
 * @author Ulrik Sandberg
 */
public class ShowDetailsTest extends TestCase {

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

    public void testHandleRequest() throws Exception {
        Person expectedPerson = new Person();
        personServiceControl.expectAndReturn(personServiceMock
                .findByPrimaryKey("Some country", "Some company", "Some name"),
                expectedPerson);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addParameter("name", "Some name");
        mockHttpServletRequest.addParameter("company", "Some company");
        mockHttpServletRequest.addParameter("country", "Some country");
        ShowDetails tested = new ShowDetails();
        tested.setPersonService(personServiceMock);

        replay();

        ModelAndView result = tested
                .handleRequest(mockHttpServletRequest, null);

        verify();
        assertEquals("showDetails", result.getViewName());
        Person person = (Person) result.getModel().get("person");
        assertNotNull(person);
        assertSame(expectedPerson, person);
    }
}
