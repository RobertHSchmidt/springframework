-- Autogenerated: do not edit this file
DROP TABLE  BATCH_EXECUTION_CONTEXT ;
DROP TABLE  BATCH_STEP_EXECUTION ;
DROP TABLE  BATCH_JOB_EXECUTION ;
DROP TABLE  BATCH_JOB_PARAMS ;
DROP TABLE  BATCH_JOB_INSTANCE ;

DROP SEQUENCE  BATCH_STEP_EXECUTION_SEQ ;
DROP SEQUENCE  BATCH_JOB_EXECUTION_SEQ ;
DROP SEQUENCE  BATCH_JOB_SEQ ;

-- Autogenerated: do not edit this file
CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID NUMBER(38)  PRIMARY KEY ,  
	VERSION NUMBER(38),  
	JOB_NAME VARCHAR2(100) NOT NULL , 
	JOB_KEY VARCHAR2(2500)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID NUMBER(38)  PRIMARY KEY ,
	VERSION NUMBER(38),  
	JOB_INSTANCE_ID NUMBER(38) NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL, 
	END_TIME TIMESTAMP DEFAULT NULL,
	STATUS VARCHAR2(10),
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR2(20),
	EXIT_MESSAGE VARCHAR2(2500),
	constraint JOB_INSTANCE_EXECUTION_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;
	
CREATE TABLE BATCH_JOB_PARAMS  (
	JOB_INSTANCE_ID NUMBER(38) NOT NULL ,
    TYPE_CD VARCHAR2(6) NOT NULL ,
	KEY_NAME VARCHAR2(100) NOT NULL , 
	STRING_VAL VARCHAR2(250) , 
	DATE_VAL TIMESTAMP DEFAULT NULL,
	LONG_VAL NUMBER(38) ,
	DOUBLE_VAL NUMBER,
	constraint JOB_INSTANCE_PARAMS_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;
	
CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID NUMBER(38)  PRIMARY KEY ,
	VERSION NUMBER(38) NOT NULL,  
	STEP_NAME VARCHAR2(100) NOT NULL,
	JOB_EXECUTION_ID NUMBER(38) NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP DEFAULT NULL,  
	STATUS VARCHAR2(10),
	COMMIT_COUNT NUMBER(38) , 
	ITEM_COUNT NUMBER(38) ,
	READ_SKIP_COUNT NUMBER(38) ,
	WRITE_SKIP_COUNT NUMBER(38) ,
	ROLLBACK_COUNT NUMBER(38) , 
	CONTINUABLE CHAR(1),
	EXIT_CODE VARCHAR2(20),
	EXIT_MESSAGE VARCHAR2(2500),
	constraint JOB_EXECUTION_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;
	
CREATE TABLE BATCH_EXECUTION_CONTEXT  (
	EXECUTION_ID NUMBER(38) NOT NULL ,
	DISCRIMINATOR VARCHAR2(1) NOT NULL ,
    TYPE_CD VARCHAR2(6) NOT NULL ,
	KEY_NAME VARCHAR2(1000) NOT NULL , 
	STRING_VAL VARCHAR2(1000) , 
	DATE_VAL TIMESTAMP DEFAULT NULL ,
	LONG_VAL NUMBER(38) ,
	DOUBLE_VAL NUMBER ,
    OBJECT_VAL BLOB
) ;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_SEQ;
