/*
**
#* [[
#* Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
#* reserved.
#*
#* Permission is hereby granted, free of charge, to any person obtaining a
#* copy of this software and associated documentation files (the
#* "Software"), to deal in the Software without restriction, including
#* without limitation the rights to use, copy, modify, merge, publish,
#* distribute, sublicense, and/or sell copies of the Software, and to
#* permit persons to whom the Software is furnished to do so, subject to
#* the following conditions:
#*
#* The above copyright notice and this permission notice shall be included
#* in all copies or substantial portions of the Software.
#*
#* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
#* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
#* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
#* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
#* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
#* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
#* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#*
#* Nothing in this notice shall be deemed to grant any rights to
#* trademarks, copyrights, patents, trade secrets or any other intellectual
#* property of the licensor or any contributor except as expressly stated
#* herein. No patent license is granted separate from the Software, for
#* code that you delete from the Software, or for combinations of the
#* Software with other software or hardware.
#* ]]
**
**/

/* MS SQL Server version */

use DATABASE_NAME
go

/*********************************************************************************************
#* Table Name    : 	OA_Event
#*
#* Description   : 	Contains the events to be processed
#*********************************************************************************************
*/

print 'Creating table OA_Event'
go

create table OA_Event (
EventId                             numeric(32) Identity,
EventSpid				                    int NULL,
EventServiceId                      int NOT NULL,
EventTypeId                         int NOT NULL,
EventQueuedAt                       datetime NOT NULL,
EventStatus                         varchar( 10 ) NOT NULL,
EventDeliveredAt                    datetime NULL,
EventFailedText                     varchar( 255 ) NULL,
EventParam1                         varchar( 60 ) NULL,
EventParam2                         varchar( 60 ) NULL,
EventParam3                         varchar( 60 ) NULL,
EventParam4                         varchar( 60 ) NULL,
EventParam5                         varchar( 60 ) NULL,
EventParam6                         varchar( 60 ) NULL,
EventParam7                         varchar( 60 ) NULL,
EventParam8                         varchar( 60 ) NULL,
EventParam9                         varchar( 60 ) NULL,
EventParam10                        varchar( 60 ) NULL
)
go

create unique index idx0
on OA_Event ( EventId )
go

create nonclustered index idx1
on OA_Event ( EventServiceId, EventStatus, EventQueuedAt)
go

create unique nonclustered index idx2
on OA_Event ( EventStatus, EventTypeId, EventParam1, EventParam2, EventId )
go

grant select on OA_Event to public
go


/*********************************************************************************************
#* Table Name    : 	OA_EventType
#*
#* Description   : 	Lists types of events any individual system, or adaptor, knows about.
#*			            So for an Equity Trading system this may be "Order" or "Execution" or or or...
#*********************************************************************************************
*/

print 'Creating table OA_EventType'
go

create table OA_EventType (
  EventTypeId      int,
  EventTypeName    varchar(30),
  EventTypeDesc    varchar(255),
  EventSprocName   varchar(92)   -- can be in <db>.<owner>.<procname> format i.e. 92 chars possible
  )
go
create unique clustered index idx0 on OA_EventType (EventTypeId)
go
create unique index idx1 on OA_EventType (EventTypeName)
go
create unique index idx2 on OA_EventType (EventTypeId, EventTypeName)
go

grant select on OA_EventType to public
go

/*********************************************************************************************
#* Table Name    : 	OA_EventService
#*
#* Description   : 	Lists the queue-reading services which are using this system.
#*			            Typically this will be at a project level.
#*********************************************************************************************
*/

print 'Creating table OA_EventService'
go

create table OA_EventService (
  EventServiceId    int,
  EventServiceName  varchar(30),
  EventServiceDesc  varchar(255)
)
go

create unique clustered index idx0 on OA_EventService (EventServiceId)
go
create unique index idx1 on OA_EventService (EventServiceName)
go
create unique index idx2 on OA_EventService (EventServiceId, EventServiceName)
go

grant select on OA_EventService to public
go

/*********************************************************************************************
#* Stored Procedure Name    : 	OA_AddEventService
#*
#* Description              : 	Adds new event service entry into OA_EventService table
#*********************************************************************************************
*/

if exists (select name from sysobjects where name = 'OA_AddEventService' and uid = user_id() )
  drop proc OA_AddEventService
go

print 'Creating OA_AddEventService procedure'
go

create proc OA_AddEventService
	@EventServiceId int = NULL output,
	@EventServiceName varchar(30),
	@EventServiceDesc varchar(255) = NULL
