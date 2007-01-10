<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/MessageException">
  <style type="text/css">
    li { list-style-type: none}
    .title { background-color: CCCCCC; vertical-align: top}
  </style>
  <html>
  <body>
    <table>
      <tr><td class="title">Message:</td><td><xsl:value-of select="Exception/Message"/></td></tr>
      <tr><td class="title">Exception:</td><td><xsl:value-of select="Exception/@class"/></td></tr>
      <tr><td class="title">From:</td><td><xsl:value-of select="From"/>(<xsl:value-of select="HostName"/>)</td></tr>
      <tr><td class="title">Trace:</td>
        <td>
          <xsl:for-each select="Exception/StackTrace/Line">
            <li><xsl:value-of select="."/></li>
          </xsl:for-each>
        </td>
      </tr>
      <tr><td class="title">Data:</td><td><xsl:value-of select="Data"/></td></tr>
      <tr><td class="title">Reply:</td><td><xsl:value-of select="ReplyTo"/></td></tr>
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
