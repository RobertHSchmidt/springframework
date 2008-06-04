-- Autogenerated: do not edit this file
DROP TABLE  BATCH_STEP_EXECUTION_CONTEXT ;
DROP TABLE  BATCH_STEP_EXECUTION ;
DROP TABLE  BATCH_JOB_EXECUTION ;
DROP TABLE  BATCH_JOB_PARAMS ;
DROP TABLE  BATCH_JOB_INSTANCE ;

DROP TABLE  BATCH_STEP_EXECUTION_SEQ ;
DROP TABLE  BATCH_JOB_EXECUTION_SEQ ;
DROP TABLE  BATCH_JOB_SEQ ;

-- Autogenerated: do not edit this file
CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,  
	VERSION BIGINT,  
	JOB_NAME VARCHAR(100) NOT NULL , 
	JOB_KEY VARCHAR(2500)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
	VERSION BIGINT,  
	JOB_INSTANCE_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL, 
	END_TIME TIMESTAMP DEFAULT NULL,
	STATUS VARCHAR(10),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(2500),
	constraint JOB_INSTANCE_EXECUTION_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;
	
CREATE TABLE BATCH_JOB_PARAMS  (
	JOB_INSTANCE_ID BIGINT NOT NULL ,
    TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(100) NOT NULL , 
	STRING_VAL VARCHAR(250) , 
	DATE_VAL TIMESTAMP DEFAULT NULL,
	LONG_VAL BIGINT ,
	DOUBLE_VAL DOUBLE PRECISION,
	constraint JOB_INSTANCE_PARAMS_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;
	
CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
	VERSION BIGINT NOT NULL,  
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP DEFAULT NULL,  
	STATUS VARCHAR(10),
	COMMIT_COUNT BIGINT , 
	ITEM_COUNT BIGINT , 
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR(20),
	EXIT_MESSAGE VARCHAR(2500),
	constraint JOB_EXECUTION_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;
	
CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL ,
    TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(1000) NOT NULL , 
	STRING_VAL VARCHAR(1000) , 
	DATE_VAL TIMESTAMP DEFAULT NULL ,
	LONG_VAL BIGINT ,
	DOUBLE_VAL DOUBLE PRECISION ,
    OBJECT_VAL BLOB,
	constraint STEP_EXECUTION_CONTEXT_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY, DUMMY VARCHAR(1));
CREATE TABLE BATCH_JOB_EXECUTION_SEQ (ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY, DUMMY VARCHAR(1));
CREATE TABLE BATCH_JOB_SEQ (ID BIGINT  PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY, DUMMY VARCHAR(1));
