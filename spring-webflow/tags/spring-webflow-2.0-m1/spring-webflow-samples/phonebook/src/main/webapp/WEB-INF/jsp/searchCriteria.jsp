<%@ page contentType="text/html" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
	<title>Search the Phonebook</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="stylesheet" href="style.css" type="text/css">
</head>

<body>

<div id="logo">
	<img src="images/spring-logo.jpg" alt="Logo"> 
</div>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form:form commandName="searchCriteria" method="post">
	<table>
		<tr>
			<td>Search Criteria</td>
		</tr>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<form:errors cssClass="error" path="*"/>
			</td>
		</tr>
		<tr>
			<td>First Name</td>
			<td>
				<form:input path="firstName" />
			</td>
		</tr>
		<tr>
			<td>Last Name</td>
			<td>
				<form:input path="lastName" />
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
				<input type="submit" class="button" name="_eventId_search" value="Search">
			</td>
		</tr>		
	</table>
	</form:form>
</div>

<div id="copyright">
	<p>&copy; Copyright 2004-2007, <a href="http://www.springframework.org">www.springframework.org</a>, under the terms of the Apache 2.0 software license.</p>
</div>

</body>
</html>