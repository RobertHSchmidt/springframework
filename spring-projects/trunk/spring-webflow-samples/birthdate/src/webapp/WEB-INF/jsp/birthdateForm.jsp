<%@ include file="includeTop.jsp" %>

<div id="content">
	<hr>
	<html:form action="flowAction" method="post">
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
			<html:image src="images/submit.jpg" property="_eventId_submit" value="Next"/>
			<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
		</td>
	</tr>
	</html:form>
	</table>
</div>

<%@ include file="includeBottom.jsp" %>