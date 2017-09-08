package jira_qmethods

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class jira_qmethods extends Simulation {

	val httpProtocol = http
	    .baseURL("http://192.168.0.47:8081")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate, identity")
		.acceptLanguageHeader("de,en-US;q=0.7,en;q=0.3")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
		.doNotTrackHeader("1")
		.disableCaching
		
	val scn1 = scenario("Beispiel_Jira").during(3 minutes){exec(Jira.GoTo).exec(Jira.Login).exec(Jira.Search).exec(Jira.Logout)}
	val scn2 = scenario("Beispiel_Jira_Peak").during(60 seconds){exec(Jira.GoTo).exec(Jira.Login).exec(Jira.Search).exec(Jira.Logout)}

	
	object Jira {
		
	val GoTo = 
	{
		group("Jira_Group"){
			exec(http("JIRA")
				.get("/")
				.check(status.is(200)))}
				.pause(1)
	}

	val Login =
	{
		group("Login_Group"){
			exec(http("Login")
				.post("/rest/gadget/1.0/login")
				.formParam("os_username", "Username")
				.formParam("os_password", "Password")		
				.check(headerRegex("Set-Cookie", "atlassian.xsrf.token=(.*?);").find.saveAs("Token"))	
				.check(status.is(200)))}
				.pause(1)
	}
	
	val Search = 
	{
		group("Search_Group"){	
			exec(http("Suche Gatling")
				.post("/secure/QuickSearch.jspa")
				.formParam("searchString", "Gatling")
				.check(status.is(200)))
			
	// Die Suche wird hierbei doppelt ausgeführt. Dies dient nur zur Demonstration zur Verifizierung via RegEx!
			
			.exec(http("Suche verifizieren")
				.get("""/browse/QI-627?jql=text%20~%20"gatling"""")
				.check(regex("Vorgang erstellen")))}
				.pause(1)
	}

	val Logout = 
	{			
		group("Logout"){
			exec(http("Logout")
			.get("/secure/Logout!default.jspa?atl_token="+"${Token}"))
		}
	}			
}

	setUp(
		scn1.inject(rampUsers(5) over(10 seconds)),
		scn2.inject(nothingFor(60 seconds), atOnceUsers(25))
	).protocols(httpProtocol)
}
