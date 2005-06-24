<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<HTML>
	<BODY>
		<DIV align="left">Cost overview</DIV>
		<HR>
		<DIV align="left">
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
			
			<BR>
			<BR>
			
			<B>Amount:</B> ${sale.amount}<BR>
			<B>Delivery cost:</B> + ${sale.deliveryCost}<BR>
			<B>Discount:</B> - ${sale.savings} (Discount rate: ${sale.discountRate})<BR>
			<B>Total:</B> ${sale.totalCost}<BR>
		</DIV>
		<HR>
		<DIV align="right">
			<FORM action="<c:url value="/index.jsp"/>">
				<INPUT type="submit" value="Home">
			</FORM>
		</DIV>
	</BODY>
</HTML>