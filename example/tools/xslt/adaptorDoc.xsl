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

    $Header: /u1/sourcecast/data/ccvs/repository/oa3/cookbook/xslt/adaptorDoc.xsl,v 1.4 2006/10/06 12:39:06 shirea Exp $

    @author Andrew Shire

    Autogenerates detailed documentation for OA3 XML files
    (interprets Spring config with knowledge of key OA3 semantics).

    Used by "beans.xsl" which generates documentation for an entire cookbook example.
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml1/strict">
    <!--
        Overall document template.
      -->
    <xsl:template match="/beans">
        <!-- Get description for whole of this adaptor -->
        <xsl:if test="description">
          <p>
            <xsl:apply-templates select="description"/>
          </p>
        </xsl:if>
        <!-- Generate clickable table of contents for top-level beans -->
        <table border="1" bgcolor="#CCCCCC">
            <colgroup>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
            </colgroup>

            <tr bgcolor="#000099">
                <th><font color="white">Beans</font></th>
                <th><font color="white">chainedNodes</font></th>
                <th><font color="white">discardChainedNodes</font></th>
                <th><font color="white">staticExceptionRouting</font></th>
                <th><font color="white">dynamicExceptionRouting</font></th>
            </tr>
            <xsl:for-each select="bean">
                <tr>
                    <td>
                        <xsl:choose>
                            <!-- Named bean -->
                            <xsl:when test="@id">
                                <a href="#{@id}"><xsl:value-of select="@id"/></a>
                            </xsl:when>
                            <!-- Anonymous bean -->
                            <xsl:otherwise>
                                <xsl:value-of select="name()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                    <td>
                        <xsl:apply-templates select="property[@name='chainedNodes']/list"/>
                    </td>
                    <td>
                        <xsl:apply-templates select="property[@name='discardChainedNodes']/list"/>
                    </td>
                    <td>
                        <xsl:apply-templates select="property[@name='staticExceptionRouting']/map"/>
                    </td>
                    <td>
                        <xsl:apply-templates select="property[@name='dynamicExceptionRouting']/map"/>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
        <!-- Generate documentation for each bean -->
        <xsl:for-each select="bean">
            <p>
                <xsl:apply-templates select="."/>
            </p>
        </xsl:for-each>
    </xsl:template>

    <!--
        Templates for bean.
      -->
    <xsl:template match="bean">
        <table border="1" bgcolor="#CCCCCC">
            <colgroup>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
            </colgroup>

            <tr bgcolor="#000099"><th colspan="3"><font color="white">
                <xsl:choose>
                    <!-- Named bean -->
                    <xsl:when test="@id">
                        <a name="{@id}"></a>
                        <a class="th" href="./beans2cookbook.html#{@class}">
                            <xsl:value-of select="@id"/>
                        </a>
                    </xsl:when>
                    <!-- Anonymous bean -->
                    <xsl:otherwise>
                        <a class="th" href="./beans2cookbook.html#{@class}">
                            <xsl:value-of select="name()"/>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
                <!-- Beans always have a class, unless they refer to a factory bean: -->
                <xsl:if test="@class">
                    <xsl:text> : </xsl:text>
                    <a class="th" href="../javadocs/{translate(@class,'.','/')}.html">
                      <xsl:value-of select="@class"/>
                    </a>
                </xsl:if>
                <xsl:if test="@factory-bean">
                    <xsl:text> : factory-bean=</xsl:text>
                    <a class="th" href="{@factory-bean}">
                      <xsl:value-of select="@factory-bean"/>
                    </a>
                </xsl:if>
            </font></th></tr>

            <xsl:if test="description">
                <tr bgcolor="#CCCCCC"><td colspan="3">
                    <xsl:apply-templates select="description"/>
                </td></tr>
            </xsl:if>
            <xsl:if test="property">
                <tr><td colspan="3">
                        <xsl:apply-templates select="property"/>
                </td></tr>
            </xsl:if>
        </table>
    </xsl:template>

    <!--
        Templates for the properties.
      -->
    <xsl:template match="property[@value]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@name"/></td>
            <td><xsl:value-of select="@value"/></td>
            <td><xsl:apply-templates select="description"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="property[@ref]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@name"/></td>
            <td><a href="#{@ref}"><xsl:value-of select="@ref"/></a></td>
            <td><xsl:apply-templates select="description"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="property[not(@value) and not(@ref)]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@name"/></td>
            <td><xsl:apply-templates select="*"/></td>
        </tr>
    </xsl:template>

    <!--
        Templates for the structured data types.
        Note these do not include name of object (to aid reuse in docs).
      -->
    <xsl:template match="list">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:for-each select="*">
                <tr>
                    <td><xsl:value-of select="position()"/></td>
                    <td><xsl:apply-templates select="."/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xsl:template match="map">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:apply-templates select="entry"/>
        </table>
    </xsl:template>

    <xsl:template match="props">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:apply-templates select="prop"/>
        </table>
    </xsl:template>

    <!--
        Templates for the compound data types used by structured data types.
      -->
    <xsl:template match="entry">
        <tr>
            <td><xsl:value-of select="position()"/></td>
            <td><xsl:apply-templates select="key|@key|@key-ref"/></td>
            <td><xsl:apply-templates select="ref|@ref"/></td>
            <td><xsl:apply-templates select="value|@value|@value-ref"/></td>
            <td><xsl:value-of select="descendant-or-self::comment()"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="prop">
        <tr>
            <td><xsl:value-of select="position()"/></td>
            <td><xsl:apply-templates select="@key"/></td>
            <td><xsl:apply-templates select="descendant::node()"/></td>
            <td><xsl:value-of select="descendant-or-self::comment()"/></td>
        </tr>
    </xsl:template>

    <!--
        Templates for the basic data types.
        Note these are deliberately minimalist (to aid reuse in docs).
      -->
    <xsl:template match="key">
        <xsl:value-of select="value"/>
    </xsl:template>

    <xsl:template match="value">
        <xsl:value-of select=".|@*"/>
    </xsl:template>

    <xsl:template match="ref">
        <xsl:if test="@bean">
            <a href="#{@bean}"><xsl:value-of select="@bean"/></a>
        </xsl:if>
        <xsl:if test="@local">
            <a href="#{@bean}"><xsl:value-of select="@local"/></a>
        </xsl:if>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="@ref|@key-ref|@value-ref">
        <a href="#{.}"><xsl:value-of select="."/></a>
    </xsl:template>

</xsl:stylesheet>
