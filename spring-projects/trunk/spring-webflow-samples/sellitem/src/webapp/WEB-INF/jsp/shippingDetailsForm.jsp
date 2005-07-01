<%@ include file="includeTop.jsp" %>

<div align="left">Enter shipping details</div>
<hr>
<div align="left">
	Price: ${sale.price}<br>
	Item count: ${sale.itemCount}<br>
	Category: ${sale.category}<br>
	Shipping: ${sale.shipping}

	<form name="shippingForm" method="post">
		Shipping type:
		<spring:bind path="sale.shippingType">
			<select name="${status.expression}">
				<option value="S" <c:if test="${status.value=='S'}">selected</c:if>>
					Standard (10 extra cost)
				</option>
				<option value="E" <c:if test="${status.value=='E'}">selected</c:if>>
					Express (20 extra cost)
				</option>
			</select>
		</spring:bind>
		
		<div align="right">
			<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
			<input type="submit" name="_eventId_submit" value="Next">
		</div>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>