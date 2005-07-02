<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert"><img src="images/webflow-logo.jpg"/></div>
	<h2>The Number Guess Game
	<h3>Guess a number between 1 and 100!</h3>
	<hr>
	<form name="guessForm" method="post">
		<table>
			<tr>
				<td>Number of guesses:</td>
				<td>${data.guesses}</td>
			</tr>
			<tr>
				<td>Your last guess was:</td>
				<td>${data.lastGuessResult}</td>
			</tr>
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