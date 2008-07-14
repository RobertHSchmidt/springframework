-- Autogenerated: do not edit this file

-- Upgrades an existing 1.0 schema to version 1.1.

CREATE TABLE BATCH_EXECUTION_CONTEXT  (
	EXECUTION_ID BIGINT NOT NULL,
	DISCRIMINATOR VARCHAR(1) NOT NULL,
	TYPE_CD VARCHAR(6) NOT NULL,
	KEY_NAME VARCHAR(1000) NOT NULL, 
	STRING_VAL VARCHAR(1000) , 
	DATE_VAL TIMESTAMP DEFAULT NULL,
	LONG_VAL BIGINT ,
	DOUBLE_VAL DOUBLE PRECISION ,
	OBJECT_VAL BLOB 
) ;

INSERT INTO BATCH_EXECUTION_CONTEXT (EXECUTION_ID, DISCRIMINATOR, TYPE_CD, KEY_NAME, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, OBJECT_VAL)
SELECT STEP_EXECUTION_ID, 'S', TYPE_CD, KEY_NAME, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, OBJECT_VAL FROM BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE BATCH_STEP_EXECUTION_CONTEXT;
ALTER TABLE BATCH_JOB_EXECUTION ADD CREATE_TIME TIMESTAMP;
UPDATE BATCH_JOB_EXECUTION SET CREATE_TIME = START_TIME;
ALTER TABLE BATCH_JOB_EXECUTION ALTER COLUMN CREATE_TIME SET NOT NULL;
ALTER TABLE BATCH_STEP_EXECUTION ADD READ_SKIP_COUNT BIGINT;
ALTER TABLE BATCH_STEP_EXECUTION ADD WRITE_SKIP_COUNT BIGINT;
ALTER TABLE BATCH_STEP_EXECUTION ADD ROLLBACK_COUNT BIGINT;  
