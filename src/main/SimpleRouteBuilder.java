package main;


import hipchat.HipchatMessageProcessor;

import java.util.Properties;

import mongodb.MongoByArtistProcessor;
import mongodb.MongoFixArtistString;
import mongodb.MongoInsertProcessor;
import mongodb.MongoResultProcessor;

import org.apache.camel.builder.RouteBuilder;

import lastFM.EventFinder;
import lastFM.LastFMProcessor;
import lastFM.LastFMSplitExpression;
import metrics.MetricsProcessor;
import newsletter.EnrichWithSubscribers;
import newsletter.GrabberAggregationStrategy;
import newsletter.NewsletterFullArtist;

import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;

import subscriptionhandler.EmailModifyProcessor;
import subscriptionhandler.EmailSubscribeProcessor;
import subscriptionhandler.EmailUnsubscribeProcessor;
import subscriptionhandler.SqlSplitExpression;
import twitter.ArtistFinder;
import twitter.TweetProcessor;
import youtube.YoutubeChannelProcessor;

public class SimpleRouteBuilder extends RouteBuilder {


	@Override
	public void configure() throws Exception {
		Properties p = Launcher.properties;
		// Enable global metrics support
		MetricsRoutePolicyFactory mrpf = new MetricsRoutePolicyFactory();
		this.getContext().addRoutePolicyFactory(mrpf);

		from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
		.choice()
		.when(header("Subject").isEqualTo("subscribe")).to("direct:subscribe")
		.when(header("Subject").isEqualTo("unsubscribe")).to("direct:unsubscribe")
		.when(header("Subject").isEqualTo("modify")).to("direct:modify")
		.end();

		from("direct:subscribe").process(new EmailSubscribeProcessor()).to("metrics:counter:subscribe.counter").to("direct:writedb");
		from("direct:unsubscribe").process(new EmailUnsubscribeProcessor()).to("metrics:counter:unsubscribe.counter").to("direct:writedb");
		from("direct:modify").process(new EmailModifyProcessor()).to("metrics:counter:modify.counter").to("direct:writedb");

		from("direct:writedb").to("metrics:timer:RDBM-write.timer?action=start")
		.split(new SqlSplitExpression()).wireTap("file:out").end().to("jdbc:accounts").to("metrics:counter:RDBM-write.counter").end()
		.to("metrics:timer:RDBM-write.timer?action=stop");

		from("timer://foo2?repeatCount=1&delay=0")
		.to("metrics:timer:twitter-process.timer?action=start")
		.setBody(simple("select distinct(artist) from subscriptions"))
		.to("jdbc:accounts")
		.split(body())
		.setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
		.process(new ArtistFinder())
		.to("direct:twittergrabber")
		.to("metrics:timer:twitter-process.timer?action=stop");


		// we can only get tweets from the last 8 days here!!!
		from("direct:twittergrabber")
		.to("twitter://search?consumerKey="+ p.getProperty("twitter.consumerkey")+"&consumerSecret="+p.getProperty("twitter.consumersecret")+"&accessToken="+p.getProperty("twitter.accesstoken")+"&accessTokenSecret="+p.getProperty("twitter.accesstokensecret"))
		.process(new TweetProcessor())
		.to("metrics:counter:twitter-artists-processed.counter")
		.to("direct:mongoInsert");

		// Hipchat playlist workflow
		from("timer://foo4?repeatCount=1&delay=0")
		.to("metrics:timer:hipchat-process.timer?action=start")
		.setHeader("type", simple("youtube"))
		.setHeader("caller", simple("hipchat"))
		.setBody(simple("select email from subscriber"))
		.to("jdbc:accounts")
		.split(body())
		.setBody(body().regexReplaceAll("\\{email=(.*)(\\r)?\\}", "$1"))
		.to("direct:getPlaylists")
		.to("metrics:timer:hipchat-process.timer?action=stop");
		
		from("direct:getPlaylists")
		.setHeader("subscriber", body())
		.setBody(simple("select artist from subscriptions where email = '${body}'"))
		.to("jdbc:accounts")
		.split(body())
		.setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
		.setHeader("artist",body())
		.to("direct:mongoGetArtist");
		
		from("direct:sendHipchat")
		.setHeader("HipchatToRoom",simple("smile-twitter"))
		.setHeader("HipchatTriggerNotification", simple("true"))
		.process(new HipchatMessageProcessor())
		.to("hipchat://?authToken=" + p.getProperty("hipchat.authtoken"))
		.to("metrics:counter:playlists-sent-hipchat.counter");

		// LastFM grabber starts here 
		from("timer://foo?repeatCount=1&delay=0")
		.to("metrics:timer:lastfm-process.timer?action=start")
		.setBody(simple("SELECT subscriptions.artist, locations.location FROM subscriptions,locations WHERE subscriptions.email=locations.email "))
		.to("jdbc:accounts?outputType=StreamList")
		.split(body()).streaming()
		.process(new EventFinder())
		.to("metrics:counter:lastfm-artists-found.counter")
		.to("direct:mongoInsert")
		.to("metrics:timer:lastfm-process.timer?action=stop");


		// Youtube grabber starts here
		from("timer://foo?repeatCount=1&delay=0")
			.to("metrics:timer:youtube-process.timer?action=start")
			.setBody(simple("select distinct(artist) from subscriptions"))
			.to("jdbc:accounts?outputType=StreamList")
			.split(body())
			.streaming()
			.setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
			.process(new ArtistFinder())
			.to("direct:youtubeAPI")
			.to("metrics:timer:youtube-process.timer?action=stop");

		from("direct:youtubeAPI")
			.process(new YoutubeChannelProcessor())
			.to("metrics:counter:YouTube-Playlists-generated.counter")
			.to("direct:mongoInsert");
			//.to("file:out?fileName=youtube_${date:now:yyyyMMdd_HHmmssSSS}.txt");
		
		//from("timer://foo1?repeatCount=30&delay=5000")

		from("jetty:http://localhost:12345/stats").process(new MetricsProcessor());

		//.to("file:out?fileName=metrics_${date:now:yyyyMMdd_HHmmssSSS}.json");

		// Versuch Velocity 
//		from("direct:a").
//		//to("velocity:org/apache/camel/component/velocity/letter.vm")
//		to("velocity:file:template/newsletter.vm")
//		.to("mock:result");

		/**
		 **--------------------
		 **Newsletter-Mail-Route
		 **--------------------
		 */
		
		EnrichWithSubscribers enrichWithSubscribers;
		
//		from("timer:newsletter?period=86400000") //can be set to specific time "time=yyyy-MM-dd HH:mm:ss" or just set the period to one day "period=86400000"
//		.log("--------------------timer fired..--------------------------------").
//		bean(flightOfferDAO , "getTodaysFlightoffers").id("flightOfferBean").
//		split(body()).
//		pollEnrich("bean:hotelDAO?method=getAllHotels", new HotelAggregationStrategy()).process(new PrintListFlightoffer()).
//		process(enrichWithSubscribers).
//		to("velocity:file:template/newsletter.vm").id("velocityTemplate").
//		to("smtp://188.40.32.121?username=workflow@seferovic.net&password=workflowpassword&contentType=text/html").
//		log("-------------------FINISHED--------------------------------------");

		
//		from("direct:mongoCache")
//		.log(LoggingLevel.INFO, "Load MongoDB in the Cache")
//		.setHeader(CacheConstants.CACHE_OPERATION,
//				constant(CacheConstants.CACHE_OPERATION_DELETEALL))
//		.to("mongodb:mongoBean?database=event2be&collection=Activity&operation=findAll")
//		.setHeader(CacheConstants.CACHE_OPERATION,
//				constant(CacheConstants.CACHE_OPERATION_ADD))
//		.setHeader(CacheConstants.CACHE_KEY, constant("Recommendation"))
//		.to("cache://elementCache").end();
//		
		//Poll Weather
//				from("timer://timerWeather?fixedRate=true&period=86 400s")
//			    .to("weather://weatherData?location=Vienna,Austria&mode=XML")
//			    .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_ADD))
//			    .setHeader(CacheConstants.CACHE_KEY, constant("Weather"))
//				.to("cache://weatherCache")
//				.end();
		//enrich weather data
//				from("seda:ActivityChannel")
//				.pollEnrich("cache://weatherCache", weatherEnrichAggregation)
		
		/// test
		from("timer://newsletter?repeatCount=1&delay=0")
		.log("--------------------timer fired..--------------------------------")
	//	.setHeader("type", simple("youtube"))
		.setHeader("caller", simple("newsletter"))
		.setBody(simple("select email from subscriber"))
		.to("jdbc:accounts")
		.split(body())
		.setBody(body().regexReplaceAll("\\{email=(.*)(\\r)?\\}", "$1"))
		.to("direct:getArtistsForNewsletter")
		.log("-------------------FINISHED--------------------------------------");
		
		from("direct:getArtistsForNewsletter")
		.log("--------------------getArtistsForNewsletter..--------------------------------")
		.setHeader("subscriber", body())
		.setBody(simple("select artist from subscriptions where email = '${body}'"))
	// toDO: Locations rausfiltern -> Kreuzprodukt
	// .setBody(simple("select artist from subscriptions where email = '${body}'"))
	//.setBody(simple("SELECT subscriptions.artist, locations.location FROM subscriptions,locations WHERE subscriptions.email=locations.email "))
		.to("jdbc:accounts")
		.split(body())
		.setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
		.setHeader("artist",body())
		.to("file:fm-out?fileName=getArtistMessage_${date:now:yyyyMMdd_HHmmssSSS}.txt")
		.to("direct:mongoGetFullArtist");
		//.to("direct:mongoGetArtist");
		
//		from("direct:sendNewsletter")
//		.log("--------------------sendNewsletter..--------------------------------")
		//.setHeader("youtube", body())
		//.setHeader("type", simple("twitter"))
//		.log("------------------Sending Newsletter to File")
		//.to("file:fm-out?fileName=getPlaylist_${date:now:yyyyMMdd_HHmmssSSS}.txt")
//		.to("direct:mongoGetFullArtist");
		
		from("direct:aggregateAll").aggregate(header("artist"), new NewsletterFullArtist())
//.header("artist")
		.completionInterval(15000)
	    .log("********************** Aggregator ALL  **************************")
//	    .process(new Processor() {
//	                   @Override
//	                   public void process(Exchange exchange) throws Exception {
//	                       logger.debug("All INC: " + exchange.getIn().getBody(String.class));
//	                   }
//	               })
//	       .pollEnrich("{{newsletter.pollEnrich}}" +
//	               "", new NewsletterEnrichAS())
//	       .to("{{global.smtp}}");
	    .log("------------------Sending Newsletter to File")
		.to("file:fm-out?fileName=getFullArtistMessage_${date:now:yyyyMMdd_HHmmssSSS}.txt");
		
//		.pollEnrich("direct:mongoGetArtist", new GrabberAggregationStrategy())
		//.setHeader("Newsletter",simple("newsletter-generation"))
		//.setHeader("subject", simple("New incident: first hello")) //${header.CamelFileName}
	//	.process(new LastFMProcessor()).split(new LastFMSplitExpression())
		
		
//		.setHeader("Subject", constant("Thanks for ordering"))
//		.setHeader("From", constant("donotreply@riders.com"))
		//.process(new HipchatMessageProcessor())
		//.to("velocity:file:template/newsletter.vm")
//		.to("velocity:file:template/newsletter.vm")
//		.split(new LastFMSplitExpression())
//		.to("file:fm-out?fileName=lastFM_${date:now:yyyyMMdd_HHmmssSSS}.txt");
	//	.to("smtp://" + p.getProperty("email.host") + "?password=" + p.getProperty("email.password")+"&From="+p.getProperty("email.user") +"&to="+p.getProperty("email.user")); 

		
// end testing

		
		
		
		
		
		from("file:fm-in?noop=true")
		.log("Working on file ${header.CamelFileName}")
		.setHeader("subject", simple("New incident: first hello")) //${header.CamelFileName}
		.to("smtp://" + p.getProperty("email.host") + "?password=" + p.getProperty("email.password")+"&From="+p.getProperty("email.user") +"&to="+p.getProperty("email.user")); 

		/*zum Experimentieren
		 * 
		 * //	"smtp://you@mymailserver.com?password=secret&From=you@apache.org" + recipients
			//smtp://host[:port]?password=somepwd&username=someuser

			//.to("smtps://smtp.gmail.com?username=fullemailaddress&password=secretpw&to=recipient@mail.com");
			//"smtps://myname@gmx.at?password=secretpw&to=recipient@mail.com"

		 */
		//Inserts data about artist into MongoDB overwrites if already existing
		from("direct:mongoInsert")
		.to("metrics:timer:mongo-insert.timer?action=start")
		.choice()
		.when(body().isNotEqualTo("{}"))
		.process(new MongoInsertProcessor())
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=save"))
		.to("metrics:counter:mongo-insert.counter")
		.to("metrics:timer:mongo-insert.timer?action=stop").end();       

		//gets one grabber from a single artist collection
		from("direct:mongoGetArtist")
		.to("metrics:timer:mongo-getArtist.timer?action=start")
		.setBody()
		.simple("${header.type}")
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=findById"))
		.process(new MongoResultProcessor())
		.to("metrics:counter:mongo-getArtist.counter")
		.to("direct:chooseCall")
		.to("metrics:timer:mongo-getArtist.timer?action=stop");
		
		
		// Routing for results from MongoDB
		from("direct:chooseCall")
		.choice().when(header("caller").isEqualTo("hipchat")).to("direct:sendHipchat").end()
		.choice().when(header("caller").isEqualTo("newsletter")).to("direct:sendNewsletter").end();

		//gets all data for a single Artist
		//atm still returns multiple messages
		from("direct:mongoGetFullArtist")
		.to("metrics:timer:mongo-getFullArtist.timer?action=start")
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=findAll"))
		.split().body()
		.process(new MongoResultProcessor())
		.to("metrics:counter:mongo-getFullArtist.counter")
	//	.to("mock:sortArtists")
		.to("direct:chooseCallFullArtist")
		.to("metrics:timer:mongo-getFullArtist.timer?action=stop");
		
		from("direct:chooseCallFullArtist")
	//	.choice().when(header("caller").isEqualTo("hipchat")).to("direct:sendHipchat").end()
		.choice().when(header("caller").isEqualTo("newsletter")).to("direct:aggregateAll");

		// First try to get multiple artists not sure if we need it
		//       from("direct:mongoGetFullArtists")
		//       .setHeader("artists")
		//       .simple("metallica, slayer")
		//       .to("log:mongo:splitter1?level=INFO")
		//       .split().method("splitterBean", "splitHeader")
		//       .to("direct:mongoGetFullArtist")
		//       .to("log:mongo:splitter2?level=INFO")
		//       .aggregate().body()
		//       .to("log:mongo:aggregator?level=INFO");






		/****** TEST ROUTES FOR MONGO DB PLZ DONT DELETE *****/       

		//     from("timer://runOnce?repeatCount=1&delay=5000")
		//     .to("direct:testFindAll");
		//     .to("direct:testInsert");
		//     .to("direct:testFindById");
		//     .to("direct:testRemove");
		//     .to("direct:mongoGetArtists");       

		from("direct:testInsert")
		.setHeader("artist")
		.simple("blind guardian")
		.setHeader("type")
		.simple("test3")
		.process(new MongoInsertProcessor())
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=test&collection=${header.artist}&operation=save"));

		from("direct:testRemove")
		.setHeader("artist")
		.simple("slayer")
		.setHeader("type")
		.simple("test")       
		.process(new MongoByArtistProcessor())
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=test&collection=${header.artist}&operation=remove"));


		from("direct:testFindById")
		.setHeader("artist")
		.simple("blind guardian")
		.setHeader("type")
		.simple("test")      
		.setBody()
		.simple("${header.type}")
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=test&collection=${header.artist}&operation=findById"))
		.to("log:mongo:findById1?level=INFO")
		.process(new MongoResultProcessor())
		.to("log:mongo:findById2?level=INFO");

		from("direct:testFindAll")
		.setHeader("artist")
		.simple("blind guardian")
		.setHeader("type")
		.simple("test")      
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=test&collection=${header.artist}&operation=findAll"))
		.to("log:mongo:findAll1?level=INFO")
		.split().body()
		.process(new MongoResultProcessor())
		.to("log:mongo:findAll2?level=INFO");    	

	}  
}
