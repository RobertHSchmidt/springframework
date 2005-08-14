<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="phonebook.htm" method="post">
	<table>
		<tr>
			<td>Person Details</td>
		</tr>
		<tr>
			<td colpan="2"><hr></td>
		</tr>
		<tr>
			<td><b>First Name</b></td>
			<td>${result.firstName}</td>
		</tr>
		<tr>
			<td><b>Last Name</b></td>
			<td>${result.lastName}</td>
		</tr>
		<tr>
			<td><b>User Id</B></td>
			<td>${result.userId}</td>
		</tr>
		<tr>
			<td><b>Phone</b></td>
			<td>${result.phone}</td>
		</tr>
		<tr>
			<td colspan="2">
				<br>
				<b>Colleagues:</b>
				<br>
				<c:forEach var="colleague" items="${result.colleagues}">
					<a href="phonebook.htm?_flowExecutionId=${flowExecutionId}&_eventId=select&id=${colleague.id}">
						${colleague.firstName} ${colleague.lastName}<br>
					</a>
				</c:forEach>				
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
				<input type="submit" class="button" name="_eventId_back" value="Back">
			</td>
		</tr>
	</table>
	</form>
</DIV>