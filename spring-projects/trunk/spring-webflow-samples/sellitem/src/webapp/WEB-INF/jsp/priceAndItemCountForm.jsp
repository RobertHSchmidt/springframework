<%@ include file="includeTop.jsp" %>

<div align="left">Enter price and item count</div>
<hr>
<div align="left">
	<form name="priceAndItemCountForm" method="post">
		Price:
		<spring:bind path="sale.price">
			<input
				type="text"
				name="${status.expression}"
				value="${status.value}">
			<c:if test="${status.error}">
				<div class="error">${status.errorMessage}</div>
			</c:if>
		</spring:bind>

		<br>
			
		Item count:
		<spring:bind path="sale.itemCount">
			<input
				type="text"
				name="${status.expression}"
				value="${status.value}">
			<c:if test="${status.error}">
				<div class="error">${status.errorMessage}</div>
			</c:if>
		</spring:bind>
		<div align="right">
			<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
			<input type="submit" name="_eventId_submit" value="Next">
		</div>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>