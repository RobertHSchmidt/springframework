<?xml version="1.0"?>
<!-- 
    This is the XSL HTML configuration file for the Spring
    Reference Documentation.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY db_xsl_path        "../lib/docbook-xsl/">
    <!ENTITY callout_gfx_path   "../images/callouts/">
    <!ENTITY admon_gfx_path     "../images/admons/">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default">
                
<xsl:import href="&db_xsl_path;/html/onechunk.xsl"/>

<!--###################################################
                     HTML Settings
    ################################################### -->   

    <xsl:param name="html.stylesheet">styles/html.css</xsl:param>

    <!-- These extensions are required for table printing and other stuff -->
    <xsl:param name="use.extensions">1</xsl:param>
    <xsl:param name="tablecolumns.extension">0</xsl:param>
    <xsl:param name="callout.extensions">1</xsl:param>
    <xsl:param name="graphicsize.extension">0</xsl:param>

<!--###################################################
                      Table Of Contents
    ################################################### -->   

    <!-- Generate the TOCs for named components only -->
    <xsl:param name="generate.toc">
        book   toc
    </xsl:param>
    
    <!-- Show only Sections up to level 3 in the TOCs -->
    <xsl:param name="toc.section.depth">3</xsl:param>
    
<!--###################################################
                         Labels
    ################################################### -->   

    <!-- Label Chapters and Sections (numbering) -->
    <xsl:param name="chapter.autolabel">1</xsl:param>
    <xsl:param name="section.autolabel" select="1"/>
    <xsl:param name="section.label.includes.component.label" select="1"/>
        
<!--###################################################
                         Callouts
    ################################################### -->   

    <!-- Use images for callouts instead of (1) (2) (3) -->
    <xsl:param name="callout.graphics">1</xsl:param>
    <xsl:param name="callout.graphics.path">&callout_gfx_path;</xsl:param>
    
    <!-- Place callout marks at this column in annotated areas -->
    <xsl:param name="callout.defaultcolumn">90</xsl:param>

<!--###################################################
                       Admonitions
    ################################################### -->   

    <!-- Use nice graphics for admonitions -->
    <xsl:param name="admon.graphics">'1'</xsl:param>
    <xsl:param name="admon.graphics.path">&admon_gfx_path;</xsl:param>

	<!--###################################################
                          Misc
    ################################################### -->
	<!-- Placement of titles -->
	<xsl:param name="formal.title.placement">
        figure after
        example before
        equation before
        table before
        procedure before
    </xsl:param>
	<xsl:template match="author" mode="titlepage.mode">
		<xsl:if test="name(preceding-sibling::*[1]) = 'author'">
			<xsl:text>, </xsl:text>
		</xsl:if>
		<span class="{name(.)}">
			<xsl:call-template name="person.name" />
			<xsl:apply-templates mode="titlepage.mode" select="./contrib" />
			<xsl:apply-templates mode="titlepage.mode" select="./affiliation" />
		</span>
	</xsl:template>
	<xsl:template match="authorgroup" mode="titlepage.mode">
		<div class="{name(.)}">
			<h2>Authors</h2>
			<p/>
			<xsl:apply-templates mode="titlepage.mode" />
		</div>
	</xsl:template>

    <!--###################################################
                     Headers and Footers
    ################################################### -->
    <!-- let's have a Spring and I21 banner across the top of each page -->
    <xsl:template name="user.header.navigation">
        <div style="background-color:white;border:none;height:73px;border:1px solid black;">
            <a style="border:none;" href="http://www.springframework.org/" title="The Spring Framework">
                <img style="border:none;" src="images/xdev-spring_logo.jpg" />
            </a>
            <a style="border:none;" href="http://www.interface21.com/" title="Interface21 - Spring from the Source">
                <img style="border:none;position:absolute;padding-top:5px;right:42px;" src="images/i21-banner-rhs.jpg" />
            </a>
        </div>
    </xsl:template>
    <!-- no other header navigation (prev, next, etc.) -->
    <xsl:template name="header.navigation" />
    <xsl:param name="navig.showtitles">1</xsl:param>
    <!-- let's have a 'Sponsored by Interface21' strapline (or somesuch) across the bottom of each page -->
    <xsl:template name="footer.navigation">
        <xsl:param name="prev" select="/foo" />
        <xsl:param name="next" select="/foo" />
        <xsl:param name="nav.context" />
        <xsl:variable name="home" select="/*[1]" />
        <xsl:variable name="up" select="parent::*" />
        <xsl:variable name="row1" select="count($prev) &gt; 0
                                        or count($up) &gt; 0
                                        or count($next) &gt; 0" />
        <xsl:variable name="row2" select="($prev and $navig.showtitles != 0)
                                        or (generate-id($home) != generate-id(.)
                                            or $nav.context = 'toc')
                                        or ($chunk.tocs.and.lots != 0
                                            and $nav.context != 'toc')
                                        or ($next and $navig.showtitles != 0)" />
        <xsl:if test="$suppress.navigation = '0' and $suppress.footer.navigation = '0'">
            <div class="navfooter">
                <xsl:if test="$footer.rule != 0">
                    <hr />
                </xsl:if>
                <xsl:if test="$row1 or $row2">
                    <table width="100%" summary="Navigation footer">
                        <xsl:if test="$row1">
                            <tr>
                                <td width="40%" align="left" valign="top">
                                    <xsl:if test="$navig.showtitles != 0">
                                        <xsl:apply-templates select="$prev" mode="object.title.markup" />
                                    </xsl:if>
                                    <xsl:text>&#160;</xsl:text>
                                </td>
                                <td width="20%" align="center">
                                    <span style="color:white;font-size:90%;">
                                        <a href="http://www.interface21.com/" title="Interface21 - Spring from the Source">Sponsored by Interface21</a>
                                    </span>
                                </td>
                                <td width="40%" align="right" valign="top">
                                    <xsl:text>&#160;</xsl:text>
                                    <xsl:if test="$navig.showtitles != 0">
                                        <xsl:apply-templates select="$next" mode="object.title.markup" />
                                    </xsl:if>
                                </td>
                            </tr>
                        </xsl:if>
                    </table>
                </xsl:if>
            </div>
        </xsl:if>
    </xsl:template>
  
</xsl:stylesheet>
