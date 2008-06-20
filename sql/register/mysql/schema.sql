DROP TABLE OA_Register
GO
CREATE TABLE OA_Register(
    pkId                	bigint(20) AUTO_INCREMENT NOT NULL,
    fldAdaptorName          varchar(255) NOT NULL,
    fldAdaptorId            varchar(255) NULL,
    fldAdaptorVersion       varchar(255) NOT NULL,
    fldHostname         	varchar(255) NOT NULL,
    fldIpAddress        	varchar(255) NOT NULL,
    fldOSName               varchar(255) NOT NULL,
    fldOSVersion        	varchar(255) NOT NULL,
    fldUsername         	varchar(255) NOT NULL,
    fldInstallDir           varchar(255) NOT NULL,
    fldJavaVersion          varchar(255) NOT NULL,
    fldJavaClasspath        text NOT NULL,
    fldPropertiesFile       mediumtext NOT NULL,
    fldAdditionalDetails	varchar(4192) NULL,
    fldTimestamp        	timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(pkID)
)
GO
