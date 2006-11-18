/*
 * Copyright 2002-2005 the original author or authors.
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


import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.service.PersonService;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Controller implementation that handles edit of a person.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class EditForm extends SimpleFormController {

    private PersonService personService;

    protected Object formBackingObject(HttpServletRequest request)
            throws Exception {
        return personService.findByPrimaryKey(request.getParameter("country"),
                request.getParameter("company"), request.getParameter("name"));
    }

    protected void doSubmitAction(Object command) throws Exception {
        Person person = (Person) command;
        personService.update(person);
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
    
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(String[].class, new StringArrayPropertyEditor());
    }
}
