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
	ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,  
	VERSION BIGINT,  
	JOB_NAME VARCHAR(100) NOT NULL , 
	JOB_STREAM VARCHAR(20) , 
	SCHEDULE_DATE DATE ,
	JOB_RUN CHAR(2),
	STATUS VARCHAR(10) );

CREATE TABLE BATCH_JOB_EXECUTION  (
	ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
	VERSION BIGINT,  
	JOB_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,
	STATUS VARCHAR(10),
	EXIT_CODE VARCHAR(250));

CREATE TABLE BATCH_STEP  (
	ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
	VERSION BIGINT,  
	JOB_ID BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	STATUS VARCHAR(10),
	RESTART_DATA VARCHAR(200));

CREATE TABLE BATCH_STEP_EXECUTION  (
	ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
	VERSION BIGINT NOT NULL,  
	STEP_ID BIGINT NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,  
	STATUS VARCHAR(10),
	COMMIT_COUNT BIGINT , 
	TASK_COUNT BIGINT , 
	TASK_STATISTICS VARCHAR(250),
	EXIT_CODE VARCHAR(250),
	EXIT_MESSAGE VARCHAR(250));

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_STEP_SEQ;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_SEQ;
