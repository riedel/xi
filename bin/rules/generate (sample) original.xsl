<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xi="http://ananas.org/2002/xi/rules"
                xmlns:flat="http://ananas.org/2003/xi/flat"
                xmlns:an="http://ananas.org/2002/sample">

<!-- this is far from being complete, it's just for illustration purposes -->

<xsl:output method="xi:flat"/>

<xsl:template match="Interchange">
   <flat:root default-align="right" default-padding="'">
      <xsl:for-each select="Form">
         <flat:field width="8">DEX051</flat:field>
         <flat:field>E</flat:field>
         <flat:field width="11" align/>   <!-- filler -->
         <flat:field width="8">
            <xsl:call-template name="format-date">
               <xsl:with-param name="date" select="DocumentDate"/>
            </xsl:call-template>
         </flat:field>
         <flat:field width="14"/>
         <flat:field width="3">T<xsl:value-of select="SendingInstitution/Country"/></flat:field>
         <flat:field width="35"/>
         <flat:field width="3">T<xsl:value-of select="SendingInstitution/InstitutionCode"/></flat:field>
         <flat:br/>   <!-- this inserts a newline, marks end of record -->
      </xsl:for-each>
   </flat:root>
</xsl:template>

<xsl:template name="format-date"> 
   <xsl:param name="date"/>
   <xsl:value-of select="substring($date,1,4)"/>
   <xsl:value-of select="substring($date,6,2)"/>
   <xsl:value-of select="substring($date,9,2)"/>
</xsl:template>


</xsl:stylesheet>
