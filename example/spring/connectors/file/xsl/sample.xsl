<?xml version="1.0" encoding="UTF-8"?>
<!--
     This XSLT can be found at:
     http://search.cpan.org/~atrickett/XML-RSS-Tools-0.16/docs/example-4.pod
     
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rss="http://purl.org/rss/1.0/"
                xmlns:rss09="http://my.netscape.com/rdf/simple/0.9/"
                exclude-result-prefixes="xsl rdf dc rss rss09"
                >
        <xsl:output method="html" omit-xml-declaration="yes" indent="yes"/>
        <xsl:template match="a">
          <xsl:apply-templates/>
        </xsl:template>
</xsl:stylesheet>
