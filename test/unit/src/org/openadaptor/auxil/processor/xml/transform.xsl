<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" omit-xml-declaration="no" />

<xsl:template match="/">
    <developers>
        <xsl:for-each select="/team/*[type='Developer']">
        <xsl:sort select="name"/>
            <person><xsl:value-of select="name"/></person>
        </xsl:for-each>
    </developers>
</xsl:template>

</xsl:stylesheet>