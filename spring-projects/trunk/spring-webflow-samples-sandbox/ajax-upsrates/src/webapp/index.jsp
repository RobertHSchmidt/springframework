<HTML>
	<head>
		<title>UPS rates - A Spring Web Flow Sample using AJAX</title>
		<script src="prototype.js"></script>
		<script src="swf_ajax.js"></script>
	</head>
	<BODY>
		<DIV align="left">UPS rates - A Spring Web Flow Sample using AJAX</DIV>
		
		<HR>
		
		<DIV align="left">			
			<P>
				This Spring web flow sample application implements a wizard using AJAX calls the server.
				This wizard takes the details for a UPS shipment and calls a web service to get a rate.
				This sample needs Internet access to talk to the remote server.
				
				 It illustrates the following concepts:
				<UL>
					<li>
						Using a JavaScript component to submit regular forms through an AJAX request. Inserting the HTML
						received from the server in a DIV tag.
					</li>
					<LI>
						Using the "_flowId" request parameter to let the view tell the web
						flow controller which flow needs to be started.
					</LI>
					<LI>
						Implementing a wizard using web flows.
					</LI>
					<LI>
						Using continuations to make the flow completely stable, no matter
						how browser navigation buttons are used.
					</LI>
				</UL>
			</P>
		</DIV>
		
		<HR>

		<div id="upsRates">
			<script type="text/javascript">
			<!--
			window.onload = function() {
				new SimpleRequest('upsRates', 'upsrates.htm', 'get', '_flowId=upsrates');
			};
			//-->
			</script>
		</div>
	</BODY>
</HTML>
