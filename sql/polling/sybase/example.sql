/*
** Example schema and data for use with 
**
**   org.openadaptor.auxil.connector.jdbc.reader.JDBCPollConnector
*/

CREATE TABLE Trade (
  TradeId  INT NOT NULL,
  Ticker   VARCHAR(32) NOT NULL,
  Side     CHAR(1) NOT NULL,
  Quantity INT NOT NULL,
  Price    FLOAT NOT NULL
)
go

CREATE TABLE TradeEvent (
  EventId   INT IDENTITY,
  TradeId   INT NOT NULL,
  Processed CHAR(1) NULL
)
go

CREATE PROC PollTradeEvent
AS
BEGIN
  DECLARE @EventId INT
  SELECT @EventId = NULL
  SELECT @EventId = MIN(EventId) FROM TradeEvent WHERE Processed = NULL
  UPDATE TradeEvent SET Processed = "Y" WHERE EventId = @EventId
  SELECT e.EventId, t.TradeId, t.Ticker, t.Side, t.Quantity, t.Price 
  FROM TradeEvent e, Trade t
  WHERE e.EventId = @EventId AND e.TradeId = t.TradeId
END
go

sp_procxmode PollTradeEvent, "anymode"
go

INSERT Trade VALUES (1, "BT.L", "B", 1000000, 20.05)
INSERT TradeEvent (TradeId) VALUES (1)
go

INSERT Trade VALUES (2, "BT.L", "S", 500000, 19.99)
INSERT TradeEvent (TradeId) VALUES (1)
go

/*
** HERE IS THE SQL TO RESET THE EVENTS
*/

UPDATE TradeEvent SET Processed = NULL
go
