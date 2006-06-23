using System;
using System.Web;
using System.Web.Services.Protocols;
using Microsoft.Web.Services2;
using Microsoft.Web.Services2.Security;
using Microsoft.Web.Services2.Security.Tokens;

namespace Spring.Ws.Samples.Airline.Client.CSharp {

	public class Client {
		public static void Main(string[] args) {
			try {
				AirlineService service = new AirlineService();
				if (args.Length > 0) {
					service.Destination = new Uri(args[0]);
				}
				UsernameToken userToken = new UsernameToken("john","changeme", PasswordOption.SendHashed);
				SoapEnvelope request = new SoapEnvelope();
				request.setBodyObject(@"<GetFrequentFlyerMileage xmlns=""http://www.springframework.org/spring-ws/samples/airline/schemas""/>");
				request.Context.Security.Tokens.Add(userToken);
				SoapEnvelope response = service.GetFrequentFlyerMileage(request);
			} catch (SoapException ex) {
				Console.Error.WriteLine("SOAP Fault Code    {0}", ex.Code);
				Console.Error.WriteLine("SOAP Fault String: {0}", ex.Message);
			}
		}

	}
}
