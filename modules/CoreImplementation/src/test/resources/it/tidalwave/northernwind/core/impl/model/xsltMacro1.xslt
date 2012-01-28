<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="no"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="div[@class='nwXsltMacro.Photo']">
        <xsl:element name="div" namespace="">
            <xsl:attribute name="align">center</xsl:attribute>
            <xsl:element name="a">
                <xsl:attribute name="title">
                    <xsl:value-of select="p[@class='nwXsltMacro.Photo.caption']"/>
                </xsl:attribute>
                <xsl:attribute name="rel">lightbox</xsl:attribute>
                <xsl:attribute name="href">$mediaLink(relativePath='/stillimages/1280/<xsl:value-of select="p[@class='nwXsltMacro.Photo.photoId']"/>.jpg')$</xsl:attribute>
                <xsl:element name="img">
                    <xsl:attribute name="src">$mediaLink(relativePath='/stillimages/800/<xsl:value-of select="p[@class='nwXsltMacro.Photo.photoId']"/>.jpg')$</xsl:attribute>
                    <xsl:attribute name="class">framedPhoto</xsl:attribute>
                </xsl:element>
            </xsl:element>
            <xsl:element name="p">
                <xsl:attribute name="class">caption</xsl:attribute>
                <xsl:value-of select="p[@class='nwXsltMacro.Photo.caption']"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
