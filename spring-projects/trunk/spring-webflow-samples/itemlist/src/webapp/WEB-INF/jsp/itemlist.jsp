<%@ include file="includeTop.jsp" %>

<div id="content">
	<form action="app.htm" method="post"/>
	<table>
		<tr>
			<td>
				Item list:
			</td>
		</tr>
		<tr>
			<td>
				<table border="1" width="300px">
					<c:forEach var="item" items="${list}">
						<tr><td>${item}</td></tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		<tr>
			<td class="buttonBar">
				<!-- Tell webflow what executing flow we're participating in -->
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}"/>
				<input type="hidden" name="_stateId" value="displayItemlist"/>
				<!-- Tell webflow what event happened -->
				<input type="submit" name="_eventId_add" value="Add New Item">
			</td>
		</tr>
	</table>
    </form>
</div>

<%@ include file="includeBottom.jsp" %>