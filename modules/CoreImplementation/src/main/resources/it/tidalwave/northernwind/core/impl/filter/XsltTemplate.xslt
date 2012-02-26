<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dyn="http://exslt.org/dynamic"
                xmlns:math="http://exslt.org/math"
                xmlns:str="http://exslt.org/strings"
                xmlns:set="http://exslt.org/sets"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:pom="http://maven.apache.org/POM/4.0.0">
    <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

    <!-- ***************************************************************************************************************
    *
    *
    *
    **************************************************************************************************************** -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- ***************************************************************************************************************
    *
    * Ensures that text sections are always properly escaped.
    *
    **************************************************************************************************************** -->
    <xsl:template match="text()" >
        <xsl:call-template name="escape">
            <xsl:with-param name="string" select="."/>
        </xsl:call-template>
    </xsl:template>    
    
    <!-- ***************************************************************************************************************
    *
    * Ensures that script sections are not escaped and wrapped by a CDATA.
    *
    **************************************************************************************************************** -->
    <xsl:template match="script"> 
        <xsl:copy> 
            <xsl:apply-templates select="@*"/> 
            <xsl:copy-of select="*"/>
            <xsl:if test='string-length(.) &gt; 0'>
                <xsl:value-of select="'&#10;//&lt;![CDATA['"/>
                <xsl:value-of select="."/> 
                <xsl:value-of select="'&#10;//]]&gt;&#10;'"/>
            </xsl:if>
        </xsl:copy> 
     </xsl:template>
     
    <!-- ***************************************************************************************************************
    *
    *
    *
    **************************************************************************************************************** -->
    <xsl:template name="escape">
        <xsl:param name="string"/>
        
        <xsl:call-template name="x-replace-substring">
            <xsl:with-param name="original">
                <xsl:call-template name="x-replace-substring">
                    <xsl:with-param name="original">
                        <xsl:call-template name="x-replace-substring">
                            <xsl:with-param name="original" select="$string"/>
                            <xsl:with-param name="substring" select="'&amp;'"/>
                            <xsl:with-param name="replacement" select="'&amp;amp;'"/>
                        </xsl:call-template>
                    </xsl:with-param>
                    <xsl:with-param name="substring" select="'&lt;'"/>
                    <xsl:with-param name="replacement" select="'&amp;lt;'"/>
                </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="substring" select="'&gt;'"/>
            <xsl:with-param name="replacement" select="'&amp;gt;'"/>
        </xsl:call-template>
    </xsl:template>
            
    <!-- ***************************************************************************************************************
    *
    *
    *
    **************************************************************************************************************** -->
    <xsl:template name="x-replace-substring">
        <xsl:param name="original"/>
        <xsl:param name="substring"/>
        <xsl:param name="replacement" select="''"/>
        
        <xsl:choose>
            <xsl:when test="contains($original,$substring)">
                <xsl:value-of select="substring-before($original, $substring)"/>
                <xsl:copy-of select="$replacement"/>
                <xsl:call-template name="x-replace-substring">
                    <xsl:with-param name="original" select="substring-after($original, $substring)"/>
                    <xsl:with-param name="substring" select="$substring"/>
                    <xsl:with-param name="replacement" select="$replacement"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$original"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    %content%

</xsl:stylesheet>
