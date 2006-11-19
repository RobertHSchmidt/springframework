<%@ include file="include.jsp"%>

<h3>Details for <c:out value="${person.fullName}" /></h3>

<table cellspacing="2" cellpadding="4">
	<tr>
		<th align="right">Full Name:</th>
		<td><c:out value="${person.fullName}" /></td>
	</tr>
	<tr>
		<th align="right">Company:</th>
		<td><c:out value="${person.company}" /></td>
	</tr>
	<tr>
		<th align="right">Country:</th>
		<td><c:out value="${person.country}" /></td>
	</tr>
	<tr>
		<th align="right">Phone:</th>
		<td><c:out value="${person.phone}" /></td>
	</tr>
	<tr>
		<th align="right">Description:</th>
		<td>
		<c:forEach items="${person.description}" var="description">
		<c:out value="${description}" /> <br />
		</c:forEach>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<form>
				<c:url var="editUrl" value="/edit.htm">
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
				<input type="button" value="Edit"
					onclick="document.location='<c:out value="${editUrl}"/>'" />
				&nbsp;
				<input type="button" value="Back"
					onclick="document.location='<c:url value="/search.htm"/>'" />
			</form>
		</td>
	</tr>
</table>
