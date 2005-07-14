<%@ include file="includeTop.jsp" %>

<div id="content">
	<form action="itemList.htm" method="post"/>
	<table>
		<tr>
			<td>
				Item data:
			</td>
			<td>
				<input type="text" name="data">
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<!-- Tell webflow what transaction we're in  -->
				<input type="hidden" name="_txToken" value="${txToken}">
				<!-- Tell webflow what executing flow we're participating in -->
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}"/>
				<!-- Tell webflow what event happened -->
				<input type="submit" name="_eventId_submit" value="Submit">
			</td>
		</tr>
	</table>
    </form>
</div>

<%@ include file="includeBottom.jsp" %>