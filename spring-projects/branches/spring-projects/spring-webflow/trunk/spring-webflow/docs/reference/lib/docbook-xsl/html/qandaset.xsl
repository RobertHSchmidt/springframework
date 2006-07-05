<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:doc="http://nwalsh.com/xsl/documentation/1.0"
                exclude-result-prefixes="doc"
                version='1.0'>

<!-- ********************************************************************
     $Id: qandaset.xsl,v 1.1 2003/03/27 23:07:21 turin42 Exp $
     ********************************************************************

     This file is part of the XSL DocBook Stylesheet distribution.
     See ../README or http://nwalsh.com/docbook/xsl/ for copyright
     and other information.

     ******************************************************************** -->

<!-- ==================================================================== -->

<xsl:template match="qandaset">
  <xsl:variable name="title" select="title"/>
  <xsl:variable name="preamble" select="*[name(.) != 'title'
                                          and name(.) != 'titleabbrev'
                                          and name(.) != 'qandadiv'
                                          and name(.) != 'qandaentry']"/>
  <xsl:variable name="label-width">
    <xsl:call-template name="dbhtml-attribute">
      <xsl:with-param name="pis"
                      select="processing-instruction('dbhtml')"/>
      <xsl:with-param name="attribute" select="'label-width'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="table-summary">
    <xsl:call-template name="dbhtml-attribute">
      <xsl:with-param name="pis"
                      select="processing-instruction('dbhtml')"/>
      <xsl:with-param name="attribute" select="'table-summary'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="cellpadding">
    <xsl:call-template name="dbhtml-attribute">
      <xsl:with-param name="pis"
                      select="processing-instruction('dbhtml')"/>
      <xsl:with-param name="attribute" select="'cellpadding'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="cellspacing">
    <xsl:call-template name="dbhtml-attribute">
      <xsl:with-param name="pis"
                      select="processing-instruction('dbhtml')"/>
      <xsl:with-param name="attribute" select="'cellspacing'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="toc">
    <xsl:call-template name="dbhtml-attribute">
      <xsl:with-param name="pis"
                      select="processing-instruction('dbhtml')"/>
      <xsl:with-param name="attribute" select="'toc'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="toc.params">
    <xsl:call-template name="find.path.params">
      <xsl:with-param name="table" select="normalize-space($generate.toc)"/>
    </xsl:call-template>
  </xsl:variable>

  <div class="{name(.)}">
    <xsl:apply-templates select="$title"/>
    <xsl:if test="contains($toc.params, 'toc') and $toc != '0'">
      <xsl:call-template name="process.qanda.toc"/>
    </xsl:if>
    <xsl:apply-templates select="$preamble"/>
    <table border="0" summary="Q and A Set">
      <xsl:if test="$table-summary != ''">
        <xsl:attribute name="summary">
          <xsl:value-of select="$table-summary"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="$cellpadding != ''">
        <xsl:attribute name="cellpadding">
          <xsl:value-of select="$cellpadding"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="$cellspacing != ''">
        <xsl:attribute name="cellspacing">
          <xsl:value-of select="$cellspacing"/>
        </xsl:attribute>
      </xsl:if>

      <col align="left">
        <xsl:attribute name="width">
          <xsl:choose>
            <xsl:when test="$label-width != ''">
              <xsl:value-of select="$label-width"/>
            </xsl:when>
            <xsl:otherwise>1%</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </col>
      <tbody>
        <xsl:apply-templates select="qandaentry|qandadiv"/>
      </tbody>
    </table>
  </div>
</xsl:template>

<xsl:template match="qandaset/title">
  <xsl:variable name="qalevel">
    <xsl:call-template name="qanda.section.level"/>
  </xsl:variable>
  <xsl:element name="h{string(number($qalevel)+1)}">
    <xsl:attribute name="class">
      <xsl:value-of select="name(.)"/>
    </xsl:attribute>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="qandadiv">
  <xsl:variable name="preamble" select="*[name(.) != 'title'
                                          and name(.) != 'titleabbrev'
                                          and name(.) != 'qandadiv'
                                          and name(.) != 'qandaentry']"/>

  <xsl:if test="title">
    <tr class="qandadiv">
      <td align="left" valign="top" colspan="2">
        <xsl:call-template name="anchor">
          <xsl:with-param name="conditional" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates select="title"/>
      </td>
    </tr>
  </xsl:if>

  <xsl:variable name="toc.params">
    <xsl:call-template name="find.path.params">
      <xsl:with-param name="table" select="normalize-space($generate.toc)"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:if test="contains($toc.params, 'toc')">
    <tr class="toc" colspan="2">
      <td align="left" valign="top" colspan="2">
        <xsl:call-template name="process.qanda.toc"/>
      </td>
    </tr>
  </xsl:if>
  <xsl:if test="$preamble">
    <tr class="toc" colspan="2">
      <td align="left" valign="top" colspan="2">
        <xsl:apply-templates select="$preamble"/>
      </td>
    </tr>
  </xsl:if>
  <xsl:apply-templates select="qandadiv|qandaentry"/>
