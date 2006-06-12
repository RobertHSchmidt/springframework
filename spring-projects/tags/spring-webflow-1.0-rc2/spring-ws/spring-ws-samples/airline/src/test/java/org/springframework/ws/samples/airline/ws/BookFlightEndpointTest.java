/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.samples.airline.ws;

import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.MockControl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;

public class BookFlightEndpointTest extends XMLTestCase {

    private BookFlightEndpoint endpoint;

    private Element requestElement;

    private MockControl serviceControl;

    private AirlineService serviceMock;

    private DateTime departure;

    private List passengers;

    private Ticket ticket;

    private Document responseDocument;

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        endpoint = new BookFlightEndpoint();
        serviceControl = MockControl.createControl(AirlineService.class);
        serviceMock = (AirlineService) serviceControl.getMock();
        endpoint.setAirlineService(serviceMock);
        endpoint.afterPropertiesSet();
        SAXBuilder saxBuilder = new SAXBuilder();
        Document requestDocument = saxBuilder.build(getClass().getResourceAsStream("bookFlightRequest.xml"));
        responseDocument = saxBuilder.build(getClass().getResourceAsStream("bookFlightResponse.xml"));
        requestElement = requestDocument.getRootElement();
        departure = new DateTime(2006, 1, 1, 0, 0, 0, 0);
        DateTime arrival = new DateTime(2006, 2, 2, 0, 0, 0, 0);
        Passenger passenger = new Passenger("John", "Doe");
        passengers = new ArrayList();
        passengers.add(passenger);
        Flight flight = new Flight();
        flight.setNumber("EF1234");
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        Airport from = new Airport("ABC", "Airport", "City");
        Airport to = new Airport("DEF", "Airport", "City");
        flight.setFrom(from);
        flight.setTo(to);
        flight.setServiceClass(ServiceClass.ECONOMY);
        ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setIssueDate(new YearMonthDay(2006, 1, 1));
        ticket.addPassenger(passenger);
    }

    public void testInvoke() throws Exception {
        serviceControl.expectAndReturn(serviceMock.bookFlight("EF1234", departure, passengers), ticket);
        serviceControl.replay();
        Element result = endpoint.invokeInternal(requestElement);
        assertNotNull("Invalid result", result);
        Document resultDocument = new Document();
        resultDocument.setRootElement(result);
        XMLOutputter outputter = new XMLOutputter();
        assertXMLEqual(outputter.outputString(responseDocument), outputter.outputString(resultDocument));
        serviceControl.verify();
    }


}