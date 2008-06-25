DROP TABLE OA_Register
GO
CREATE TABLE OA_Register(
    Id                	bigint(20) AUTO_INCREMENT NOT NULL,
    AdaptorName         varchar(255) NOT NULL,
    AdaptorId           varchar(255) NULL,
    AdaptorVersion      varchar(255) NOT NULL,
    Hostname         	varchar(255) NOT NULL,
    IpAddress        	varchar(255) NOT NULL,
    OSName              varchar(255) NOT NULL,
    OSVersion        	varchar(255) NOT NULL,
    Username         	varchar(255) NOT NULL,
    InstallDir          varchar(255) NOT NULL,
    JavaVersion         varchar(255) NOT NULL,
    JavaClasspath       text NOT NULL,
    PropertiesFile      mediumtext NOT NULL,
    AdditionalDetails	varchar(4192) NULL,
    Timestamp        	timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(Id)
)
GO
