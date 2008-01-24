-- Autogenerated: do not edit this file
DROP TABLE  BATCH_STEP_EXECUTION ;
DROP TABLE  BATCH_JOB_EXECUTION ;
DROP TABLE  BATCH_STEP_INSTANCE ;
DROP TABLE  BATCH_JOB_INSTANCE ;
DROP TABLE  BATCH_JOB_INSTANCE_PROPERTIES ;

DROP SEQUENCE  BATCH_STEP_EXECUTION_SEQ ;
DROP SEQUENCE  BATCH_STEP_SEQ ;
DROP SEQUENCE  BATCH_JOB_EXECUTION_SEQ ;
DROP SEQUENCE  BATCH_JOB_SEQ ;

-- Autogenerated: do not edit this file
CREATE TABLE BATCH_JOB_INSTANCE  (
	ID BIGINT  PRIMARY KEY ,  
	VERSION BIGINT,  
	JOB_NAME VARCHAR(100) NOT NULL , 
	JOB_KEY VARCHAR(250) , 
	STATUS VARCHAR(10) );

CREATE TABLE BATCH_JOB_EXECUTION  (
	ID BIGINT  PRIMARY KEY ,
	VERSION BIGINT,  
	JOB_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,
	STATUS VARCHAR(10),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(2500));
	
CREATE TABLE BATCH_JOB_INSTANCE_PROPERTIES  (
	JOB_ID BIGINT NOT NULL ,
    TYPE_CD VARCHAR(6) NOT NULL ,
	KEY VARCHAR(100) NOT NULL , 
	STRING_VAL VARCHAR(250) , 
	DATE_VAL TIMESTAMP ,
	LONG_VAL VARCHAR(10) );

CREATE TABLE BATCH_STEP_INSTANCE  (
	ID BIGINT  PRIMARY KEY ,
	VERSION BIGINT,  
	JOB_ID BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	STATUS VARCHAR(10),
	RESTART_DATA VARCHAR(2500));

CREATE TABLE BATCH_STEP_EXECUTION  (
	ID BIGINT  PRIMARY KEY ,
	VERSION BIGINT NOT NULL,  
	STEP_ID BIGINT NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,  
	STATUS VARCHAR(10),
	COMMIT_COUNT BIGINT , 
	TASK_COUNT BIGINT , 
	TASK_STATISTICS VARCHAR(2500),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(2500));

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_STEP_SEQ;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_SEQ;
