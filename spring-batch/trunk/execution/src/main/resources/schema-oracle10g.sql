-- Autogenerated: do not edit this file
DROP TABLE BATCH_STEP_EXECUTION ;
DROP TABLE BATCH_JOB_EXECUTION ;
DROP TABLE BATCH_STEP ;
DROP TABLE BATCH_JOB ;

DROP SEQUENCE BATCH_STEP_EXECUTION_SEQ ;
DROP SEQUENCE BATCH_STEP_SEQ ;
DROP SEQUENCE BATCH_JOB_EXECUTION_SEQ ;
DROP SEQUENCE BATCH_JOB_SEQ ;

-- Autogenerated: do not edit this file
CREATE TABLE BATCH_JOB  (
	ID INT  PRIMARY KEY ,  
	VERSION INT,  
	JOB_NAME VARCHAR(100) NOT NULL , 
	JOB_STREAM VARCHAR(20) , 
	SCHEDULE_DATE DATE ,
	JOB_RUN CHAR(2),
	STATUS VARCHAR(10) );

CREATE TABLE BATCH_JOB_EXECUTION  (
	ID INT  PRIMARY KEY ,
	VERSION INT,  
	JOB_ID INT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,
	STATUS VARCHAR(10),
	EXIT_CODE VARCHAR(250));

CREATE TABLE BATCH_STEP  (
	ID INT  PRIMARY KEY ,
	VERSION INT,  
	JOB_ID INT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	STATUS VARCHAR(10),
	RESTART_DATA VARCHAR(200));

CREATE TABLE BATCH_STEP_EXECUTION  (
	ID INT  PRIMARY KEY ,
	VERSION INT NOT NULL,  
	STEP_ID INT NOT NULL,
	JOB_EXECUTION_ID INT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,  
	STATUS VARCHAR(10),
	COMMIT_COUNT INT , 
	TASK_COUNT INT , 
	TASK_STATISTICS VARCHAR(250),
	EXIT_CODE VARCHAR(250),
	EXIT_MESSAGE VARCHAR(250));

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_STEP_SEQ;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_SEQ;
