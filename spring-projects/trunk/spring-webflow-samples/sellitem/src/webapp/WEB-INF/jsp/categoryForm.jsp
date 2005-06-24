<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<HTML>
	<BODY>
		<DIV align="left">Enter category</DIV>
		<HR>
		<DIV align="left">
			Price: ${sale.price}<br>
			Item count: ${sale.itemCount}
			
			<FORM name="categoryForm" method="post">
				<INPUT type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
				<INPUT type="hidden" name="_eventId" value="submit">
			
				Category:
				<spring:bind path="sale.category">
					<SELECT name="${status.expression}">
						<OPTION value="" <c:if test="${status.value==''}">selected</c:if>>
							None (0.02 discount rate)
						</OPTION>
						<OPTION value="A" <c:if test="${status.value=='A'}">selected</c:if>>
							Cat. A (0.1 discount rate when more than 100 items)
						</OPTION>
						<OPTION value="B" <c:if test="${status.value=='B'}">selected</c:if>>
							Cat. B (0.2 discount rate when more than 200 items)
						</OPTION>
					</SELECT>
				</spring:bind>
				
				<BR>
				
				Ship item to you?:
				<spring:bind path="sale.shipping"> 
					<INPUT type="hidden" name="_${status.expression}"  value="visible" /> 
					<INPUT type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked</c:if>> 
				</spring:bind>
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.categoryForm.submit()" value="Next">
		</DIV>
	</BODY>
</HTML>