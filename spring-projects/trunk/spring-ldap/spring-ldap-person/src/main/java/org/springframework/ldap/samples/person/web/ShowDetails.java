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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.service.PersonService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Controller implementation that retrieves the details of a person.
 * 
 * @author Ulrik Sandberg
 */
public class ShowDetails implements Controller {

    PersonService personService;

    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String name = request.getParameter("name");
        String company = request.getParameter("company");
        String country = request.getParameter("country");
        Person person = personService.findByPrimaryKey(country, company, name);
        return new ModelAndView("showDetails", "person", person);
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
