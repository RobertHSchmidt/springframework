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
	ID NUMBER(38)  PRIMARY KEY ,  
	VERSION NUMBER(38),  
	JOB_NAME VARCHAR(100) NOT NULL , 
	JOB_KEY VARCHAR(20) , 
	SCHEDULE_DATE DATE ,
	STATUS VARCHAR(10) );

CREATE TABLE BATCH_JOB_EXECUTION  (
	ID NUMBER(38)  PRIMARY KEY ,
	VERSION NUMBER(38),  
	JOB_ID NUMBER(38) NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,
	STATUS VARCHAR(10),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(250));

CREATE TABLE BATCH_STEP  (
	ID NUMBER(38)  PRIMARY KEY ,
	VERSION NUMBER(38),  
	JOB_ID NUMBER(38) NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	STATUS VARCHAR(10),
	RESTART_DATA VARCHAR(200));

CREATE TABLE BATCH_STEP_EXECUTION  (
	ID NUMBER(38)  PRIMARY KEY ,
	VERSION NUMBER(38) NOT NULL,  
	STEP_ID NUMBER(38) NOT NULL,
	JOB_EXECUTION_ID NUMBER(38) NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,  
	STATUS VARCHAR(10),
	COMMIT_COUNT NUMBER(38) , 
	TASK_COUNT NUMBER(38) , 
	TASK_STATISTICS VARCHAR(250),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(250));

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_STEP_SEQ;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_SEQ;