</xsl:template>

<xsl:template match="qandadiv/title">
  <xsl:variable name="qalevel">
    <xsl:call-template name="qandadiv.section.level"/>
  </xsl:variable>

  <xsl:element name="h{string(number($qalevel)+1)}">
    <xsl:attribute name="class">
      <xsl:value-of select="name(.)"/>
    </xsl:attribute>
    <xsl:call-template name="anchor">
      <xsl:with-param name="node" select=".."/>
      <xsl:with-param name="conditional" select="0"/>
    </xsl:call-template>
    <xsl:apply-templates select="parent::qandadiv" mode="label.markup"/>
    <xsl:value-of select="$autotoc.label.separator"/>
    <xsl:text> </xsl:text>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="qandaentry">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="question">
  <xsl:variable name="deflabel">
    <xsl:choose>
      <xsl:when test="ancestor-or-self::*[@defaultlabel]">
        <xsl:value-of select="(ancestor-or-self::*[@defaultlabel])[last()]
                              /@defaultlabel"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="qanda.defaultlabel"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <tr class="{name(.)}">
    <td align="left" valign="top">
      <xsl:call-template name="anchor">
        <xsl:with-param name="node" select=".."/>
        <xsl:with-param name="conditional" select="0"/>
      </xsl:call-template>
      <xsl:call-template name="anchor">
        <xsl:with-param name="conditional" select="0"/>
      </xsl:call-template>

      <b>
        <xsl:apply-templates select="." mode="label.markup"/>
        <xsl:text>. </xsl:text> <!-- FIXME: Hack!!! This should be in the locale! -->
      </b>
    </td>
    <td align="left" valign="top">
      <xsl:choose>
        <xsl:when test="$deflabel = 'none' and not(label)">
          <b><xsl:apply-templates select="*[name(.) != 'label']"/></b>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="*[name(.) != 'label']"/>
        </xsl:otherwise>
      </xsl:choose>
    </td>
  </tr>
</xsl:template>

<xsl:template match="answer">
  <tr class="{name(.)}">
    <td align="left" valign="top">
      <xsl:call-template name="anchor"/>
      <b>
        <!-- FIXME: Hack!!! This should be in the locale! -->
        <xsl:variable name="answer.label">
          <xsl:apply-templates select="." mode="label.markup"/>
        </xsl:variable>
        <xsl:copy-of select="$answer.label"/>
        <xsl:if test="string($answer.label) != ''">
          <xsl:text>. </xsl:text>
        </xsl:if>
      </b>
    </td>
    <td align="left" valign="top">
      <xsl:apply-templates select="*[name(.) != 'label']"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="label">
  <xsl:apply-templates/>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="process.qanda.toc">
  <dl>
    <xsl:apply-templates select="qandadiv" mode="qandatoc.mode"/>
    <xsl:apply-templates select="qandaentry" mode="qandatoc.mode"/>
  </dl>
</xsl:template>

<xsl:template match="qandadiv" mode="qandatoc.mode">
  <dt><xsl:apply-templates select="title" mode="qandatoc.mode"/></dt>
  <dd><xsl:call-template name="process.qanda.toc"/></dd>
</xsl:template>

<xsl:template match="qandadiv/title" mode="qandatoc.mode">
  <xsl:variable name="qalevel">
    <xsl:call-template name="qandadiv.section.level"/>
  </xsl:variable>
  <xsl:variable name="id">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="parent::*"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:apply-templates select="parent::qandadiv" mode="label.markup"/>
  <xsl:value-of select="$autotoc.label.separator"/>
  <xsl:text> </xsl:text>
  <a>
    <xsl:attribute name="href">
      <xsl:call-template name="href.target">
        <xsl:with-param name="object" select="parent::*"/>
      </xsl:call-template>
    </xsl:attribute>
    <xsl:apply-templates/>
  </a>
</xsl:template>

<xsl:template match="qandaentry" mode="qandatoc.mode">
  <xsl:apply-templates mode="qandatoc.mode"/>
</xsl:template>

<xsl:template match="question" mode="qandatoc.mode">
  <xsl:variable name="firstch" select="(*[name(.)!='label'])[1]"/>

  <dt>
    <xsl:apply-templates select="." mode="label.markup"/>
    <xsl:text>. </xsl:text> <!-- FIXME: Hack!!! This should be in the locale! -->
    <a>
      <xsl:attribute name="href">
        <xsl:call-template name="href.target">
          <xsl:with-param name="object" select=".."/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:value-of select="$firstch"/>
    </a>
  </dt>
</xsl:template>

<xsl:template match="answer|revhistory" mode="qandatoc.mode">
  <!-- nop -->
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="*" mode="no.wrapper.mode">
  <xsl:apply-templates/>
</xsl:template>

<!-- ==================================================================== -->

</xsl:stylesheet>
