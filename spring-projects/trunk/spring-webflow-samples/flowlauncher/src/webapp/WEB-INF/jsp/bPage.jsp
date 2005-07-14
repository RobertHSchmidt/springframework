<%@ include file="includeTop.jsp" %>

<div id="content">
	Sample B Flow
	<hr>
	Flow input was: ${input}<BR>
	<c:if test="${!flowExecutionContext.rootFlowActive}">
	<br>
	Sample B is running as a sub flow of another flow, so we can end Sample B and
	return to the parent flow, using either an anchor or a form:
	<li>
		<a href="<c:url value="/flow.htm?_flowExecutionId=${flowExecutionId}&_eventId=end"/>">
			End Sample B
		</a>
	</li>
	<li>
		<form action="<c:url value="/flow.htm"/>" method="post">
			<input type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
			<input type="submit" name="_eventId_end" value="End Sample B">
		</form>
	</li>
	</c:if>
	<hr>
	<form action="<c:url value="/index.html"/>">
		<input type="submit" value="Home">
	</form>
</div>

<%@ include file="includeBottom.jsp" %>
