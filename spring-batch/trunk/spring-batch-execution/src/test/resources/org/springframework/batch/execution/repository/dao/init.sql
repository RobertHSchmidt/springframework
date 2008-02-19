-- Autogenerated: do not edit this file
CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT IDENTITY PRIMARY KEY ,  
	VERSION BIGINT,  
	JOB_NAME VARCHAR(100) NOT NULL , 
	JOB_KEY VARCHAR(250));

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT IDENTITY PRIMARY KEY ,
	VERSION BIGINT,  
	JOB_INSTANCE_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,
	STATUS VARCHAR(10),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(2500));
	
CREATE TABLE BATCH_JOB_PARAMS  (
	JOB_INSTANCE_ID BIGINT NOT NULL ,
    TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(100) NOT NULL , 
	STRING_VAL VARCHAR(250) , 
	DATE_VAL TIMESTAMP ,
	LONG_VAL BIGINT );
	
CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT IDENTITY PRIMARY KEY ,
	VERSION BIGINT NOT NULL,  
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP ,  
	STATUS VARCHAR(10),
	COMMIT_COUNT BIGINT , 
	TASK_COUNT BIGINT , 
	TASK_STATISTICS VARCHAR(4000),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(2500));
	
CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL ,
    TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(1000) NOT NULL , 
	STRING_VAL VARCHAR(1000) , 
	DATE_VAL TIMESTAMP ,
	LONG_VAL VARCHAR(10) ,
	DOUBLE_VAL DOUBLE PRECISION ,
    OBJECT_VAL LONGVARBINARY);

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
	ID BIGINT IDENTITY
);
CREATE TABLE BATCH_STEP_SEQ (
	ID BIGINT IDENTITY
);
CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
	ID BIGINT IDENTITY
);
CREATE TABLE BATCH_JOB_SEQ (
	ID BIGINT IDENTITY
);
