/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.ws.samples.airline.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.dao.TicketDao;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.NoSeatAvailableException;
import org.springframework.ws.samples.airline.service.NoSuchFlightException;

public class AirlineServiceImplTest extends TestCase {

    private AirlineServiceImpl airlineService;

    private MockControl flightDaoControl;

    private FlightDao flightDaoMock;

    private MockControl ticketDaoControl;

    private TicketDao ticketDaoMock;

    protected void setUp() throws Exception {
        airlineService = new AirlineServiceImpl();
        flightDaoControl = MockControl.createControl(FlightDao.class);
        flightDaoMock = (FlightDao) flightDaoControl.getMock();
        airlineService.setFlightDao(flightDaoMock);
        ticketDaoControl = MockControl.createControl(TicketDao.class);
        ticketDaoMock = (TicketDao) ticketDaoControl.getMock();
        airlineService.setTicketDao(ticketDaoMock);
    }

/*
    public void testBookFlight() {
        Flight flight = new Flight();
        flightDaoControl
                .expectAndReturn(flightDaoMock.findFlights("1234", null, null), Collections.singletonList(flight));
        Passenger customer = new Passenger();
        customerDaoControl.expectAndReturn(passengerDaoMock.getCustomer(42L), customer);
        ticketDaoMock.save(null);
        ticketDaoControl.setMatcher(MockControl.ALWAYS_MATCHER);

        customerDaoControl.replay();
        flightDaoControl.replay();
        ticketDaoControl.replay();

        airlineService.bookFlight("1234", 42L);

        customerDaoControl.verify();
        flightDaoControl.verify();
        ticketDaoControl.verify();
    }
*/

    public void testGetFlights() throws Exception {
        String toCode = "to";
        String fromCode = "from";
        YearMonthDay departureDate = new YearMonthDay(2006, 1, 31);
        Flight flight = new Flight();
        List flights = new ArrayList();
        flights.add(flight);
        DateTime startOfPeriod = new DateTime(2006, 1, 31, 0, 0, 0, 0);
        DateTime endOfPeriod = new DateTime(2006, 2, 1, 0, 0, 0, 0);
        flightDaoControl.expectAndReturn(
                flightDaoMock.findFlights(fromCode, toCode, startOfPeriod, endOfPeriod, ServiceClass.ECONOMY), flights);
        flightDaoControl.replay();
        ticketDaoControl.replay();

        List result = airlineService.getFlights(fromCode, toCode, departureDate, ServiceClass.ECONOMY);
        assertEquals("Invalid result", flights, result);

        flightDaoControl.verify();
        ticketDaoControl.verify();
    }

    public void testGetFlightsDefaultServiceClass() throws Exception {
        String toCode = "to";
        String fromCode = "from";
        YearMonthDay departureDate = new YearMonthDay(2006, 1, 31);
        Flight flight = new Flight();
        List flights = new ArrayList();
        flights.add(flight);
        DateTime startOfPeriod = new DateTime(2006, 1, 31, 0, 0, 0, 0);
        DateTime endOfPeriod = new DateTime(2006, 2, 1, 0, 0, 0, 0);
        flightDaoControl.expectAndReturn(
                flightDaoMock.findFlights(fromCode, toCode, startOfPeriod, endOfPeriod, ServiceClass.ECONOMY), flights);
        flightDaoControl.replay();
        ticketDaoControl.replay();

        List result = airlineService.getFlights(fromCode, toCode, departureDate, null);
        assertEquals("Invalid result", flights, result);

        flightDaoControl.verify();
        ticketDaoControl.verify();
    }

    public void testBookFlight() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        Passenger passenger = new Passenger("John", "Doe");
        List passengers = Collections.singletonList(passenger);
        Flight flight = new Flight();
        flight.setNumber(flightNumber);
        flight.setSeatsAvailable(10);
        flightDaoControl.expectAndReturn(flightDaoMock.getFlight(flightNumber, departureTime), flight);
        flightDaoMock.update(flight);
        ticketDaoMock.save(null);
        ticketDaoControl.setMatcher(MockControl.ALWAYS_MATCHER);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        Ticket ticket = airlineService.bookFlight(flightNumber, departureTime, passengers);
        assertNotNull("Invalid ticket", ticket);
        assertEquals("Invalid flight", flight, ticket.getFlight());
        assertEquals("Invalid passengers count", 1, ticket.getPassengers().size());
        flightDaoControl.verify();
        ticketDaoControl.verify();
    }

    public void testBookFlightNoSuchFlight() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        List passengers = Collections.singletonList(new Passenger());
        flightDaoControl.expectAndReturn(flightDaoMock.getFlight(flightNumber, departureTime), null);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        try {
            airlineService.bookFlight(flightNumber, departureTime, passengers);
            fail("Should have thrown an NoSuchFlightException");
        }
        catch (NoSuchFlightException ex) {
        }
        flightDaoControl.verify();
        ticketDaoControl.verify();

    }

    public void testBookFlightNoSeatAvailable() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        List passengers = Collections.singletonList(new Passenger());
        Flight flight = new Flight();
        flightDaoControl.expectAndReturn(flightDaoMock.getFlight(flightNumber, departureTime), flight);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        try {
            airlineService.bookFlight(flightNumber, departureTime, passengers);
            fail("Should have thrown an NoSeatAvailableException");
        }
        catch (NoSeatAvailableException ex) {
        }
        flightDaoControl.verify();
        ticketDaoControl.verify();
    }
}
