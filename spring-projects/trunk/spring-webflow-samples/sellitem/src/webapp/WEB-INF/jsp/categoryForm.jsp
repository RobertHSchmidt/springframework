<%@ include file="includeTop.jsp" %>

<div align="left">Enter category</div>
<hr>
<div align="left">
	Price: ${sale.price}<br>
	Item count: ${sale.itemCount}
			
	<form name="categoryForm" method="post">
		Category:
		<spring:bind path="sale.category">
			<select name="${status.expression}">
				<option value="" <c:if test="${status.value ==''}">selected</c:if>>
					None (0.02 discount rate)
				</option>
				<option value="A" <c:if test="${status.value =='A'}">selected</c:if>>
					Cat. A (0.1 discount rate when more than 100 items)
				</option>
				<option value="B" <c:if test="${status.value =='B'}">selected</c:if>>
					Cat. B (0.2 discount rate when more than 200 items)
				</option>
			</select>
		</spring:bind>
		
		<BR>
		
		Ship item to you?:
		<spring:bind path="sale.shipping"> 
			<INPUT type="hidden" name="_${status.expression}"  value="visible" /> 
			<INPUT type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked</c:if>> 
		</spring:bind>
		
		<div align="right">
			<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
			<input type="submit" name="_eventId_submit" value="Next">
		</div>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>