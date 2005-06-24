<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<HTML>
	<BODY>
		<DIV align="left">Enter shipping details</DIV>
		<HR>
		<DIV align="left">
			Price: ${sale.price}<BR>
			Item count: ${sale.itemCount}<BR>
			Category: ${sale.category}<BR>
			Shipping: ${sale.shipping}

			<FORM name="shippingForm" method="post">
				<INPUT type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
				<INPUT type="hidden" name="_eventId" value="submit">
		
				Shipping type:
				<spring:bind path="sale.shippingType">
					<SELECT name="${status.expression}">
						<OPTION value="S" <c:if test="${status.value=='S'}">selected</c:if>>
							Standard (10 extra cost)
						</OPTION>
						<OPTION value="E" <c:if test="${status.value=='E'}">selected</c:if>>
							Express (20 extra cost)
						</OPTION>
					</SELECT>
				</spring:bind>					
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.shippingForm.submit()" value="Next">
		</DIV>
	</BODY>
</HTML>