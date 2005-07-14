<%@ include file="includeTop.jsp" %>

<div id="content">
	<form action="itemlist.htm" method="post"/>
	<table>
		<tr>
			<td>
				Item list:
			</td>
			<td>
				<table border="1">
					<c:forEach var="item" items="${list}">
						<tr><td>${item}</td></tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<!-- Tell webflow what executing flow we're participating in -->
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}"/>
				<!-- Tell webflow what event happened -->
				<input type="submit" name="_eventId_add" value="Add New Item">
			</td>
		</tr>
	</table>
    </form>
</div>

<%@ include file="includeBottom.jsp" %>