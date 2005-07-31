<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert"><img src="images/webflow-logo.jpg"/></div>
	<h2>You guessed it!</h2>
	<table>
	<tr>
		<td>Answer:</td>
		<td>${numberGuessAction.answer}</td>
	</tr>
	<tr>
		<td>Total number of guesses:</td>
		<td>${numberGuessAction.guesses}</td>
	</tr>
	<tr>
		<td>Elapsed time in seconds:</td>
		<td>${numberGuessAction.durationSeconds}</td>
	</tr>
	<tr>
		<td colspan="2" class="buttonBar">
			<form action="play.htm">
				<input type="hidden" name="_flowId" value="numberGuess">
				<input type="submit" value="Play Again!">
			</form>
		</td>
	</tr>
	</table>
	
<%@ include file="includeBottom.jsp" %>