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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

import org.springframework.util.Assert;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.dao.TicketDao;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.springframework.ws.samples.airline.service.NoSeatAvailableException;
import org.springframework.ws.samples.airline.service.NoSuchFlightException;

public class AirlineServiceImpl implements AirlineService {

    private final static Log logger = LogFactory.getLog(AirlineServiceImpl.class);

    private FlightDao flightDao;

    private TicketDao ticketDao;

    public void setFlightDao(FlightDao flightDao) {
        this.flightDao = flightDao;
    }

    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    public Ticket bookFlight(String flightNumber, DateTime departureTime, List passengers)
            throws NoSuchFlightException, NoSeatAvailableException {
        Assert.notEmpty(passengers, "No passengers given");
        if (logger.isDebugEnabled()) {
            logger.debug("Booking flight '" + flightNumber + "' on '" + departureTime + "' for " + passengers.size() +
                    " passengers");
        }
        Flight flight = flightDao.getFlight(flightNumber, departureTime);
        if (flight == null) {
            throw new NoSuchFlightException(flightNumber, departureTime);
        }
        else if (flight.getSeatsAvailable() < passengers.size()) {
            throw new NoSeatAvailableException(flight);
        }
        Ticket ticket = new Ticket();
        ticket.setIssueDate(new YearMonthDay());
        ticket.setFlight(flight);
        for (Iterator iterator = passengers.iterator(); iterator.hasNext();) {
            Passenger passenger = (Passenger) iterator.next();
            ticket.addPassenger(passenger);
        }
        updateSeatCount(flight, passengers);
        ticketDao.save(ticket);
        return ticket;
    }

    private void updateSeatCount(Flight flight, List passengers) {
        int newCount = flight.getSeatsAvailable() - passengers.size();
        flight.setSeatsAvailable(newCount);
        flightDao.update(flight);
    }

    public List getFlights(String fromAirportCode,
                           String toAirportCode,
                           YearMonthDay departureDate,
                           ServiceClass serviceClass) {
        if (serviceClass == null) {
            serviceClass = ServiceClass.ECONOMY;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Getting flights from '" + fromAirportCode + "' to '" + toAirportCode + "' on " + departureDate);
        }
        DateTime startOfPeriod = departureDate.toDateTimeAtMidnight();
        DateTime endOfPeriod = startOfPeriod.plusDays(1);
        return flightDao.findFlights(fromAirportCode, toAirportCode, startOfPeriod, endOfPeriod, serviceClass);
    }

}
