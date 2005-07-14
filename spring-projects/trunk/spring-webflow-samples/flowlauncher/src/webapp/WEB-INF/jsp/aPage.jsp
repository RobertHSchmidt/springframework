<%@ include file="includeTop.jsp" %>

<div id="content">
	Sample A Flow
	<hr>
	Flow input was: ${input}<br>
	<br>
	From Sample A you can terminate Sample A and launch Sample B from an end state of Sample A.
	Input parameters are given as normal request parameters. Again, this can be done using
	either an anchor or a form:
	<li>
		<a href="<c:url value="/flow.htm?_flowExecutionId=${flowExecutionId}&_eventId=end-A-and-launch-B&input=someInputForSampleB"/>">
			End Sample A and Launch Sample B
		</a>
	</li>
	<hr>
	<li>
		<form action="<c:url value="/flow.htm"/>" method="post">
			<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
			<input type="text" name="input" value="someInputForSampleB">
			<input type="submit" name="_eventId_end-A-and-launch-B" value="End Sample A and Launch Sample B">
		</form>
	</li>
	<br>
	Alternatively, you can spawn Sample B as a sub flow from Sample A. In this case a flow
	attribute mapper maps input into the spawning subflow. Here you also have the option of
	using either an anchor or a form:
	<li>
		<a href="<c:url value="/flow.htm?_flowExecutionId=${flowExecutionId}&_eventId=launch-B-as-subflow"/>">
			Launch Sample B as a Sub Flow
		</a>
	</li>
	<li>
		<form action="<c:url value="/flow.htm"/>" method="post">
			<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
			<input type="submit" name="_eventId_launch-B-as-subFlow" value="Launch Sample B as a Sub Flow">
		</form>
	</li>
	<br>
	Yet another option is just launching Sample B as a top-level flow without even terminating Sample A:
	<li>
		<a href="<c:url value="/flow.htm?_flowId=sampleB&input=someInputForSampleB"/>">
			Launch Sample B
		</a>
	</li>
	<li>
		<form action="<c:url value="/flow.htm"/>" method="post">
			<input type="hidden" name="_flowId" value="sampleB">
			<input type="text" name="input" value="someInputForSampleB">
			<input type="submit" value="Launch Sample B">
		</form>
	</li>
	<li>
		<form action="<c:url value="/index.html"/>">
			<INPUT type="submit" value="Home">
		</form>
	</li>
</div>

<%@ include file="includeBottom.jsp" %>