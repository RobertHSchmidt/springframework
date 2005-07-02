<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert"><img src="images/webflow-logo.jpg"/></div>
	<h2>The Four Digit Number Guess Game: Guess a four digit number!</h2>
	<hr>
	<p>Note: each guess must be 4 unique digits!</p>
	<p>Number of guesses so far: ${data.guesses}</p>

	<%@include file="guessHistoryTable.jsp" %>
			
	<form name="guessForm" method="post">
		<table>
			<c:if test="${flowExecutionContext.lastEventId == 'invalidInput'}">
				<tr>
					<td colspan="2">
						<div class="error">Your guess was invalid: it must be a 4 digit number (e.g 1234), and each digit must be unique.</div>
					</td>
				</tr>
			</c:if>
		    <tr>
		    	<td>Guess:</td>
		    	<td>
		    		<input type="text" name="guess" value="${param.guess}">
		    	</td>
		    </tr>
			<tr>
				<td colspan="2" class="buttonBar">
					<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
					<input type="submit" class="button" name="_eventId_submit" value="Guess">
				</td>
			</tr>
		</table>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>