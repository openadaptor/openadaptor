<?xml version="1.0"?>
<!--
  [[
  Copyright (C) 2006,2007 The Software Conservancy as Trustee. All rights
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
  
  Produces an index of all cookbook examples to the beans used by each example.
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/TR/xhtml1/strict"
  xmlns:beans="http://www.springframework.org/schema/beans">

  <xsl:param name="oaVersion" />

  <xsl:template match="/cookbook">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-15" />
        <style type="text/css">
          html { background: white }
          body { background: white; color: black; font-family: Arial, Helvetica, san-serif }
          td { text-align: left; vertical-align: top }
          th { text-align: left; vertical-align: top }
          a.th:link {color: white; }
          a.th:visited {color: white; }
        </style>
        <title>ConfigToBeans</title>
      </head>

      <body>
        <table bgcolor="#000099">
          <tr>
            <th>
              <font color="white">
                <xsl:text>ConfigToBeans</xsl:text>
                <xsl:text> | </xsl:text>
                <a class="th" href="./beans2config.html">BeansToConfig</a>
                <xsl:text> | </xsl:text>
                <a class="th" href="./allimages.html">ImagesIndex</a>
              </font>
            </th>
          </tr>
          <tr bgcolor="#CCCCCC">
            <td>
              <xsl:value-of select="$oaVersion" />
            </td>
          </tr>
        </table>

        <!-- Generate clickable index with a table for each config example -->
        <xsl:for-each select="beans">

          <xsl:variable name="idAsRelativePath" select="translate(translate(@id,'_','/'),'-','_')" />
          <p><font color="white">.</font></p>
          <table border="1" bgcolor="#FFFFFF">
          
            <!-- First row: cookbook example name -->
            <tr bgcolor="#000099">
              <th colspan="2">
                <font color="white">
                  <xsl:choose>
                    <!-- Named cookbook example -->
                    <xsl:when test="@id">
                      <a class="th" name="{@id}">
                        <xsl:value-of select="$idAsRelativePath" />
                      </a>
                      <xsl:text> | </xsl:text>
                      <a class="th" href="./{$idAsRelativePath}.html">Documentation</a>
                      <xsl:text> | </xsl:text>
                      <a class="th" href="../{$idAsRelativePath}.xml">XML</a>
                    </xsl:when>
                    <!-- Anonymous cookbook example ("Header" filename is missing from comment block)-->
                    <xsl:otherwise>
                      <tr>
                        <xsl:value-of select="name()" />
                      </tr>
                    </xsl:otherwise>
                  </xsl:choose>
                </font>
              </th>
            </tr>
            <!-- Second row: cookbook example description -->
            <!-- <tr> -->
            <!--   <td colspan="2"> -->
            <!--     <pre><xsl:apply-templates select="description" /></pre> -->
            <!--   </td> -->
            <!-- </tr> -->

            <xsl:variable name="thisExample" select="@id" />
            <xsl:variable name="thisExampleAsRelativePath" select="translate(translate($thisExample,'_','/'),'-','_')" />

            <!-- Third row: table with a row for each bean class (that is used by this cookbook example) -->

            <xsl:if test=".//bean">  <!-- Avoid risk of empty table which upsets some browsers -->
              <tr>

            <xsl:for-each select=".//bean">
              <xsl:variable name="classAsRelativePath" select="translate(@class,'.','/')" />
              <tr>
                <td>
                  <xsl:value-of select="@class" />
                  <xsl:text> | </xsl:text>
                  <a href="../../javadocs/{$classAsRelativePath}.html">JavaDoc</a>
                  <xsl:text> | </xsl:text>
                  <a href="./beans2config.html#{@class}">BeansToConfig</a>
                </td>
                <td bgcolor="#CCCCCC">
                  <xsl:choose>
                    <!-- Named bean => show name of bean: -->
                    <xsl:when test="string-length(@id) > 0">
                      <xsl:value-of select="@id" />
                      <xsl:text> | </xsl:text>
                      <a href="./{$thisExampleAsRelativePath}.html#{@id}">Documentation</a>
                    </xsl:when>
                    <!-- Anonymous bean => show name of top-level parent bean: -->
                    <xsl:otherwise>
                      <xsl:variable name="parentBeanName" select="ancestor::bean[@id!='']/@id" />
                        <xsl:choose>
                          <xsl:when test="string-length($parentBeanName) > 0">
                            <xsl:text>[ in</xsl:text>
                            <xsl:value-of select="$parentBeanName" />
                            <xsl:text>] | </xsl:text>
                            <a href="./{$thisExampleAsRelativePath}.html#{$parentBeanName}">Documentation</a>
                          </xsl:when>
                          <!-- Don't show name of top-level parent bean if it is anonymous too ;-) -->
                          <xsl:otherwise>
                            <xsl:text>[no id for bean]</xsl:text>
                            <xsl:text> | </xsl:text>
                            <a href="./{$thisExampleAsRelativePath}.html">Documentation</a>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
              </tr>
            </xsl:for-each>

</tr>
</xsl:if>


          </table>
        </xsl:for-each>

      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
