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


/*
** sp_get_oaevent_test
**
*/

if exists ( select 1 from sysobjects where name = 'sp_get_oaevent_test' and type = 'P')
begin
	print 'dropping proc sp_get_oaevent_test'
	drop proc sp_get_oaevent_test
end
go

print 'creating proc sp_get_oaevent_test'
go

create proc sp_get_oaevent_test
(
	@EventParam1	varchar(60) = NULL,
	@EventParam2	varchar(60) = NULL,
	@EventParam3	varchar(60) = NULL,
	@EventParam4	varchar(60) = NULL,
	@EventParam5	varchar(60) = NULL,
	@EventParam6	varchar(60) = NULL,
	@EventParam7	varchar(60) = NULL,
	@EventParam8	varchar(60) = NULL,
	@EventParam9	varchar(60) = NULL,
	@EventParam10	varchar(60) = NULL
)
as
begin
	declare @now datetime

	select @now = getdate()

	select
		'StringAtt' = 'hello sybase world',
		'IntAtt'    = 12345,
		'NegIntAtt' = -54321,
		'DateAtt'   = @now,
		'FloatAtt'  = 12345.00005

	return 0
end
go

grant exec on sp_get_oaevent_test to public
go


/*
** add OA configuration for SybaseSource example
**
*/

delete OA_EventType where EventTypeName = 'OA_Event_Test'
delete OA_EventService where EventServiceName = 'TestService'
go

exec OA_AddEventType @EventTypeName = 'OA_Event_Test', @EventTypeDesc = '', @EventSprocName = 'sp_get_oaevent_test'
go

exec OA_AddEventService @EventServiceName = 'TestService', @EventServiceDesc = ''
go


/*
** queue some Events
**
*/

exec OA_QueueEvent @EventServiceName = 'TestService', @EventTypeName = 'OA_Event_Test', @EventParam1 = '1'
exec OA_QueueEvent @EventServiceName = 'TestService', @EventTypeName = 'OA_Event_Test', @EventParam1 = '2'
exec OA_QueueEvent @EventServiceName = 'TestService', @EventTypeName = 'OA_Event_Test', @EventParam1 = '3'
exec OA_QueueEvent @EventServiceName = 'TestService', @EventTypeName = 'OA_Event_Test', @EventParam1 = '4'
exec OA_QueueEvent @EventServiceName = 'TestService', @EventTypeName = 'OA_Event_Test', @EventParam1 = '5'
go