as
BEGIN

  declare @ErrorCode int, @ErrorMessage varchar(255), @NumRows int

  Print 'DEBUG: OA_AddEventService called with ServiceName ' + @EventServiceName + ' EventServiceDesc ' + @EventServiceDesc

  if (@EventServiceDesc = NULL)
    select @EventServiceDesc = @EventServiceName + ' - description not supplied'

  select @EventServiceId = (isnull (max (EventServiceId), 0)) + 1 from OA_EventService

  insert OA_EventService (EventServiceId, EventServiceName, EventServiceDesc)
  values (@EventServiceId, @EventServiceName, @EventServiceDesc)

  select @ErrorCode = @@error, @NumRows = @@rowcount

  if (@NumRows != 1)
  BEGIN
    Raiserror ('ERROR: OA3_AddMessageService ServiceName %s ServiceDesc %s inserted %d rows', 16, 1, @EventServiceName, @EventServiceDesc, @NumRows)
    return 1
  END

  return @ErrorCode
END
go

grant execute on OA_AddEventService to public
go


/*********************************************************************************************
#* Stored Procedure Name    : 	OA_AddEventType
#*
#* Description              : 	Adds new Event type entry into OA_EventType table
#*********************************************************************************************
*/

if exists (select name from sysobjects where name = 'OA_AddEventType' and uid = user_id() )
  drop proc OA_AddEventType
go

print 'Creating OA_AddEventType procedure'
go

create proc OA_AddEventType
  @EventTypeId int = NULL output,
  @EventTypeName varchar(30),
  @EventTypeDesc varchar(255) = NULL,
  @EventSprocName varchar(92) = ''
as
BEGIN

  declare @ErrorCode int, @ErrorEvent varchar(255), @NumRows int

  Print 'DEBUG: OA_AddEventType called with EventTypeName ' + @EventTypeName + ' EventTypeDesc ' + @EventTypeDesc + ' EventSprocName ' + @EventSprocName

  if (@EventTypeDesc = NULL)
    select @EventTypeDesc = @EventTypeName + ' - description not supplied'

  select @EventTypeId = (isnull (max (EventTypeId), 0)) + 1 from OA_EventType

  insert OA_EventType (EventTypeId, EventTypeName, EventTypeDesc, EventSprocName)
  values (@EventTypeId, @EventTypeName, @EventTypeDesc, @EventSprocName)

  select @ErrorCode = @@error, @NumRows = @@rowcount

  if (@NumRows != 1)
  BEGIN
    Raiserror('ERROR: OA_AddEventType TypeName %s TypeDesc %s SprocName %s inserted %d rows',16,1, @EventTypeName, @EventTypeDesc, @EventSprocName, @NumRows)
    Return 1
  END

  return @ErrorCode
go

grant execute on OA_AddEventType to public
go



/*********************************************************************************************
#* Stored Procedure Name    : 	OA_QueueEvent
#*
#* Description              : 	Add event to OA_Event table
#*********************************************************************************************
*/

if exists ( select 1 from sysobjects where name = 'OA_QueueEvent'
	and type = 'P' and uid = user_id() )
begin
	drop proc OA_QueueEvent
end
go

create proc OA_QueueEvent
@EventTypeName    varchar(30), -- in preference to Id to minimize bug likelihood in stored procs
@EventServiceName varchar(30), -- "Narrowband" notification (mandatory)
@EventParam1      varchar(60)  = NULL,
@EventParam2      varchar(60)  = NULL,
@EventParam3      varchar(60)  = NULL,
@EventParam4      varchar(60)  = NULL,
@EventParam5      varchar(60)  = NULL,
@EventParam6      varchar(60)  = NULL,
@EventParam7      varchar(60)  = NULL,
@EventParam8      varchar(60)  = NULL,
@EventParam9      varchar(60)  = NULL,
@EventParam10     varchar(60)  = NULL
as
BEGIN

  declare @ErrorCode int, @ErrorMessage varchar(255), @NumRows int

  declare @EventServiceId int,
          @EventTypeId int

  select @ErrorCode = 0

  select @EventTypeId = EventTypeId from OA_EventType where EventTypeName = @EventTypeName
  if (@@rowcount = 0)
  BEGIN
    Raiserror('WARNING: TypeName %s does not exist - not queueing event, returning fail',16,1, @EventTypeName)
    Return 1
  END

  select @EventServiceId = EventServiceId from OA_EventService where EventServiceName = @EventServiceName
  if (@@rowcount = 0)
  BEGIN
    Raiserror('WARNING: ServiceName %1! does not have a service defined - returning fail',16,1,@EventServiceName)
    Return 1
  END

  insert OA_Event (
    EventServiceId, EventTypeId,
    EventQueuedAt, EventStatus,
    EventDeliveredAt, EventFailedText,
    EventParam1, EventParam2, EventParam3, EventParam4, EventParam5,
    EventParam6, EventParam7, EventParam8, EventParam9, EventParam10
  )
  select
    @EventServiceId, @EventTypeId,
    getdate(), 'NEW',
    NULL, NULL,
    @EventParam1, @EventParam2, @EventParam3, @EventParam4, @EventParam5,
    @EventParam6, @EventParam7, @EventParam8, @EventParam9, @EventParam10

  select @ErrorCode = @@error, @NumRows = @@rowcount
  if (@ErrorCode != 0) -- this should never happen
  BEGIN
    Raiserror('ERROR: OA_QueueEvent got error code [%d] while inserting into OA_Event',16,1,@ErrorCode)
  END

  return @ErrorCode
