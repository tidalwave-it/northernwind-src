<xsl:template match="div[@class='nwXsltMacro.Photo']">
    <xsl:element name="div" namespace="">
        <xsl:attribute name="align">center</xsl:attribute>
        <xsl:element name="a">
            <xsl:attribute name="title">
                <xsl:value-of select="p[@class='nwXsltMacro.Photo.caption']"/>
            </xsl:attribute>
            <xsl:attribute name="rel">lightbox</xsl:attribute>
            <xsl:attribute name="href">$mediaLink(relativePath='/stillimages/<xsl:value-of select="p[@class='nwXsltMacro.Photo.photoId']"/>/1280/image.jpg')$</xsl:attribute>
            <xsl:element name="img">
                <xsl:attribute name="src">$mediaLink(relativePath='/stillimages/<xsl:value-of select="p[@class='nwXsltMacro.Photo.photoId']"/>/800/image.jpg')$</xsl:attribute>
                <xsl:attribute name="class">framedPhoto</xsl:attribute>
            </xsl:element>
        </xsl:element>
        <xsl:element name="p">
            <xsl:attribute name="class">caption</xsl:attribute>
            <xsl:value-of select="p[@class='nwXsltMacro.Photo.caption']"/>
        </xsl:element>
    </xsl:element>
</xsl:template>
