<%@ page session="true" %> <%-- make sure we have a session --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
	
		<DIV align="left">Item List - A Spring Web Flow Sample</DIV>
		
		<HR>
		
		<DIV align="left">
			<P>
				<A href="app.htm?_flowId=itemlist">Item List</A>
			</P>
			
			<P>
				This Spring web flow sample application illustrates 3 features:
				<UL>
					<LI>
						Double submit prevention: refreshing or back button use will not allow you to resubmit data.
					</LI>
					<LI>
						Flow expiry and cleanup: after 1 minute of idle time, a flow will expire and
						will no longer be available for request processing.
					</LI>
					<LI>
						Use of an inline-flow, including the ability to map subflow output attributes
						directly into collection attributes in parent flow scope.
					</LI>
					<LI>
						Event pattern matching, for matching eventId expressions to transitions.
					</LI>
				</UL>
			</P>
		</DIV>
		
		<HR>

		<DIV align="right"></DIV>
		
	</BODY>
</HTML>
