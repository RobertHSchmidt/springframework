<%@ include file="includeTop.jsp" %>

<div id="content">
	Your Age
	<hr>
	<p>
		${birthDate.name}, you are now <I>${age}</I> old.
		You were born on <fmt:formatDate value="${birthDate.date}" pattern="dd-MM-yyyy"/>.
	</p>
	<hr>
	<form action="<c:url value="/index.jsp"/>">
		<INPUT type="submit" value="Home">
	</form>
</div>

<%@ include file="includeBottom.jsp" %>
