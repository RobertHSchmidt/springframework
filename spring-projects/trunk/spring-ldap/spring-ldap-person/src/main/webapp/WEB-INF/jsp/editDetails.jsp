<%@ include file="include.jsp"%>

<h3>Edit details for <c:out value="${person.fullName}" /></h3>

<form action="<c:url value="/edit.htm"/>" method="post">
<spring:nestedPath path="person">
<table cellspacing="2" cellpadding="4">
	<tr>
		<th align="right">Full Name:</th>
		<spring:bind path="fullName">
		<td><input type="text" name="<c:out value="${status.expression}" />" value="<c:out value="${status.value}" />" /></td>
		</spring:bind>
	</tr>
	<tr>
		<th align="right">Company:</th>
		<spring:bind path="company">
		<td><input type="text" name="<c:out value="${status.expression}" />" value="<c:out value="${status.value}" />" /></td>
		</spring:bind>
	</tr>
	<tr>
		<th align="right">Country:</th>
		<spring:bind path="country">
		<td><input type="text" name="<c:out value="${status.expression}" />" value="<c:out value="${status.value}" />" /></td>
		</spring:bind>
	</tr>
	<tr>
		<th align="right">Phone:</th>
		<spring:bind path="phone">
		<td><input type="text" name="<c:out value="${status.expression}" />" value="<c:out value="${status.value}" />" /></td>
		</spring:bind>
	</tr>
	<tr>
		<th align="right">Description:</th>
		<spring:bind path="description">
		<td><input type="text" name="<c:out value="${status.expression}" />" value="<c:out value="${status.value}" />" /></td>
		</spring:bind>
	</tr>
	<tr>
		<td colspan="2">
			<c:url var="backUrl" value="/details.htm">
				<c:param name="name">
					<c:out value="${person.fullName}" />
				</c:param>
				<c:param name="company">
					<c:out value="${person.company}" />
				</c:param>
				<c:param name="country">
					<c:out value="${person.country}" />
				</c:param>
			</c:url>
			<input type="submit" value="Submit"/>
			&nbsp;
			<input type="button" value="Back"
			onclick="document.location='<c:out value="${backUrl}" />'" />
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<a href="<c:url value="logoff.jsp"/>">Logoff</a> user <authz:authentication operation="username"/>
		</td>
	</tr>
</table>
</spring:nestedPath>
</form>
