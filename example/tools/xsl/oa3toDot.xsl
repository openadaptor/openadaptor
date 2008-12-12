<?xml version="1.0" encoding="UTF-8"?>
<!--
  [[
  Copyright (C) 2006 - 2008 The Software Conservancy as Trustee. All rights
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
  
  $HeadURL: https://openadaptor3.openadaptor.org/svn/openadaptor3/trunk/example/tools/xslt/oa3toDot.xsl $
  
  @author Andrew Shire
  @author Eddy Higgins
  
  Converts an OA3 XML config file (a Spring config file) into GraphViz "dot" format.
  We wanted to produce a node map with HTML links to the detailed
  documentation we generate using "beans.xsl".
  The "dot" format file can be processed by GraphViz to produce a GIF and image map pair,
  which we can then expose through HTML pages generated by "nodemap.xsl" and "allnodesmaps.xsl".
  
  With thanks to Mike Thomas (http://www.samoht.com) for making available a simple
  XSLT stylesheet that converts a Spring bean config file into a Graphviz/Dot input file.
  He showed how well it could work and we used his work as the inspiration for this file.
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:beans="http://www.springframework.org/schema/beans">
  <xsl:output method="text" />

  <!-- Which you have depends on which DNS name you used in your SVN checkout URL: -->
  <xsl:param name="filepathGlobPrefix1" select="'HeadURL: https://www.openadaptor.org/svn/openadaptor3/trunk/example/'"/>
  <xsl:param name="filepathGlobPrefix2" select="'HeadURL: https://openadaptor3.openadaptor.org/svn/openadaptor3/trunk/example/'"/>
  <xsl:param name="filepathGlobPrefix3" select="'some value1 defined specifically for your build environment'"/>
  <xsl:param name="filepathGlobPrefix4" select="'some value2 defined specifically for your build environment'"/>

  <xsl:variable name="exampleName"
    select="concat(
      substring-before(substring-after(beans:beans/beans:description|comment(),$filepathGlobPrefix1),'.xml '),
      substring-before(substring-after(beans:beans/beans:description|comment(),$filepathGlobPrefix2),'.xml '),
      substring-before(substring-after(beans:beans/beans:description|comment(),$filepathGlobPrefix3),'.xml '),
      substring-before(substring-after(beans:beans/beans:description|comment(),$filepathGlobPrefix4),'.xml ')
    )"/>    

  <xsl:variable name="exampleShortName">
    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="$exampleName" />
      <xsl:with-param name="substr" select="'/'" />
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="baseRelativeDepth"
    select="string-length(translate($exampleName,'/abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ._-1234567890','/'))" />
  <xsl:variable name="baseRelativeDotDot">
    <xsl:choose>
      <xsl:when test="$baseRelativeDepth = '1'">../</xsl:when>
      <xsl:when test="$baseRelativeDepth = '2'">../../</xsl:when>
      <xsl:when test="$baseRelativeDepth = '3'">../../../</xsl:when>
      <xsl:when test="$baseRelativeDepth = '4'">../../../../</xsl:when>
      <xsl:when test="$baseRelativeDepth = '5'">
        ../../../../../
      </xsl:when>
      <xsl:when test="$baseRelativeDepth = '6'">
        ../../../../../../
      </xsl:when>
      <xsl:when test="$baseRelativeDepth = '7'">
        ../../../../../../../
      </xsl:when>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="graphName"
    select="concat('Map_', translate(translate($exampleName, '/', '_'),'-','_'))" />

  <xsl:template match="/">
    digraph
    <xsl:value-of select="$graphName" />
    {
    graph [ ];
    node [ shape=rectangle, style=filled, fontname=Courier, fontsize=10, color=pink ];
    edge [ fontname=Courier, fontsize=9 ];

    <!-- Insert nodes for all top-level beans: -->
    <xsl:apply-templates select="beans:beans/beans:bean" />

    }
  </xsl:template>


  <xsl:template match="beans:bean">
    <xsl:variable name="srcNode" select="concat(@id,@name)" />

    <!-- Name of node ("node id"): -->
    <xsl:text>"</xsl:text>
    <xsl:value-of select="translate($srcNode, '-', '_')" />
    <xsl:text>"</xsl:text>

    <!-- Begin attribute list for node: -->
    <xsl:text>[</xsl:text>

    <!-- Node label defined as an HTML fragment (hence embedded in angle brackets): -->
    <xsl:text>label= &lt;</xsl:text>
    <xsl:text>&lt;table&gt;&lt;tr&gt;&lt;td&gt;</xsl:text>
    <xsl:value-of select="$srcNode" />
    <xsl:text>&lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;</xsl:text>
    <xsl:variable name="labelClassLen" select="40" />
    <xsl:variable name="tooltipClassLen" select="70" />
    <xsl:choose>
      <xsl:when test="string-length(@class) > $labelClassLen">
        <xsl:value-of select="concat('...', substring(@class, string-length(@class)-$labelClassLen))" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@class" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="@factory-bean">
      <xsl:text>Factory:</xsl:text>
      <xsl:value-of select="@factory-bean" />
      <xsl:choose>
        <xsl:when test="string-length(@factory-bean) > $tooltipClassLen">
          <xsl:value-of select="concat('...', substring(@factory-bean, string-length(@factory-bean)-$tooltipClassLen))" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@factory-bean" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:text>&lt;/td&gt;&lt;/tr&gt;</xsl:text>
    <xsl:text>&lt;/table&gt;</xsl:text>
    <xsl:text>&gt;</xsl:text>

    <!-- Node URL: -->
    <xsl:if test="string-length($srcNode) > 0">
      <xsl:text>URL="</xsl:text>
      <xsl:value-of select="concat($baseRelativeDotDot, $exampleName, '.html#', $srcNode)" />
      <xsl:text>",</xsl:text>
      <xsl:text>tooltip="</xsl:text>
      <xsl:value-of select="$srcNode" />
      <xsl:value-of select="': '" />
      <xsl:value-of select="@class|@factory-bean" />
      <xsl:text>",</xsl:text>
    </xsl:if>

    <!-- Node colour according to node type: -->
    <xsl:choose>
      <!-- Adaptors, Routers/Pipelines and Factories: -->
      <xsl:when test="contains(@class, 'org.openadaptor.core.adaptor')">
        <xsl:text>color=lightgray</xsl:text>
      </xsl:when>
      <xsl:when test="contains(@class, 'org.openadaptor.core.router')">
        <xsl:text>color=lightgray</xsl:text>
      </xsl:when>
      <xsl:when test="@factory-bean">
        <xsl:text>color=lightgray</xsl:text>
      </xsl:when>

      <!-- Standard OA3 classes: -->
      <xsl:when test="contains(@class, 'org.openadaptor')">
        <xsl:choose>
          <xsl:when test="contains(@class, 'org.openadaptor.core.node')">
            <xsl:text>color=LightCyan</xsl:text>
          </xsl:when>
          <xsl:when test="contains(@class, 'org.openadaptor.core.connector')">
            <xsl:text>color=YellowGreen</xsl:text>
          </xsl:when>
          <xsl:when test="contains(@class, 'org.openadaptor.auxil.connector')">
            <xsl:text>color=YellowGreen</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>color=LightBlue</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- Standard Spring classes: -->
      <xsl:when test="contains(@class, 'org.springframework')">
        <xsl:choose>
          <xsl:when test="contains(@class, 'interceptor')">
            <xsl:text>color=Orange</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>color=Wheat</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <xsl:when test="@parent">
        <xsl:text>color=LightCyan</xsl:text>
      </xsl:when>

      <!-- Extension classes: -->
      <xsl:otherwise>
        <xsl:text>color=Pink</xsl:text>
      </xsl:otherwise>
    </xsl:choose>

    <!-- End attribute list for node: -->
    <xsl:text>];
</xsl:text>

    <xsl:call-template name="calculateEdges">
      <xsl:with-param name="srcNode" select="$srcNode" />
    </xsl:call-template>
  </xsl:template>


  <xsl:template name="calculateEdges">
    <xsl:param name="srcNode"
      select="concat(ancestor::beans:bean[@id|@name]/@id,ancestor::beans:bean[@id|@name]/@name)" />

    <xsl:choose>
      <xsl:when
        test="(@class='org.openadaptor.core.router.Router') or (@class='org.openadaptor.core.router.Pipeline')">
        <xsl:for-each select="beans:property">

          <!-- Router: Add process routing edge definitions for this node: -->
          <xsl:if test="@name='processMap'">
            <xsl:for-each select="beans:map/beans:entry">
              <xsl:variable name="entryKeyNode" select="@key-ref" />

              <!-- Reference to single processing node (single processing route): -->
              <xsl:if test="@value-ref">
                <xsl:call-template name="routingEdge">
                  <xsl:with-param name="srcNode" select="$entryKeyNode" />
                  <xsl:with-param name="destNode" select="@value-ref" />
                  <xsl:with-param name="edgeTooltipLabel" select="'processRouting'" />
                  <xsl:with-param name="edgeColor" select="'black'" />
                  <xsl:with-param name="edgeLayoutWeight" select="1.0" />
                  <xsl:with-param name="arrowHeadShape" select="'normal'" />
                </xsl:call-template>
              </xsl:if>

              <!-- List of references to next processing nodes (processing fan out): -->
              <xsl:for-each select="beans:list/beans:ref">
                <xsl:call-template name="routingEdge">
                  <xsl:with-param name="srcNode" select="$entryKeyNode" />
                  <xsl:with-param name="destNode" select="@bean" />
                  <xsl:with-param name="edgeTooltipLabel" select="'processRouting'" />
                  <xsl:with-param name="edgeColor" select="'black'" />
                  <xsl:with-param name="edgeLayoutWeight" select="1.0" />
                  <xsl:with-param name="arrowHeadShape" select="'normal'" />
                </xsl:call-template>
              </xsl:for-each>

            </xsl:for-each>

            <!-- Apply bean rules to inline beans in routing properties (treat as top-level beans) -->
            <xsl:apply-templates select="beans:list/beans:bean" />
          </xsl:if>


          <!-- Pipeline: Add process routing edge definitions for this node: -->
          <xsl:if test="@name='processors'">
            <xsl:for-each select="beans:list/beans:ref">
              <!-- Each entry in pipeline list routes to next entry in pipeline list: -->
              <xsl:if test="position()!=last()">
                <xsl:variable name="destPos" select="position()+1" />
                <xsl:call-template name="routingEdge">
                  <xsl:with-param name="srcNode" select="@bean" />
                  <xsl:with-param name="destNode" select="../beans:ref[position()=$destPos]/@bean" />
                  <xsl:with-param name="edgeTooltipLabel" select="'processRouting'" />
                  <xsl:with-param name="edgeColor" select="'black'" />
                  <xsl:with-param name="edgeLayoutWeight" select="1.0" />
                  <xsl:with-param name="arrowHeadShape" select="'normal'" />
                </xsl:call-template>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>


          <!-- Router: Add discard routing edge definitions for this node: -->
          <xsl:if test="@name='discardMap'">
            <xsl:for-each select="beans:map/beans:entry">
              <xsl:variable name="entryKeyNode" select="@key-ref" />
              
              <!-- Reference to single discard node (single discard route): -->
              <xsl:if test="@value-ref">
                <xsl:call-template name="routingEdge">
                  <xsl:with-param name="srcNode" select="$entryKeyNode" />
                  <xsl:with-param name="destNode" select="@value-ref" />
                  <xsl:with-param name="edgeLabel" select="'Discard'" />
                  <xsl:with-param name="edgeTooltipLabel" select="'discardRouting'" />
                  <xsl:with-param name="edgeColor" select="'blue'" />
                  <xsl:with-param name="edgeLayoutWeight" select="0.9" />
                  <xsl:with-param name="arrowHeadShape" select="'vee'" />
                </xsl:call-template>
              </xsl:if>

              <!-- List of references to discard nodes (discard fan out): -->
              <xsl:for-each select="beans:list/beans:ref">
                <xsl:call-template name="routingEdge">
                  <xsl:with-param name="srcNode" select="$entryKeyNode" />
                  <xsl:with-param name="destNode" select="@bean" />
                  <xsl:with-param name="edgeLabel" select="'Discard'" />
                  <xsl:with-param name="edgeTooltipLabel" select="'discardRouting'" />
                  <xsl:with-param name="edgeColor" select="'blue'" />
                  <xsl:with-param name="edgeLayoutWeight" select="0.9" />
                  <xsl:with-param name="arrowHeadShape" select="'vee'" />
                </xsl:call-template>
              </xsl:for-each>

            </xsl:for-each>

            <!-- Apply bean rules to inline beans in routing properties (treat as top-level beans) -->
            <xsl:apply-templates select="beans:list/beans:bean" />
          </xsl:if>


          <!-- Pipeline: does not support Discard routing (so no edges to define here). -->



          <!-- Exception routing: this is all handled using exceptionProcessor since OpenAdaptor 3.3.0 -->
          <xsl:if test="@name='exceptionProcessor'">
            
            <xsl:choose>
            
              <xsl:when test="count(beans:bean/beans:property[@name='exceptionMap']) > 0">
                <!-- add multiple exception edge definitions for this node: -->

                <xsl:for-each select="beans:bean/beans:property[@name='exceptionMap']/beans:map/beans:entry">
                  <xsl:variable name="entryKeyNode" select="@key-ref|@key" />

                  <!--
                    Typically the exceptionMap is a map of nested maps
                    (separate exception map for each node):
                    -->
                  <xsl:for-each select="beans:map/beans:entry">
                    <xsl:choose>

                      <!--
                        If the nested map has a key of "*" in the map of maps
                        then this exception routing is applied to all nodes.
                        Rather than riddling the image with exception lines, we show this by drawing
                        the Router itself as the source node.
                        -->
                      <xsl:when test="$entryKeyNode='*'">
                        <xsl:call-template name="exceptionEdge">
                          <xsl:with-param name="srcNode" select="$srcNode" />
                          <xsl:with-param name="destNode" select="@value-ref" />
                          <xsl:with-param name="edgeTooltipLabel" select="'exceptionRouting'" />
                          <xsl:with-param name="arrowTailShape" select="'dot'" />
                        </xsl:call-template>
                      </xsl:when>

                      <!--
                        Otherwise the nested map has a Node name as a key in the map of maps
                        and so this exception routing is applied just to this named node.
                        We show this by drawing the named node as the source node (obviously ;-).
                        -->
                      <xsl:otherwise>
                        <xsl:call-template name="exceptionEdge">
                          <xsl:with-param name="srcNode" select="$entryKeyNode" />
                          <xsl:with-param name="destNode" select="@value-ref" />
                          <xsl:with-param name="edgeTooltipLabel" select="'exceptionRouting'" />
                          <xsl:with-param name="arrowTailShape" select="'dot'" />
                        </xsl:call-template>
                      </xsl:otherwise>

                    </xsl:choose>
                  </xsl:for-each>

                  <!--
                    Atypically the exceptionMap is NOT a map of maps,
                    and it is just a single unnested map:
                    -->
                  <xsl:if test="not(beans:map/beans:entry)">
                    <!--
                      This single map is treated as if it appeared in a map of maps with a key of "*",
                      i.e. this routing is applied to all nodes.
                      Rather than riddling the image with exception lines, we show this by drawing
                      the Router itself as the source node.
                      -->
                    <xsl:call-template name="exceptionEdge">
                      <xsl:with-param name="srcNode" select="$srcNode" />
                      <xsl:with-param name="destNode" select="@value-ref" />
                      <xsl:with-param name="edgeTooltipLabel" select="'exceptionRouting'" />
                      <xsl:with-param name="arrowTailShape" select="'dot'" />
                    </xsl:call-template>
                  </xsl:if>

                </xsl:for-each>

                <!-- Apply bean rules to inline beans in routing properties (treat as top-level beans) -->
                <xsl:apply-templates select="beans:map/beans:entry/beans:value/beans:bean" />

              </xsl:when>
          
              <xsl:otherwise>
                <!--  add single edge for global exceptionProcessor: -->

                <!--
                  This routing is applied to all nodes.
                  Rather than riddling the image with exception lines, we show this by drawing
                  the Router itself as the source node.
                  -->
                <xsl:variable name="destNode" select="@ref" />
                <xsl:call-template name="exceptionEdge">
                  <xsl:with-param name="srcNode" select="$srcNode" />
                  <xsl:with-param name="destNode" select="$destNode" />
                  <xsl:with-param name="edgeTooltipLabel" select="'exceptionProcessor'" />
                  <xsl:with-param name="arrowTailShape" select="'dot'" />
                </xsl:call-template>

                <!-- Apply bean rules to inline beans in routing properties (treat as top-level beans) -->
                <xsl:apply-templates select="beans:map/beans:entry/beans:value/beans:bean" />

              </xsl:otherwise>

            </xsl:choose>

          </xsl:if>

        </xsl:for-each>
      </xsl:when>

      <!-- Add helper bean reference edge definitions for this node (as not Pipeline/Router): -->
      <xsl:otherwise>
        <xsl:for-each select="beans:property">
          <xsl:for-each select="@ref | beans:ref[@bean]/@bean | @factory-bean">
            <xsl:call-template name="referenceEdge">
              <xsl:with-param name="srcNode" select="$srcNode" />
            </xsl:call-template>
          </xsl:for-each>

          <xsl:for-each select="beans:list/beans:ref/@bean | beans:map/beans:entry/beans:key/beans:ref/@bean | beans:map/beans:entry/@key-ref | beans:map/beans:entry/beans:value/beans:ref/@bean | beans:map/beans:entry/@value-ref">
            <xsl:call-template name="referenceEdge">
              <xsl:with-param name="srcNode" select="$srcNode" />
            </xsl:call-template>
          </xsl:for-each>

          <!-- Apply edge rules to child beans (that are NOT inline beans in routing properties) -->
          <xsl:for-each select="beans:bean | beans:list/beans:bean | beans:map/beans:entry/beans:key/beans:bean | beans:map/beans:entry/beans:value/beans:bean">
            <xsl:call-template name="calculateEdges">
              <xsl:with-param name="srcNode" select="$srcNode" />
            </xsl:call-template>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:otherwise>
      
    </xsl:choose>

    <!-- Check for factory bean reference: -->
    <xsl:for-each select="@factory-bean">
      <xsl:call-template name="referenceEdge">
        <xsl:with-param name="srcNode" select="$srcNode" />
      </xsl:call-template>
    </xsl:for-each>

  </xsl:template>


  <!-- Process and Discard routing: -->
  <xsl:template name="routingEdge">
    <!-- Parameters (and default values for them): -->
    <xsl:param name="srcNode"
      select="concat(ancestor::beans:bean[@id|@name]/@id,ancestor::beans:bean[@id|@name]/@name)" />
    <xsl:param name="destNode" select="concat(@id,@name)" /><!-- list item is an inline bean (unusual) -->
    <xsl:param name="edgeLabel" select="''" />
    <xsl:param name="edgeTooltipLabel" select="''" />
    <xsl:param name="edgeColor" select="'black'" />
    <xsl:param name="edgeStyle" select="'solid'" />
    <xsl:param name="edgeLayoutWeight" select="1.0" />
    <xsl:param name="arrowHeadShape" select="'normal'" />

    <xsl:call-template name="genericEdge">
      <xsl:with-param name="srcNode" select="$srcNode" />
      <xsl:with-param name="destNode" select="$destNode" />
      <xsl:with-param name="edgeTooltipLabel" select="concat($srcNode, '.', $edgeTooltipLabel)" />
      <xsl:with-param name="edgeLabel" select="$edgeLabel" />
      <xsl:with-param name="edgeColor" select="$edgeColor" />
      <xsl:with-param name="edgeStyle" select="$edgeStyle" />
      <xsl:with-param name="edgeLayoutWeight" select="$edgeLayoutWeight" />
      <xsl:with-param name="arrowHeadShape" select="$arrowHeadShape" />
      <xsl:with-param name="arrowTailShape" select="'none'" />
    </xsl:call-template>
  </xsl:template>


  <!-- Exception routing: -->
  <xsl:template name="exceptionEdge">
    <!-- Parameters (and default values for them): -->
    <xsl:param name="srcNode"
      select="concat(ancestor::beans:bean[@id|@name]/@id,ancestor::beans:bean[@id|@name]/@name)" />
    <xsl:param name="destNode" select="concat(beans:value/@id, beans:value/@name)" />
    <!-- list item is an inline bean (unusual) -->
    <xsl:param name="edgeTooltipLabel" select="''" />
    <xsl:param name="edgeStyle" select="'solid'" />
    <xsl:param name="arrowTailShape" select="'none'" />

    <xsl:call-template name="genericEdge">
      <xsl:with-param name="srcNode" select="$srcNode" />
      <xsl:with-param name="destNode" select="$destNode" />
      <xsl:with-param name="edgeLabel">
        <xsl:value-of select="@key" />
        <xsl:value-of select="beans:key/@value" />
        <xsl:value-of select="beans:key/beans:value" />
      </xsl:with-param>
      <xsl:with-param name="edgeTooltipLabel" select="concat($srcNode, '.', $edgeTooltipLabel)" />
      <xsl:with-param name="edgeColor" select="'red'" />
      <xsl:with-param name="edgeStyle" select="$edgeStyle" />
      <xsl:with-param name="edgeLayoutWeight" select="0.5" />
      <xsl:with-param name="arrowHeadShape" select="'vee'" />
      <xsl:with-param name="arrowTailShape" select="$arrowTailShape" />
    </xsl:call-template>
  </xsl:template>


  <!-- Reference edges: -->
  <xsl:template name="referenceEdge">
    <!-- Parameters (and default values for them): -->
    <xsl:param name="srcNode"
      select="concat(ancestor::beans:bean[@id|@name]/@id,ancestor::beans:bean[@id|@name]/@name)" />

    <xsl:variable name="compoundPropertyName">
      <xsl:for-each select="ancestor-or-self::beans:property[@name]">
        <xsl:text>.</xsl:text>
        <xsl:value-of select="@name" />
      </xsl:for-each>
      <xsl:if test=". = ../@factory-bean">
        <xsl:text>@factory-bean</xsl:text>
      </xsl:if>
    </xsl:variable>

    <!-- Proxy components use "target" and "targetNode" properties: typically these relate to dataflow: -->
    <xsl:variable name="style">
      <xsl:variable name="len" select="string-length($compoundPropertyName)" />
      <xsl:choose>
        <xsl:when test="($len >= 7) and contains(substring($compoundPropertyName, $len - 6, 7), '.target')">
          <xsl:text>solid</xsl:text>
        </xsl:when>
        <xsl:when test="($len >= 11) and contains(substring($compoundPropertyName, $len - 10, 11), '.targetNode')">
          <xsl:text>solid</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>dashed</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:call-template name="genericEdge">
      <xsl:with-param name="srcNode" select="$srcNode" />
      <xsl:with-param name="destNode" select="." />
      <xsl:with-param name="edgeLabel" select="'Ref'" />
      <xsl:with-param name="edgeTooltipLabel" select="concat($srcNode, $compoundPropertyName)" />
      <xsl:with-param name="edgeColor" select="'black'" />
      <xsl:with-param name="edgeStyle" select="$style" />
      <xsl:with-param name="edgeLayoutWeight" select="0.3" />
      <xsl:with-param name="arrowHeadShape" select="'vee'" />
      <xsl:with-param name="arrowTailShape" select="'odot'" />
    </xsl:call-template>
  </xsl:template>


  <!-- Generic edges (routing and references): -->
  <xsl:template name="genericEdge">
    <!-- Parameters (and default values for them): -->
    <xsl:param name="srcNode"
      select="concat(ancestor::beans:bean[@id|@name]/@id,ancestor::beans:bean[@id|@name]/@name)" />
    <xsl:param name="destNode">
      <xsl:value-of select="beans:ref/@bean" />
      <xsl:value-of select="concat(@id,@name)" />
    </xsl:param>
    <xsl:param name="edgeLabel" select="''" />
    <xsl:param name="edgeTooltipLabel" select="$srcNode" />
    <xsl:param name="edgeColor" select="'black'" />
    <xsl:param name="edgeStyle" select="'solid'" />
    <xsl:param name="edgeLayoutWeight" select="1.0" />
    <xsl:param name="arrowHeadShape" select="'normal'" />
    <xsl:param name="arrowTailShape" select="'none'" />
    <xsl:param name="layoutConstraint" select="true()" />

    <!-- Define src/dest (and so direction) of edge: -->
    <xsl:text>"</xsl:text>
    <xsl:value-of select="translate($srcNode, '-', '_')" />
    <xsl:text>"</xsl:text>
    <xsl:text>-&gt;</xsl:text>
    <xsl:text>"</xsl:text>
    <xsl:value-of select="translate($destNode, '-', '_')" />
    <xsl:text>"</xsl:text>

    <!-- Begin attribute list for edge: -->
    <xsl:text>[</xsl:text>

    <xsl:if test="$edgeLabel != ''">
      <xsl:text>label=&lt;</xsl:text>
      <xsl:value-of select="$edgeLabel" />
      <xsl:text>&gt;,</xsl:text>
      <xsl:text>fontcolor=</xsl:text>
      <xsl:value-of select="$edgeColor" />
      <xsl:text>,</xsl:text>
    </xsl:if>

    <!-- Edge URL: -->
    <xsl:if test="$srcNode != ''">
      <xsl:text>URL="</xsl:text>
      <xsl:value-of select="concat($baseRelativeDotDot, $exampleName, '.html#', $srcNode)" />
      <xsl:text>",</xsl:text>
      <xsl:text>tooltip="</xsl:text>
      <xsl:value-of select="$edgeTooltipLabel" />
      <xsl:text>",</xsl:text>
    </xsl:if>

    <xsl:text>color=</xsl:text>
    <xsl:value-of select="$edgeColor" />
    <xsl:text>,</xsl:text>

    <xsl:text>style=</xsl:text>
    <xsl:value-of select="$edgeStyle" />
    <xsl:text>,</xsl:text>

    <xsl:text>weight=</xsl:text>
    <xsl:value-of select="$edgeLayoutWeight" />
    <xsl:text>,</xsl:text>

    <xsl:text>arrowhead=</xsl:text>
    <xsl:value-of select="$arrowHeadShape" />
    <xsl:text>,</xsl:text>

    <xsl:text>arrowtail=</xsl:text>
    <xsl:value-of select="$arrowTailShape" />
    <xsl:text>,</xsl:text>

    <xsl:text>constraint=</xsl:text>
    <xsl:value-of select="$layoutConstraint" />
    <xsl:text>,</xsl:text>

    <!-- End attribute list for edge: -->
    <xsl:text>];
</xsl:text>
  </xsl:template>

  <xsl:template name="substring-after-last">
    <xsl:param name="input" />
    <xsl:param name="substr" />

    <!-- Extract the string which comes after the first occurence -->
    <xsl:variable name="temp" select="substring-after($input,$substr)" />

    <xsl:choose>
      <!-- If it still contains the search string then recursively process -->
      <xsl:when test="$substr and contains($temp,$substr)">
        <xsl:call-template name="substring-after-last">
          <xsl:with-param name="input" select="$temp" />
          <xsl:with-param name="substr" select="$substr" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$temp" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
