/**
*** Sybase Message Exceptions Schema
***
**/


if exists (select 1 from  sysobjects where  id = object_id('dbo.OA_Exception') and type = 'U')
BEGIN
   print 'Dropping existing OA_Exception table'
   drop table dbo.OA_Exception
END
go


print 'Creating OA_Exception table'
go

create table dbo.OA_Exception
(
  ID                         int identity NOT NULL,
  TIMESTAMP                  varchar(30)  NOT NULL,
  EXCEPTION_CLASS_NAME       varchar(255) NOT NULL,
  EXCEPTION_MESSAGE          varchar(255) NULL,
  CAUSE_EXCEPTION_CLASS_NAME varchar(255) NULL, 
  CAUSE_EXCEPTION_MESSAGE    varchar(255) NULL, 
  STACK_TRACE                text         NULL,
  ADAPTOR_NAME               varchar(255) NULL,
  THREAD_NAME                varchar(255) NULL,
  ORIGINATING_COMPONENT      varchar(255) NULL,
  DATA_TYPE                  varchar(255) NULL,
  DATA                       text         NULL,
  FIXED                      varchar(20)  NULL,
  REPROCESSED                varchar(20)  NULL
)
go

/**
*** Following grants need to be adapted to local conventions!
***/

grant SELECT on dbo.OA_Exception to ReadOnly
grant SELECT, INSERT, UPDATE, DELETE on dbo.OA_Exception to ReadWrite