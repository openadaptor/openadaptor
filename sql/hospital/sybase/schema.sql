/**
*** Sybase Message Exceptions Schema
***
**/

if exists (select name from sysobjects where type = 'U'
		and name = 'OA_Exceptions')
BEGIN
	print 'Dropping existing OA_Exceptions table'
	drop table OA_Exceptions
END
go


print 'Creating OA_Exceptions table'
go

create table OA_Exceptions
(
  ID          int  	identity 		NOT NULL,
  TIMESTAMP     	varchar(30) 	NOT NULL,
  EXCEPTION_CLASS_NAME  varchar(255) NOT NULL,
  EXCEPTION_MESSAGE     varchar(255) NULL,
  CAUSE_EXCEPTION_CLASS_NAME varchar(255) NULL, 
  CAUSE_EXCEPTION_MESSAGE  varchar(255) NULL, 
  STACK_TRACE           text         NULL,
  ADAPTOR_NAME          varchar(255) NULL,
  THREAD_NAME           varchar(255) NULL,
  ORIGINATING_COMPONENT varchar(255) NULL,
  DATA     	        text 	        NULL,
  FIXED    	        varchar(20)  	NULL,
  REPROCESSED      	varchar(20) 	NULL
)
go

/**
*** Following grants need to be adapted to local conventions!
***/

grant SELECT on OA_Exceptions to ReadOnly
grant SELECT, INSERT, UPDATE, DELETE on OA_Exceptions  to ReadWrite
