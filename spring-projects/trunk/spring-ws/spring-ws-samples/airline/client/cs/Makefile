target:
	wsdl -out:src/AirlineService.cs ../../src/main/webapp/airline.wsdl
	mcs -out:bin/airline.exe -r:System.Web.Services src/main.cs src/AirlineService.cs
