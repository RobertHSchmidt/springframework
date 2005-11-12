<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="<portlet:actionURL/>" method="post">
	<table>
		<tr>
			<td>
			    <div class="portlet-section-header">Search Results</div>
			</td>
		</tr>
		<tr>
			<td>
				<hr>
			</td>
		</tr>
		<tr>
			<td>
				<table border="1">
					<tr>
						<th>First Name</th>
						<th>Last Name</th>
						<th>User Id</th>
						<th>Phone</th>
					</tr>
					<c:forEach var="person" items="${executeSearch.result}">
						<tr>
							<td>${person.firstName}</td>
							<td>${person.lastName}</td>
							<td>
								<a href="
                                    <portlet:renderURL>
						                <portlet:param name="_flowExecutionId" value="${flowExecutionId}" />
						            	<portlet:param name="_eventId" value="select" />
						            	<portlet:param name="id" value="${person.id}>" />
						            </portlet:renderURL>">
							        ${person.userId}
								</a>
							</td>
							<td>${person.phone}</td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		<tr>
			<td class="buttonBar">
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
				<input type="submit" class="button" name="_eventId_newSearch" value="New Search">
			</td>
		</tr>
	</table>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>