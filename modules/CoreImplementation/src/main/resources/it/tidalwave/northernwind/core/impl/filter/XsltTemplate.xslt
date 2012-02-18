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

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    $content$

</xsl:stylesheet>