<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<form action="upsrates.htm" method="post" id="selectReceiverForm">

	<fieldset>
		<legend>Select receiver location</legend>
		<spring:bind path="query.receiverCountryCode">
			<label>Country</label>
			<select name="${status.expression}">
				<option value="null">-- Select country</option>
				<c:forEach items="${countries}" var="country">
					<option value="${country.key}" <c:if test="${status.value == country.key}">selected</c:if>>${country.value}</option>
				</c:forEach>
			</select>
			<c:if test="${status.error}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
			<br>
		</spring:bind>
		<spring:bind path="query.receiverZipCode">
			<label>Zip code</label>
			<input type="text" name="${status.expression}" value="${status.value}">
			<c:if test="${status.error}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
			<br>
		</spring:bind>
		<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
		<input type="submit" value="Next" name="_eventId_submit">
	</fieldset>

	<script type="text/javascript">
	<!--
	formRequest('selectReceiverForm');
	-->
	</script>

</form>