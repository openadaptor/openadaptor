if exists (select 1 from sysobjects where type = "U" and name="ExceptionSummary")
begin
  drop table ExceptionSummary
end
go

create table ExceptionSummary (
  ExceptionId  INT          IDENTITY,
  ComponentId  VARCHAR(64)  NOT NULL,
  Application  VARCHAR(64)  NOT NULL,
  Date         DATETIME     NOT NULL,
  Message      VARCHAR(255) NOT NULL, 
  RetryAddress VARCHAR(255) NOT NULL, 
  Retries      INT          NOT NULL,
  ParentId     INT                  ,
  Host         VARCHAR(64)          ,
  Class        VARCHAR(255)         ,
  Status       CHAR(1)      NOT NULL
)
go

create unique clustered index idx0 on ExceptionSummary(ExceptionId)
go

create nonclustered index idx1 on ExceptionSummary(Application)
go

create nonclustered index idx1 on ExceptionSummary(Date)
go

create nonclustered index idx1 on ExceptionSummary(Application, Date)
go


if exists (select 1 from sysobjects where type = "U" and name="ExceptionDetail")
begin
  drop table ExceptionDetail
end
go

create table ExceptionDetail (
  ExceptionId  INT  NOT NULL,
  Detail       TEXT NOT NULL
)
go

create unique clustered index idx0 on ExceptionDetail(ExceptionId)
go
