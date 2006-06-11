CREATE DATABASE airline;
GRANT ALL ON DATABASE airline to airline;

CREATE TABLE AIRPORT (
	CODE CHAR(3) NOT NULL PRIMARY KEY,
	NAME VARCHAR(20) NOT NULL,
	CITY VARCHAR(20) NOT NULL
);

CREATE TABLE FLIGHT (
	ID BIGSERIAL NOT NULL PRIMARY KEY,
	NUMBER VARCHAR(20) NOT NULL,
	DEPARTURE_TIME TIMESTAMP NOT NULL,
	FROM_AIRPORT_CODE CHAR(3) NOT NULL REFERENCES AIRPORT(CODE),
	ARRIVAL_TIME TIMESTAMP NOT NULL,
	TO_AIRPORT_CODE CHAR(3) NOT NULL REFERENCES AIRPORT(CODE),
	SERVICE_CLASS VARCHAR(10) NOT NULL,
	SEATS_AVAILABLE INT NOT NULL
);

CREATE TABLE TICKET (
	ID BIGSERIAL NOT NULL PRIMARY KEY,
	ISSUE_DATE DATE NOT NULL,
	FLIGHT_ID INT NOT NULL REFERENCES FLIGHT(ID)
);

CREATE TABLE PASSENGER (
	 ID BIGSERIAL NOT NULL PRIMARY KEY,
	 FIRST_NAME VARCHAR(30),
	 LAST_NAME VARCHAR(30),
	 TICKET_ID INT NOT NULL REFERENCES TICKET(ID)
);
