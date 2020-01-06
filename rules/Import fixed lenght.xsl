<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xi="http://ananas.org/2002/xi/rules"
                xmlns:an="http://ananas.org/2002/sample">

<xi:rules version="1.0"
          defaultPrefix="an"
          targetNamespace="http://ananas.org/2002/sample">

<xi:ruleset name="address-book">
   <xi:match name="line"
             pattern="^(.*)$">
      <xi:group name="line-fields"/>
   </xi:match>
   <xi:error message="unknown line type"/>
</xi:ruleset>

<xi:ruleset name="line-fields">
   <xi:match name="fields"
             pattern='^(.{9})(.{35})(.*)$'>
      <xi:group name="id"/>
      <xi:group name="name"/>
      <xi:group name="code"/>
   </xi:match>
</xi:ruleset>

</xi:rules>

<xsl:output method="xml"/>

<xsl:template match="@*|node()">
   <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
</xsl:template>

</xsl:stylesheet>
