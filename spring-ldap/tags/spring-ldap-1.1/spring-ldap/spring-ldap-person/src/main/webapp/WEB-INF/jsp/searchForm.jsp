<%@ include file="include.jsp"%>

<h1>Display</h1>

<form action="<c:url value="/search.htm"/>" method="post">
<input type="hidden" name="select" value="false"/>

<table>
<tr>
	<th>Region:</th>
	<td>
		<select name="region" onchange="this.form.select.value='true';this.form.submit()">
		<c:forEach var="region" items="${regions}">
			<c:choose>
				<c:when test="${region.value == searchQuery.region}">
					<option value="<c:out value="${region.value}"/>" selected="selected"><c:out value="${region.label}"/></option>
				</c:when>
				<c:otherwise>
					<option value="<c:out value="${region.value}"/>"><c:out value="${region.label}"/></option>
				</c:otherwise>
			</c:choose>
		</c:forEach>
		</select>
	</td>
</tr>
<tr>
	<th>Country:</th>
	<td>
		<select name="country">
		<c:forEach var="country" items="${countries}">
			<c:choose>
				<c:when test="${country.value == searchQuery.country}">
					<option value="<c:out value="${country.value}"/>" selected="selected"><c:out value="${country.label}"/></option>
				</c:when>
				<c:otherwise>
					<option value="<c:out value="${country.value}"/>"><c:out value="${country.label}"/></option>
				</c:otherwise>
			</c:choose>
		</c:forEach>
		</select>
	</td>
</tr>
<tr>
	<td colspan="2">
		<input type="submit" value="Submit"/>
	</td>
</tr>
<tr>
	<td colspan="2">
		<a href="<c:url value="logoff.jsp"/>">Logoff</a> user <authz:authentication operation="username"/>
	</td>
</tr>
</table>
</form>
