<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<form action="upsrates.htm" method="post">

	<fieldset>
		<legend>Rate details</legend>

		<label>Customer type</label>
		<span>
			<c:if test="${query.residential == true}">Residential</c:if>
			<c:if test="${query.residential == false}">Business, institution or government agency</c:if>
		</span>
		<br>
		
		<label>Sender location</label>
		<span>${query.senderZipCode}, ${senderCountry}</span>
		<br>
		
		<label>Receiver location</label>
		<span>${query.receiverZipCode}, ${receiverCountry}</span>
		<br>
		
		<label>Package weight</label>
		<span>${query.packageWeight}</span>
		<br>
		
		<label>Service level</label>
		<span>${serviceLevel}</span>
		<br>
		
		<label>Package type</label>
		<span>${packageType}</span>
		<br>
		
		<label>Rate chart</label>
		<span>${rateChart}</span>
		<br>
		
		<label>Rate</label>
		<span>${rate}</span>
		<br>
		

	</fieldset>
</form>