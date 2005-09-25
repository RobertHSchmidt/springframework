<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<form action="upsrates.htm" method="post" id="selectCustomerTypeForm">

	<spring:bind path="query.residential">
		<c:if test="${status.error}">
			<span class="error">${status.errorMessage}</span>
		</c:if>
	</spring:bind>

	<fieldset>
		<legend>Select your customer profile</legend>

		<label>What's most appropriate for you?</label><br>

		<spring:bind path="query.residential">
			<label>I'm a private person</label>
			<input type="radio" name="${status.expression}" value="true" <c:if test="${query.residentialSet && status.value == true}">checked</c:if>><br>
		</spring:bind>
		
		<spring:bind path="query.residential">
			<label>We're a business, institution or government agency</label>
			<input type="radio" name="${status.expression}" value="false" <c:if test="${query.residentialSet && status.value == false}">checked</c:if>><br>
		</spring:bind>
		
		<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
		<input type="submit" value="Next" name="_eventId_submit">
	</fieldset>

	<script type="text/javascript">
	<!--
		formRequest('selectCustomerTypeForm');
	-->
	</script>

</form>