<%@ include file="includeTop.jsp"%>

<f:view>

	<div id="content">
	<div id="insert"><img src="images/webflow-logo.jpg" /></div>
	<h2>Enter price and item count</h2>
	<hr>
	<table>
		<h:form id="priceAndItemCountForm">
			<tr>
				<td>Price:</td>
				<td><h:inputText value="#{flow.sale.price}"
					required="true" /></td>
			</tr>
			<tr>
				<td>Item count:</td>
				<td><h:inputText value="#{flow.sale.itemCount}"
					required="true" /></td>
			</tr>
			<tr>
				<td colspan="2" class="buttonBar">
					<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}"/>
					<h:commandButton type="submit" value="Next" action="submit" immediate="false" /></td>
			</tr>
		</h:form>
	</table>
	</div>

</f:view>

<%@ include file="includeBottom.jsp"%>
