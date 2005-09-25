<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:hasBindErrors name="query">
	<c:forEach items="${errors.globalErrors}" var="error">
		<span class="error">${error.code}</span>
	</c:forEach>
</spring:hasBindErrors>

<form action="upsrates.htm" method="post" id="selectPackageDetailsForm">

	<fieldset>
		<legend>Select package details</legend>

		<spring:bind path="query.packageWeight">
			<label>Package weight</label>
			<input type="text" name="${status.expression}" value="<c:if test="${status.value >= 0}">${status.value}</c:if>">
			<c:if test="${status.error}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
			<br>
		</spring:bind>

		<spring:bind path="query.serviceLevelCode">
			<label>Service level</label>
			<select name="${status.expression}">
				<option value="-1">-- Select service level</option>
				<c:forEach items="${serviceLevelCodes}" var="serviceLevelCode">
					<option value="${serviceLevelCode.key}" <c:if test="${status.value == serviceLevelCode.key}">selected</c:if>>${serviceLevelCode.value}</option>
				</c:forEach>
			</select>
			<c:if test="${status.error}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
			<br>
		</spring:bind>

		<spring:bind path="query.packageType">
			<label>Package type</label>
			<select name="${status.expression}">
				<option value="-1">-- Select package type</option>
				<c:forEach items="${packageTypes}" var="packageType">
					<option value="${packageType.key}" <c:if test="${status.value == packageType.key}">selected</c:if>>${packageType.value}</option>
				</c:forEach>
			</select>
			<c:if test="${status.error}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
			<br>
		</spring:bind>

		<spring:bind path="query.rateChart">
			<label>Rate chart</label>
			<select name="${status.expression}">
				<option value="-1">-- Select rate chart</option>
				<c:forEach items="${rateCharts}" var="rateChart">
					<option value="${rateChart.key}" <c:if test="${status.value == rateChart.key}">selected</c:if>>${rateChart.value}</option>
				</c:forEach>
			</select>
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
		formRequest('selectPackageDetailsForm');
	-->
	</script>

</form>