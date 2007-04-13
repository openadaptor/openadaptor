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

    $HeadURL$

    @author Andrew Shire

    Produces an index of all beans to all cookbook examples.
    It only lists beans that are used by at least one cookbook example.
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml1/strict"
                xmlns:beans="http://www.springframework.org/schema/beans">

<xsl:param name="oaVersion"/>

<xsl:template match="/cookbook">
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
    <title>Beans to Config Index</title>
  </head>

  <body>
    <h1>Beans to Config Index</h1>

    <b><xsl:value-of select="$oaVersion"/></b>

    <!-- Generate clickable table of contents with a row for each bean class used in the cookbook -->
    <table border="1" bgcolor="#FFFFFF">
        <colgroup>
            <col bgcolor="#FFFFFF"/>
            <col bgcolor="#FFFFFF"/>
            <col bgcolor="#FFFFFF"/>
            <col bgcolor="#FFFFFF"/>
        </colgroup>

        <tr bgcolor="#000099">
            <th><font color="white">Bean class</font></th>
            <th><font color="white">Config examples</font></th>
        </tr>
        <xsl:for-each select="beans//bean">
            <xsl:sort select="@class"/>  <!-- sort by package/class names -->
            <xsl:variable name="thisClass" select="@class"/>
            <xsl:if test="not(preceding::bean[@class = $thisClass])">  <!-- ensure each class appears exactly once! -->
                <tr>
                    <!-- First column: bean class name -->
                    <td>
                        <a name="{$thisClass}"></a>
                        <a href="../../javadocs/{translate($thisClass,'.','/')}.html"><xsl:value-of select="$thisClass"/></a>
                    </td>

                    <!-- Second column: table of cookbook examples (that use this bean class) -->
                    <td>
                        <xsl:if test="//bean[@class = $thisClass]">   <!-- (reduce risk of empty table as messes up web browsers) -->
                            <table>
                                <xsl:for-each select="//bean[@class = $thisClass]">
                                    <xsl:variable name="thisExample" select="ancestor::beans[@id]/@id"/>
                                    <xsl:choose>
                                        <xsl:when test="string-length($thisExample) > 0">
                                            <tr>
                                                <td><a href="./config2beans.html#{$thisExample}"><xsl:value-of select="translate(translate($thisExample,'_','/'),'-','_')"/></a></td>
                                                <xsl:choose>
                                                    <!-- Named bean => show name of bean -->
                                                    <xsl:when test="string-length(@id) > 0">
                                                        <td><a href="./{translate(translate($thisExample,'_','/'),'-','_')}.html#{@id}">[<xsl:value-of select="@id"/>]</a></td>
                                                    </xsl:when>
                                                    <!-- Anonymous bean => show name of top-level parent bean -->
                                                    <xsl:otherwise>
                                                        <xsl:variable name="parentBeanName" select="ancestor::bean[@id!='']/@id"/>
                                                        <xsl:choose>
                                                            <xsl:when test="string-length($parentBeanName) > 0">
                                                                <td><a href="./{translate(translate($thisExample,'_','/'),'-','_')}.html#{$parentBeanName}">[in <xsl:value-of select="$parentBeanName"/>]</a></td>
                                                            </xsl:when>
                                                            <!-- Don't show name of top-level parent bean if it is anonymous too ;-) -->
                                                            <xsl:otherwise>
                                                                <td><a href="./{translate(translate($thisExample,'_','/'),'-','_')}.html">[no id for bean]</a></td>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                     </xsl:otherwise>
                                                </xsl:choose>
                                            </tr>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <tr><td></td></tr>  <!--(avoid risk of empty table as messes up web browsers) -->
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </table>
                        </xsl:if>
                    </td>
                </tr>
            </xsl:if>
        </xsl:for-each>
    </table>

  </body>
</html>
</xsl:template>
</xsl:stylesheet>
