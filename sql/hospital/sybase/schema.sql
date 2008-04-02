/**
*** Sybase Message Hospital Schema
***
**/

if exists (select name from sysobjects where type = 'U'
		and name = 'ERROR_LOG')
BEGIN
	print 'Dropping existing ERROR_LOG table'
	drop table ERROR_LOG
END
go


print 'Creating ERROR_LOG table'
go

create table ERROR_LOG
(
  ID          int  	identity 		NOT NULL,
  TIMESTAMP     	varchar(30) 	NOT NULL,
  EXCEPTION_CLASS_NAME  varchar(255) NOT NULL,
  ADAPTOR_NAME          varchar(255) NULL,
  ORIGINATING_COMPONENT varchar(255) NULL,
  DATA     	        text 	NULL,
  FIXED    	        varchar(20)  	NULL,
  REPROCESSED      	varchar(20) 	NULL
)
go

/**
*** Following grants need to be adapted to local conventions!
***/

grant SELECT on ERROR_LOG to ReadOnly
grant SELECT, INSERT, UPDATE, DELETE on ERROR_LOG  to ReadWrite
