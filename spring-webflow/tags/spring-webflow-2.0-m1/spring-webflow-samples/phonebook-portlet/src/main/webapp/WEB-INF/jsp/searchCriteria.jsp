<%@ page contentType="text/html" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>

<portlet:defineObjects/>

<html>
<head>
<title>Enter Search Criteria</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/style.css") %>" type="text/css">
</head>
<body>

<div id="logo">
	<img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/spring-logo.jpg") %>" height="73" alt="Logo" border="0"> 
</div>

<div id="content">

	<div id="insert">
		<img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/webflow-logo.jpg") %>"/>
	</div>
	
	<form action="<portlet:actionURL/>" method="post">
	<table>
		<tr>
			<td>
            	<div class="portlet-section-header">Search Criteria</div>
            </td>
		</tr>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<spring:hasBindErrors name="searchCriteria">
		<tr>
			<td colspan="2">
				<div class="portlet-msg-error">Please provide valid search criteria</div>
			</td>
		</tr>
		</spring:hasBindErrors>
		<spring:bind path="firstName">
		<tr>
			<td>First Name</td>
			<td>
				<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>">
			</td>
		</tr>
		</spring:bind>		
		<spring:bind path="lastName">
		<tr>
			<td>Last Name</td>
			<td>
				<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>">
			</td>
		</TR>
		</spring:bind>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="<c:out value="${flowExecutionKey}"/>">
				<input type="submit" class="portlet-form-button" name="_eventId_search" value="Search">
			</td>
		</tr>
	</table>
	</form>
</div>

<div id="copyright">
	<p>&copy; Copyright 2004-2007, <a href="http://www.springframework.org">www.springframework.org</a>, under the terms of the Apache 2.0 software license.</p>
</div>
</body>
</html>