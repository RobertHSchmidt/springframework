<%@ include file="include.jsp"%>

<h3>The following Persons have been found</h3>

<table cellspacing="2" cellpadding="4">
	<tr>
		<th align="left">Full Name</th>
		<th align="left">Company</th>
		<th align="left">Country</th>
	</tr>
	<c:forEach var="person" items="${persons}">
		<tr>
			<td><a
				href="<c:url value="/details.htm">
	<c:param name="name"><c:out value="${person.fullName}" /></c:param>
	<c:param name="company"><c:out value="${person.company}" /></c:param>
	<c:param name="country"><c:out value="${person.country}" /></c:param>
	</c:url>"><c:out
				value="${person.fullName}" /></a></td>
			<td><c:out value="${person.company}" /></td>
			<td><c:out value="${person.country}" /></td>
		</tr>
	</c:forEach>
	<tr>
		<td colspan="3">
		<form><input type="button" value="Refresh"
			onclick="document.location='<c:url value="/search.htm"/>'" /></form>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<a href="<c:url value="logoff.jsp"/>">Logoff</a> user <authz:authentication operation="username"/>
		</td>
	</tr>
</table>
