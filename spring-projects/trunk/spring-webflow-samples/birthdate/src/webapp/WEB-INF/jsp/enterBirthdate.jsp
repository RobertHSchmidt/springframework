<%@ include file="includeTop.jsp" %>

<div id="content">
	<hr>
	<html:form action="birthdate" method="post">
	<table>
	<tr>
		<td colspan="2">
			<html:errors/>
		</td>
	</tr>
	<tr>
		<td>Your name</td>
		<td>
			<html:text property="name" size="25" maxlength="30"/>
		</td>
	</tr>
	<tr>
		<td>Your Birth Date (DD-MM-YYYY)</td>
		<td>
			<html:text property="date" size="10" maxlength="10"/>
		</td>
	</tr>
	<tr>
		<td colspan="2" class="buttonBar">
			<html:submit value="Calculate Age"/>
			<input type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
			<input type="hidden" name="_eventId" value="submit">
		</td>
	</tr>
	</html:form>
	</table>
</div>

<%@ include file="includeBottom.jsp" %>