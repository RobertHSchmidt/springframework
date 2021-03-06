<%@ page contentType="text/html" %>
<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<html>
<head>
<title>Sell Item - Enter Shipping Information</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="style.css" type="text/css">
</head>
<body>

<div id="logo">
	<img src="images/spring-logo.jpg" alt="Logo" border="0"> 
</div>

<f:view>
<div id="content">
	<div id="insert"><img src="images/webflow-logo.jpg"/></div>
	<h2>Enter shipping information</h2>
	<hr>
	<table>
	<tr class="readOnly">
		<td>Price:</td><td><h:outputText value="#{sale.price}"/></td>
	</tr>
	<tr class="readOnly">
		<td>Item count:</td><td><h:outputText value="#{sale.itemCount}"/></td>
	</tr>
	<tr class="readOnly">
		<td>Category:</td><td><h:outputText value="#{sale.category}"/></td>
	<tr class="readOnly">
		<td>Shipping:</td><td><h:outputText value="#{sale.shipping}"/></td>
	</tr>
	
	<h:form id="shippingForm">
		<tr>
			<td>Shipping type:</td>
			<td>
				<h:selectOneMenu value="#{sale.shippingType}">
					<f:selectItem itemLabel="Standard (10 extra cost)" itemValue="S"/>
					<f:selectItem itemLabel="Express (20 extra cost)" itemValue="E"/>
				</h:selectOneMenu>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<h:commandButton type="submit" value="Next" action="submit" immediate="false" />
			</td>
		</tr>
		</h:form>
	</table>
</div>	
</f:view>

<div id="copyright">
	<p>&copy; Copyright 2004-2007, <a href="http://www.springframework.org">www.springframework.org</a>, under the terms of the Apache 2.0 software license.</p>
</div>
</body>
</html>