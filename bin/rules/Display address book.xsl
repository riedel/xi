<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xi="http://ananas.org/2002/xi/rules"
                xmlns:an="http://ananas.org/2002/sample">

<xi:rules version="1.0"
          defaultPrefix="an"
          targetNamespace="http://ananas.org/2002/sample">

<xi:ruleset name="address-book">
   <xi:match name="alias"
             pattern="^alias ([^\s]*) (.*)$">
      <xi:group name="id"/>
      <xi:group name="email"/>
   </xi:match>
   <xi:match name="note"
             pattern='^note ([^\s]*) (.*)$'>
      <xi:group name="id"/>
      <xi:group name="fields"/>
   </xi:match>
   <xi:error message="unknown line type"/>
</xi:ruleset>

<xi:ruleset name="fields">
   <xi:match name="fields"
             pattern="[\s]*&lt;([^&lt;]*)&gt;">
      <xi:group name="field"/>
   </xi:match>
</xi:ruleset>

<xi:ruleset name="field">
   <xi:match name="field"
             pattern="([^:]*):(.*)">
      <xi:group name="key"/>
      <xi:group name="value"/>
   </xi:match>
</xi:ruleset>

</xi:rules>

<xsl:output method="html"/>

<xsl:template match="an:address-book">
   <html>
      <head><title>Address book</title></head>
      <h1>Address book</h1>
      <xsl:apply-templates/>
   </html>
</xsl:template>

<xsl:template match="an:alias">
   <p>
      <xsl:variable name="id" select="an:id"/>
      <xsl:for-each select="/an:address-book/an:note[an:id = $id]">
         <xsl:value-of select="an:fields/an:field[an:key='name']/an:value"/><br/>
         <xsl:if test="an:fields/an:field[an:key='country']/an:value">
            <xsl:value-of select="an:fields/an:field[an:key='address']/an:value"/><br/>
            <xsl:value-of select="an:fields/an:field[an:key='zip']/an:value"/><br/>
            <xsl:value-of select="an:fields/an:field[an:key='city']/an:value"/><br/>
            <xsl:value-of select="an:fields/an:field[an:key='state']/an:value"/><br/>
            <xsl:value-of select="an:fields/an:field[an:key='country']/an:value"/><br/>
         </xsl:if>
      </xsl:for-each>
      <xsl:value-of select="an:email"/><br/>
   </p>
</xsl:template>

<xsl:template match="an:note"/>

</xsl:stylesheet>
