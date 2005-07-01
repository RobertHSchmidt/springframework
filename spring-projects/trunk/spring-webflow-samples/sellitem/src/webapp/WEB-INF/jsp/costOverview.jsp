<%@ include file="includeTop.jsp" %>

<div align="left">Cost overview</div>
<hr>
<div align="left">
	Price: ${sale.price}<BR>
	Item count: ${sale.itemCount}<BR>
	Category: ${sale.category}<BR>
	<c:choose>
		<c:when test="${sale.shipping}">
			Shipping: ${sale.shipping}<BR>
			Shipping type: ${sale.shippingType}
		</c:when>
		<c:otherwise>
			No shipping, you're doing pickup of the items.
		</c:otherwise>
	</c:choose>
	
	<br>
	<br>
		
	<b>Amount:</b> ${sale.amount}<br>
	<b>Delivery cost:</b> + ${sale.deliveryCost}<br>
	<b>Discount:</b> - ${sale.savings} (Discount rate: ${sale.discountRate})<br>
	<b>Total:</b> ${sale.totalCost}<br>
</div>
<hr>
<div align="right">
	<form action="<c:url value="/index.jsp"/>">
		<input type="submit" value="Home">
	</form>
</div>

<%@ include file="includeBottom.jsp" %>