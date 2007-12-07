/*
 * Copyright ${YEAR} the original author or authors.
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

package org.springframework.ws.soap.server.endpoint;

import java.io.IOException;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import junit.framework.TestCase;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class FaultCreatingValidatingMarshallingPayloadEndpointTest extends TestCase {

    private MessageContext messageContext;

    private ResourceBundleMessageSource messageSource;

    protected void setUp() throws Exception {
        this.messageSource = new ResourceBundleMessageSource();
        this.messageSource.setBasename("org.springframework.ws.soap.server.endpoint.messages");
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage request = messageFactory.createMessage();
        request.getSOAPBody().addBodyElement(new QName("http://www.springframework.org/spring-ws", "request"));
        messageContext =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
    }

    public void testValidationIncorrect() throws Exception {
        Person p = new Person("", -1);
        PersonMarshaller marshaller = new PersonMarshaller(p);

        AbstractFaultCreatingValidatingMarshallingPayloadEndpoint endpoint =
                new AbstractFaultCreatingValidatingMarshallingPayloadEndpoint() {

                    protected Object invokeInternal(Object requestObject) throws Exception {
                        fail("No expected");
                        return null;
                    }
                };
        endpoint.setValidator(new PersonValidator());
        endpoint.setMessageSource(messageSource);
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(marshaller);

        endpoint.invoke(messageContext);

        SOAPMessage response = ((SaajSoapMessage) messageContext.getResponse()).getSaajMessage();
        assertTrue("Response has no fault", response.getSOAPBody().hasFault());
        SOAPFault fault = response.getSOAPBody().getFault();
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"),
                fault.getFaultCodeAsQName());
        assertEquals("Invalid fault string", endpoint.getFaultStringOrReason(), fault.getFaultString());
        Detail detail = fault.getDetail();
        assertNotNull("No detail", detail);
        Iterator iterator = detail.getDetailEntries();
        assertTrue("No detail entry", iterator.hasNext());
        DetailEntry detailEntry = (DetailEntry) iterator.next();
        assertEquals("Invalid detail entry name", new QName("http://springframework.org/spring-ws", "ValidationError"),
                detailEntry.getElementQName());
        assertEquals("Invalid detail entry text", "Name is required", detailEntry.getTextContent());
        assertTrue("No detail entry", iterator.hasNext());
        detailEntry = (DetailEntry) iterator.next();
        assertEquals("Invalid detail entry name", new QName("http://springframework.org/spring-ws", "ValidationError"),
                detailEntry.getElementQName());
        assertEquals("Invalid detail entry text", "Age Cannot be negative", detailEntry.getTextContent());
        assertFalse("Too many detail entries", iterator.hasNext());
    }

    public void testValidationCorrect() throws Exception {
        Person p = new Person("John", 42);
        PersonMarshaller marshaller = new PersonMarshaller(p);
        AbstractFaultCreatingValidatingMarshallingPayloadEndpoint endpoint =
                new AbstractFaultCreatingValidatingMarshallingPayloadEndpoint() {

                    protected Object invokeInternal(Object requestObject) throws Exception {
                        return null;
                    }
                };
        endpoint.setValidator(new PersonValidator());
        endpoint.setMessageSource(messageSource);
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(marshaller);

        endpoint.invoke(messageContext);

        SOAPMessage response = ((SaajSoapMessage) messageContext.getResponse()).getSaajMessage();
        assertFalse("Response has fault", response.getSOAPBody().hasFault());
    }

    private static class PersonValidator implements Validator {

        public boolean supports(Class clazz) {
            return Person.class.equals(clazz);
        }

        public void validate(Object obj, Errors e) {
            ValidationUtils.rejectIfEmpty(e, "name", "name.empty");
            Person p = (Person) obj;
            if (p.getAge() < 0) {
                e.rejectValue("age", "age.negativevalue");
            }
            else if (p.getAge() > 110) {
                e.rejectValue("age", "too.darn.old");
            }
        }
    }

    private static class Person {

        private String name;

        private int age;

        private Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String toString() {
            return "Person{" + name + "," + age + "}";
        }
    }

    private static class PersonMarshaller implements Unmarshaller, Marshaller {

        private final Person person;

        private PersonMarshaller(Person person) {
            this.person = person;
        }

        public Object unmarshal(Source source) throws XmlMappingException, IOException {
            return person;
        }

        public boolean supports(Class clazz) {
            return Person.class.equals(clazz);
        }

        public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
        }
    }

}