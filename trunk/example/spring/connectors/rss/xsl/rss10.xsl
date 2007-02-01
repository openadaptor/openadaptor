<?xml version="1.0" encoding="utf-8"?>

<!--

rss10.xsl
Version 0.1 (Alpha)

Last Update: 5 Apr 2004

Copyright (C) 2004 Adal Chiriliuc

Website: http://adal.eu.org
Email:   contact@adal.eu.org

License: http://creativecommons.org/licenses/by-sa/1.0/

Usage:
  Add the following line AFTER the <?xml ... ?> declaration in a RSS 1.0 feed file.
  The href attribute must point to this file. So you might need something like
  href="http://www.mysite.com/rss/rss10.xsl"

  <?xml-stylesheet type="text/xsl" href="rss10.xsl"?>

History:
  Version 0.1 - 5 Apr 2004 - Original Release

-->

<!--

This is an alpha version. There's a lot more to do.

TODO:
  css       - incomplete
  image     - many elements are optional or not present
  items     - only the items which appear here should be displayed
  image     - see above
  textinput - see above
  footer    - add

  Search for a solution for the disable-output-escaping="yes" problem in Mozilla.

-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rss="http://purl.org/rss/1.0/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
    xmlns:content="http://purl.org/rss/1.0/modules/content/"
    xmlns:admin="http://webns.net/mvcb/"
>

<xsl:output method="html"/>

<xsl:template match="/">
    <html>
        <head>
            <title><xsl:value-of select="rdf:RDF/rss:channel/rss:title"/> (RSS 1.0 feed)</title>
            <xsl:call-template name="output_css"/>
        </head>
        <body>
            <xsl:call-template name="output_intro"/>
            <xsl:apply-templates select="/rdf:RDF/rss:channel"/>
            <h1>Summary</h1>
            <xsl:for-each select="/rdf:RDF/rss:item">
                <xsl:sort select="dc:date" order="descending"/>
                <xsl:call-template name="display_item">
                    <xsl:with-param name="full" select="0"/>
                </xsl:call-template>
            </xsl:for-each>
            <h1>Items</h1>
            <xsl:for-each select="/rdf:RDF/rss:item">
                <xsl:sort select="dc:date" order="descending"/>
                <xsl:call-template name="display_item">
                    <xsl:with-param name="full" select="1"/>
                </xsl:call-template>
            </xsl:for-each>
        </body>
    </html>
</xsl:template>

<xsl:template match="/rdf:RDF/rss:channel">
    <h1><a href="{rss:link}"><xsl:value-of select="rss:title"/></a></h1>
    <xsl:apply-templates select="/rdf:RDF/rss:image"/>
    <xsl:call-template name="display_description"/>
    URL: <a href="{@rdf:about}"><xsl:value-of select="@rdf:about"/></a>
    <xsl:apply-templates select="/rdf:RDF/rss:textinput"/>
    <xsl:call-template name="display_properties"/>
</xsl:template>

<xsl:template match="/rdf:RDF/rss:image">
    <a href="{rss:link}" title="{rss:title}">
        <img src="{rss:url}" width="{rss:width}" height="{rss:height}" alt="{rss:title}" border="0"/>
    </a>
</xsl:template>

<xsl:template match="/rdf:RDF/rss:textinput">
    <form action="{rss:link}">
        <p><xsl:value-of select="rss:description"/></p>
        <input type="text" name="{rss:name}"/>
        <input type="submit" value="{rss:title}"/>
    </form>
</xsl:template>

