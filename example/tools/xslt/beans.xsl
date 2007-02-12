<?xml version="1.0"?>
<!--
    [[
    Copyright (C) 2006 The Software Conservancy as Trustee. All rights
    reserved.

    Permission is hereby granted, free of charge, to any person obtaining a
    copy of this software and associated documentation files (the
    "Software"), to deal in the Software without restriction, including
    without limitation the rights to use, copy, modify, merge, publish,
    distribute, sublicense, and/or sell copies of the Software, and to
    permit persons to whom the Software is furnished to do so, subject to
    the following conditions:

    The above copyright notice and this permission notice shall be included
    in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
    OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

    Nothing in this notice shall be deemed to grant any rights to
    trademarks, copyrights, patents, trade secrets or any other intellectual
    property of the licensor or any contributor except as expressly stated
    herein. No patent license is granted separate from the Software, for
    code that you delete from the Software, or for combinations of the
    Software with other software or hardware.
    ]]

    $Header: /u1/sourcecast/data/ccvs/repository/oa3/cookbook/xslt/beans.xsl,v 1.4 2006/10/05 14:02:31 shirea Exp $

    @author Andrew Shire

    Autogenerates detailed documentation for an OA3 XML file such as a cookbook example.
    (interprets Spring config with knowledge of key OA3 semantics).

    Uses "beans.xsl" to generate detailed documentation for each of the beans in the file.
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml1/strict">

<xsl:import href="adaptorDoc.xsl"/>

<xsl:param name="oaVersion"/>

<xsl:template match="/">
<xsl:variable name="thisExample" select="substring-before(substring-after(comment(),'/cvs/oa3/cookbook/'),'.xml,v')"/>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-15"/>
    <style type="text/css">
        html { background: white }
        body {
            background: white;
            color: black;
            font-family: Arial, Helvetica, san-serif
        }
        td { text-align: left; vertical-align: top }
        th { text-align: left; vertical-align: top }
        a.th:link    {color: white; }
        a.th:visited {color: white; }
    </style>
    <title>Cookbook Beans: <xsl:value-of select="$thisExample"/></title>
  </head>

  <body>
    <h1>Cookbook Beans: <xsl:value-of select="$thisExample"/></h1>

    <p>
        <b><xsl:value-of select="$oaVersion"/></b>
    </p>
    <p>
        <a href="{$thisExample}.xml"><xsl:value-of select="$thisExample"/>.xml</a>
    </p>
    <p>
        <a href="images/{$thisExample}.html">Node Map for <xsl:value-of select="$thisExample"/></a>
    </p>
    <p>
        <a href="cookbook2beans.html#{$thisExample}">Cookbook to Beans index for <xsl:value-of select="$thisExample"/>.</a>
    </p>

    <xsl:apply-templates select="/beans"/>

  </body>
</html>
</xsl:template>
</xsl:stylesheet>