END
go

grant execute on OA_QueueEvent to public
go


/*********************************************************************************************
#* Stored Procedure Name    : 	OA_SetEventStatus
#*
#* Description              : 	This proc sets the EventStatus flag
#*********************************************************************************************
*/

if exists (select name from sysobjects where name = 'OA_SetEventStatus' and uid = user_id() )
  drop proc OA_SetEventStatus
go

print 'Creating OA_SetEventStatus procedure'
go

create proc OA_SetEventStatus
	@EventId numeric(32),
	@EventStatus varchar(10),
	@EventFailedText varchar(255)
as
BEGIN -- OA_SetEventStatus

	declare  @ErrorMessage varchar(255), @NumRows int
	declare @RightNow datetime
	declare @EventTypeId int

	select @EventTypeId = EventTypeId from OA_Event where EventId = @EventId

	BEGIN TRAN
	SAVE TRAN StatusUpdate

	update OA_Event set EventStatus = @EventStatus, EventDeliveredAt = getdate(), EventFailedText= @EventFailedText
	where EventId = @EventId

	select @NumRows = @@rowcount

	if (@NumRows != 1)
	BEGIN -- Error, number of rows updated != 1

    Raiserror ('ERROR: OA_SetEventStatus EventId %d Status %s updated %d rows',16,1, @EventId, @EventStatus, @NumRows)

		ROLLBACK TRAN StatusUpdate
		COMMIT TRAN

	END -- Error, number of rows updated != 1

	COMMIT TRAN StatusUpdate

END -- OA_SetEventStatus
go

grant execute on OA_SetEventStatus to public
go


/*********************************************************************************************
#* Stored Procedure Name    : 	OA_GetNextQueuedEvent
#*
#* Description              : 	Returns next event with status 'NEW'
#*********************************************************************************************
*/

if exists ( select 1 from sysobjects where name = 'OA_GetNextQueuedEvent'
	and type = 'P' and uid = user_id() )
begin
	drop proc OA_GetNextQueuedEvent
end
go

print 'Creating OA_GetNextQueuedEvent procedure'
go

create proc OA_GetNextQueuedEvent
  @EventServiceId     int,
  @EventTypeId        int,
  @EventStatus        varchar(10) = 'SENT'
as
BEGIN -- OA_GetNextQueuedEvent

  declare @EventId        numeric(32)
  declare @ErrorCode      int
  declare @ErrorMessage   varchar(255)
  declare @NumRows        int
  declare @EventQueuedAt  datetime

  BEGIN TRAN
  SAVE TRAN getEvent

    select @EventId = NULL

    -- Get a new event
    select @EventId = min(EventId)
    from
      OA_Event
    where EventStatus = 'NEW'
      and EventServiceId = @EventServiceId
      and EventTypeId = @EventTypeId

    if @EventId is not NULL
      BEGIN
        -- Update event
        update OA_Event
        set EventStatus = @EventStatus, EventDeliveredAt = getdate()
        where EventId = @EventId

        -- Return data
        select a.EventId, a.EventServiceId, a.EventTypeId, b.EventTypeName, b.EventSprocName,
             EventParam1, EventParam2, EventParam3, EventParam4, EventParam5,
             EventParam6, EventParam7, EventParam8, EventParam9, EventParam10
       from
          OA_Event a, OA_EventType b
       where
          @EventId = EventId
      END

  COMMIT TRAN getEvent

END -- OA_GetNextQueuedEvent
go

grant execute on OA_GetNextQueuedEvent to public
go

