<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:web="http://java.sun.com/xml/ns/javaee" >
    <xsl:output method="xml" indent="yes" omit-xml-declaration="no"/>

    <xsl:template match="web:fragments">
        <xsl:for-each select="web:fragment">
            <xsl:variable name="fragment" select="."/>
            <xsl:variable name="path">
                <xsl:text>../webapp-fragments/</xsl:text>
                <xsl:value-of select="$fragment"/>
                <xsl:text>.xml</xsl:text>
            </xsl:variable>
            <xsl:comment> <xsl:value-of select="$fragment"/> </xsl:comment>
            <xsl:for-each select="document($path)/fragment">
                <xsl:apply-templates select="@* | node()"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="comment()">
    </xsl:template>

</xsl:stylesheet> 

