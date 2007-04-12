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
                xmlns="http://www.w3.org/TR/xhtml1/strict"
                xmlns:beans="http://www.springframework.org/schema/beans">
    <!--
        Overall document template.
      -->
    <xsl:template match="/beans:beans">
        <!-- Get description for whole of this adaptor -->
        <xsl:if test="beans:description">
          <pre><xsl:apply-templates select="beans:description"/></pre>
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
            <xsl:for-each select="beans:bean">
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
                        <xsl:apply-templates select="beans:property[@name='chainedNodes']/beans:list"/>
                    </td>
                    <td>
                        <xsl:apply-templates select="beans:property[@name='discardChainedNodes']/beans:list"/>
                    </td>
                    <td>
                        <xsl:apply-templates select="beans:property[@name='staticExceptionRouting']/beans:map"/>
                    </td>
                    <td>
                        <xsl:apply-templates select="beans:property[@name='dynamicExceptionRouting']/beans:map"/>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
        <!-- Generate documentation for each bean -->
        <xsl:for-each select="beans:bean">
            <p>
                <xsl:apply-templates select="."/>
            </p>
        </xsl:for-each>
    </xsl:template>

    <!--
        Templates for bean.
      -->
    <xsl:template match="beans:bean">
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
                <xsl:if test="@beans:class">
                    <xsl:text> : </xsl:text>
                    <a class="th" href="../javadocs/{translate(@beans:class,'.','/')}.html">
                      <xsl:value-of select="@beans:class"/>
                    </a>
                </xsl:if>
                <xsl:if test="@beans:factory-bean">
                    <xsl:text> : factory-bean=</xsl:text>
                    <a class="th" href="{@beans:factory-bean}">
                      <xsl:value-of select="@beans:factory-bean"/>
                    </a>
                </xsl:if>
            </font></th></tr>

            <xsl:if test="beans:description">
                <tr bgcolor="#CCCCCC"><td colspan="3">
                    <xsl:apply-templates select="beans:description"/>
                </td></tr>
            </xsl:if>
            <xsl:if test="beans:property">
                <tr><td colspan="3">
                        <xsl:apply-templates select="beans:property"/>
                </td></tr>
            </xsl:if>
        </table>
    </xsl:template>

    <!--
        Templates for the properties.
      -->
    <xsl:template match="beans:property[@beans:value]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@beans:name"/></td>
            <td><xsl:value-of select="@beans:value"/></td>
            <td><xsl:apply-templates select="beans:description"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="beans:property[@beans:ref]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@beans:name"/></td>
            <td><a href="#{@ref}"><xsl:value-of select="@beans:ref"/></a></td>
            <td><xsl:apply-templates select="beans:description"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="beans:property[not(@beans:value) and not(@beans:ref)]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@beans:name"/></td>
            <td><xsl:apply-templates select="*"/></td>
        </tr>
    </xsl:template>

    <!--
        Templates for the structured data types.
        Note these do not include name of object (to aid reuse in docs).
      -->
    <xsl:template match="beans:list">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:for-each select="*">
                <tr>
                    <td><xsl:value-of select="position()"/></td>
                    <td><xsl:apply-templates select="."/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xsl:template match="beans:map">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:apply-templates select="beans:entry"/>
        </table>
    </xsl:template>

    <xsl:template match="beans:props">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:apply-templates select="beans:prop"/>
        </table>
    </xsl:template>

    <!--
        Templates for the compound data types used by structured data types.
      -->
    <xsl:template match="beans:entry">
        <tr>
            <td><xsl:value-of select="position()"/></td>
            <td><xsl:apply-templates select="beans:key|@beans:key|@beans:key-ref"/></td>
            <td><xsl:apply-templates select="beans:ref|@beans:ref"/></td>
            <td><xsl:apply-templates select="beans:value|@beans:value|@beans:value-ref"/></td>
            <td><xsl:value-of select="descendant-or-self::comment()"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="beans:prop">
        <tr>
            <td><xsl:value-of select="position()"/></td>
            <td><xsl:apply-templates select="@beans:key"/></td>
            <td><xsl:apply-templates select="descendant::node()"/></td>
            <td><xsl:value-of select="descendant-or-self::comment()"/></td>
        </tr>
    </xsl:template>

    <!--
        Templates for the basic data types.
        Note these are deliberately minimalist (to aid reuse in docs).
      -->
    <xsl:template match="beans:key">
        <xsl:value-of select="beans:value"/>
    </xsl:template>

    <xsl:template match="beans:value">
        <xsl:value-of select=".|@*"/>
    </xsl:template>

    <xsl:template match="beans:ref">
        <xsl:if test="@beans:bean">
            <a href="#{@beans:bean}"><xsl:value-of select="@beans:bean"/></a>
        </xsl:if>
        <xsl:if test="@beans:local">
            <a href="#{@beans:bean}"><xsl:value-of select="@beans:local"/></a>
        </xsl:if>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="@beans:ref|@beans:key-ref|@beans:value-ref">
        <a href="#{.}"><xsl:value-of select="."/></a>
    </xsl:template>

</xsl:stylesheet>
