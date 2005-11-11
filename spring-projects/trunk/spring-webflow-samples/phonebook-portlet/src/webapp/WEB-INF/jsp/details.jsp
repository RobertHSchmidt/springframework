<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="<portlet:actionURL/>" method="post">
	<table>
		<tr>
			<td class="portlet-section-subheader">Person Details</td>
		</tr>
		<tr>
			<td colpan="2"><hr></td>
		</tr>
		<tr>
			<td><b>First Name</b></td>
			<td>${getDetails.result.firstName}</td>
		</tr>
		<tr>
			<td><b>Last Name</b></td>
			<td>${getDetails.result.lastName}</td>
		</tr>
		<tr>
			<td><b>User Id</B></td>
			<td>${getDetails.result.userId}</td>
		</tr>
		<tr>
			<td><b>Phone</b></td>
			<td>${getDetails.result.phone}</td>
		</tr>
		<tr>
			<td colspan="2">
				<br>
				<b>Colleagues:</b>
				<br>
				<c:forEach var="colleague" items="${getDetails.result.colleagues}">
					<a href="
                                            <portlet:renderURL>
			            	        <portlet:param name="_flowExecutionId" value="${flowExecutionId}" />
			            	        <portlet:param name="_eventId" value="select" />
			            	        <portlet:param name="id" value="${colleague.id}>" />
			            	    </portlet:renderURL>">
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