<xsl:template name="output_css">
    <style type="text/css">
    body { background-color: #ffffff; color: #000000; font-family: Georgia, Times New Roman }
    acronym { border-bottom-style: dotted; border-bottom-width: 1px; cursor: help; border-bottom-color: #999999; }
    .content { background-color: #f0f0f0; padding: 10px }
    .item { }
    .description { }
    .properties { background-color: #f7f7f7 }
    </style>
</xsl:template>

<xsl:template name="output_intro">
    <xsl:comment>Generated from a RSS 1.0 feed by a XSLT style sheet.</xsl:comment>

    <!--
    <h1>What is this?</h1>
    <p>
        This is a <a href="http://web.resource.org/rss/1.0/" title="RDF Site Summary (RSS) 1.0 Official Website">RSS 1.0</a> feed.
        You can subscribe to this feed using your favorite
        <a href="http://dmoz.org/Reference/Libraries/Library_and_Information_Science/Technical_Services/Cataloguing/Metadata/RDF/Applications/RSS/News_Readers/" title="RSS News Readers">
        News Aggregator</a>, to be notified automatically when new entries are added.
        You can find out additional information here: <a href="http://www.whatisrss.com" title="One page introduction to RSS">http://www.WhatIsRSS.com</a>
    </p>
    -->
    <xsl:if test="starts-with(system-property('xsl:vendor'), 'Transformiix')">
        <xsl:if test="starts-with(system-property('xsl:version'), '1')">
            <p>
                Your browser uses the <a href="{system-property('xsl:vendor-url')}" title="XSLT Mozilla Project">
                Mozilla Transformiix</a><xsl:text> </xsl:text>
                <a href="http://www.xslt.com" title="Extensible Stylesheet Language Transformations">XSLT</a> engine.
                This engine
                <a href="http://bugzilla.mozilla.org/show_bug.cgi?id=98168#c11" title="Bug Report">will not support</a>
                the <i>disable-output-escaping</i> attribute on the <i>xsl:value-of</i> element.
                Because of this some items may look weird. If you are a RSS author, please consider using well-formed XML
                instead of <acronym title="Character Data">CDATA</acronym>.
            </p>
        </xsl:if>
    </xsl:if>
</xsl:template>

<xsl:template name="display_date">
    <xsl:variable name="year" select="substring(dc:date,1,4)"/>
    <xsl:variable name="month" select="substring(dc:date,6,2)"/>
    <xsl:variable name="day" select="substring(dc:date,9,2)"/>
    <xsl:variable name="time" select="substring(dc:date,12,8)"/>
    <xsl:variable name="timezone" select="substring(dc:date,20,6)"/>
    <xsl:variable name="day_short">
        <xsl:choose>
            <xsl:when test="substring($day,1,1)='0'"><xsl:value-of select="substring($day,2,1)"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="substring($day,1,2)"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="month_name">
        <xsl:choose>
            <xsl:when test="$month='01'">January</xsl:when>
            <xsl:when test="$month='02'">February</xsl:when>
            <xsl:when test="$month='03'">March</xsl:when>
            <xsl:when test="$month='04'">April</xsl:when>
            <xsl:when test="$month='05'">May</xsl:when>
            <xsl:when test="$month='06'">June</xsl:when>
            <xsl:when test="$month='07'">July</xsl:when>
            <xsl:when test="$month='08'">August</xsl:when>
            <xsl:when test="$month='09'">September</xsl:when>
            <xsl:when test="$month='10'">October</xsl:when>
            <xsl:when test="$month='11'">November</xsl:when>
            <xsl:when test="$month='12'">December</xsl:when>
        </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$day_short"/><xsl:text> </xsl:text>
    <xsl:value-of select="$month_name"/><xsl:text> </xsl:text>
    <xsl:value-of select="$year"/><xsl:text> </xsl:text>
    <xsl:value-of select="$time"/><xsl:text> </xsl:text>
    GMT <xsl:value-of select="$timezone"/>
</xsl:template>

<xsl:template name="display_properties">
    <div class="properties">
    <p>
    <xsl:if test="dc:language!=''">
        Language: <xsl:value-of select="dc:language"/><br/>
    </xsl:if>
    <xsl:if test="dc:creator!=''">
        Creator: <xsl:value-of select="dc:creator"/><br/>
    </xsl:if>
    <xsl:if test="dc:subject!=''">
        Subject: <xsl:value-of select="dc:subject"/><br/>
    </xsl:if>
    <xsl:if test="dc:date!=''">
        Date: <xsl:call-template name="display_date"/><br/>
    </xsl:if>
    <xsl:if test="admin:generatorAgent/@rdf:resource">
        Generator: <a href="{admin:generatorAgent/@rdf:resource}"><xsl:value-of select="admin:generatorAgent/@rdf:resource"/></a><br/>
    </xsl:if>
    </p>
    </div>
</xsl:template>

<xsl:template name="display_description">
    <div class="description">
    <p>
        <xsl:value-of select="rss:description" disable-output-escaping="yes"/>
    </p>
    </div>
</xsl:template>

<xsl:template name="display_item">
    <xsl:param name="full"/>
    <xsl:choose>
        <xsl:when test="$full=1">
            <div class="item">
            <a name="item{position()}"></a>
            <h2>
                <xsl:value-of select="position()"/> - <a href="{rss:link}"><xsl:value-of select="rss:title"/></a>
            </h2>
            <xsl:call-template name="display_description"/>
            <xsl:if test="content:encoded">
                <div class="content">
                    <xsl:value-of select="content:encoded" disable-output-escaping="yes"/>
                </div>
            </xsl:if>
            <xsl:call-template name="display_properties"/>
            </div>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="position()"/> - <a href="#item{position()}"><xsl:value-of select="rss:title"/></a><br/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
