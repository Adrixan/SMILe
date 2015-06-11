package main;


import helper.DeadLetterProcessor;
import hipchat.HipchatMessageProcessor;

import java.util.HashMap;
import java.util.Properties;

import mongodb.MongoAggregationStrategy;
import mongodb.MongoByArtistProcessor;
import mongodb.MongoFilterProcessor;
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
import newsletter.HeaderChangerProcessor;
import newsletter.NewsletterFullArtist;
import newsletter.SubscriberLocationProcessor;

import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.component.mongodb.MongoDbConstants;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import amazon.AmazonAggregationStrategy;
import amazon.AmazonMongoTester;
import amazon.AmazonRequestCreator;
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
		//Set DeadLetterChannel Route
		errorHandler(deadLetterChannel("direct:DLCRoute"));
		
		//DeadLetterChannel
		from("direct:DLCRoute")
		.process(new DeadLetterProcessor())
		.to("file:dlc?fileName=exception_${date:now:yyyyMMdd_HHmmssSSS}.txt");
		
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
// Aufruf des Processor (list) anpassen ?! -> funktionert diese bei euch? 
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

		/**
		 **--------------------
		 **Newsletter-Mail-Route
		 **--------------------
		 */
		
		EnrichWithSubscribers enrichWithSubscribers;

		/* TODO:
		 * Artist-Pojo Klasse verfeinern
		 * richtig aggregieren: momentan pro Artist eine Message (Ellie Goulding wird einmal mit anderen Location geschluckt)
		 * -> es müssen alle Artists von einem Subscriber in einer Message sein
		 * Überlegung, wie Pojo bzw. Template aussieht (welche Teile dynamisch: beispiel Last.fm -> wenn kein Event stattfindet)
		 * Mail mit Subscriber enrichen (from, to, subject) als Header setzen
		 * Senden via SMTP 
		 * */
		
		
//		from("timer:newsletter?period=86400000") //can be set to specific time "time=yyyy-MM-dd HH:mm:ss" or just set the period to one day "period=86400000"
//		.log("--------------------timer fired..--------------------------------").
//		split(body()).
//		process(enrichWithSubscribers).
//		to("smtp://").
//		log("-------------------FINISHED--------------------------------------");

		
		/// test
		from("timer://newsletter?repeatCount=1&delay=0")
		.log("--------------------timer fired..--------------------------------")
	// setHeader caller: hipchat? @Peter fragen
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
		//.setBody(simple("select artist from subscriptions where email = '${body}'"))
		.setBody(simple("SELECT subscriptions.artist, locations.location FROM subscriptions,locations WHERE subscriptions.email='${body}' and locations.email = '${body}' "))
	// toDO: Locations rausfiltern -> Kreuzprodukt
	// .setBody(simple("select artist from subscriptions where email = '${body}'"))
		.to("jdbc:accounts")
		.process(new SubscriberLocationProcessor())
		.split(body())
		.process(new HeaderChangerProcessor())
			// für Testzwecke
			//.convertBodyTo(String.class)
			//.to("file:fm-out?fileName=getArtistMessage_${date:now:yyyyMMdd_HHmmssSSS}.txt")
		.to("direct:mongoGetFullArtist");
		
			//		from("direct:sendNewsletter")
			//		.log("--------------------sendNewsletter..--------------------------------")
					//.setHeader("youtube", body())
					//.setHeader("type", simple("twitter"))
			//		.log("------------------Sending Newsletter to File")
					//.to("file:fm-out?fileName=getPlaylist_${date:now:yyyyMMdd_HHmmssSSS}.txt")
//		.to("direct:mongoGetFullArtist");
		
		from("direct:aggregateAll")
				//	.split(body())
		.aggregate(header("artist"), new NewsletterFullArtist()) //header("subscriber")
		.completionInterval(5000)
	    .log("********************** Aggregator ALL  **************************")
	    .log("------------------Sending Newsletter to File")
	    //.to("velocity:file:template/newsletter.vm")		// eig Template 
	    .to("velocity:file:template/test.vm").id("velocityTemplate")
	    .convertBodyTo(String.class)
		.to("file:fm-out?fileName=getFullArtistMessage_${date:now:yyyyMMdd_HHmmssSSS}.txt");
		
//		.pollEnrich("direct:mongoGetArtist", new GrabberAggregationStrategy())
		//.setHeader("Newsletter",simple("newsletter-generation"))
		//.setHeader("subject", simple("New incident: first hello")) //${header.CamelFileName}		
//		
////		.setHeader("Subject", constant("Thanks for ordering"))
////		.setHeader("From", constant("donotreply@riders.com"))
//		//.process(new HipchatMessageProcessor())
//		//.to("velocity:file:template/newsletter.vm")
////		.to("velocity:file:template/newsletter.vm")
////		.split(new LastFMSplitExpression())
////		.to("file:fm-out?fileName=lastFM_${date:now:yyyyMMdd_HHmmssSSS}.txt");
//	//	.to("smtp://" + p.getProperty("email.host") + "?password=" + p.getProperty("email.password")+"&From="+p.getProperty("email.user") +"&to="+p.getProperty("email.user")); 
//
//		
// end testing

		
		
		
		
		// Sending Newsletter -> Versuch
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
		
// Funktioniert diese bei euch? 
		// Amazon grabber starts here
		from("timer://foo?repeatCount=1&delay=0")
		.setBody(simple("select distinct(artist) from subscriptions"))
		.to("jdbc:accounts?outputType=StreamList")
		.split(body()).streaming()
		.setBody(body().regexReplaceAll("\\{artist= (.*)(\\r)?\\}", "$1"))
		.setHeader("artist").body()
		.to("direct:amazon");

		// Amazon Route
		from("direct:amazon")
		.to("metrics:timer:amazon-process.timer?action=start")
		.process(new AmazonRequestCreator())
		.recipientList(header("amazonRequestURL")).ignoreInvalidEndpoints()
		.split().tokenizeXML("Item").streaming()
		.setHeader("amazon_uid").xpath("/Item/ASIN/text()", String. class)
		.setHeader("amazon_title").xpath("/Item/ItemAttributes/Title/text()", String. class)
		.setHeader("amazon_pageurl").xpath("/Item/DetailPageURL/text()", String. class)
		.setHeader("amazon_imageurl").xpath("/Item/LargeImage/URL/text()", String. class)
		.setHeader("amazon_price").xpath("/Item/OfferSummary/LowestNewPrice[1]/FormattedPrice/text()", String. class)
		.aggregate(header("artist"), new AmazonAggregationStrategy()).completionTimeout(5000)
		.to("metrics:timer:amazon-process.timer?action=stop")
		.process(new AmazonMongoTester())
		.to("direct:mongoInsert");		
		
		
		//Inserts data about artist into MongoDB overwrites if already existing
		from("direct:mongoInsert")
		.to("metrics:timer:mongo-insert.timer?action=start")
		.filter(body().isNotEqualTo("{}"))
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
		.choice().when(header("caller").isEqualTo("hipchat")).to("direct:sendHipchat").end();
		//.choice().when(header("caller").isEqualTo("newsletter")).to("direct:sendNewsletter").end();

		
		//gets all data for a single Artist
		//routes to mongoGetTwitterYTAmazon and
		//mongoGetLastFM
		from("direct:mongoGetFullArtist")
				//.convertBodyTo(String.class)
				//.to("file:fm-out?fileName=getArtistMessageFullZeug_${date:now:yyyyMMdd_HHmmssSSS}.txt")
		.to("metrics:timer:mongo-getFullArtist.timer?action=start")
		.multicast().to("direct:mongoGetTwitterYTAmazon", "direct:mongoGetLastFM");
		
		String in[] = new String[] {"twitter", "youtube", "amazon"};
		DBObject querryHelper = BasicDBObjectBuilder.start().add("$in", in).get();
		DBObject querry = BasicDBObjectBuilder.start().add("_id", querryHelper).get();			
		
		//gets data for single artist for twitter
		//amazon and youtube. Needs to be don seperatly
		//so we can filter for location in lastFM route
		from("direct:mongoGetTwitterYTAmazon")
		.setBody().constant(querry)
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=findAll"))
		.split().body()
		.process(new MongoResultProcessor())
		.to("direct:endMongoGetFullArtist");

		
		//gets data for a single artist from
		//lastFM add header "location" to filter
		//by location
		from("direct:mongoGetLastFM")
		.setBody()
		.simple("lastFM")
		.process(new MongoFixArtistString())
		.process(new MongoFilterProcessor())
		.recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=findById"))
		.process(new MongoResultProcessor())
		.to("direct:endMongoGetFullArtist");
		
		
		//Aggregates the messages for FullArtist 
		from("direct:endMongoGetFullArtist")
		.aggregate(header("artist"), new MongoAggregationStrategy()).completionInterval(5000) //subscriber
		.to("metrics:counter:mongo-getFullArtist.counter")
	//	.to("mock:sortArtists")
		.to("direct:chooseCallFullArtist")
		.to("metrics:timer:mongo-getFullArtist.timer?action=stop");
		
		from("direct:chooseCallFullArtist")
	//	.choice().when(header("caller").isEqualTo("hipchat")).to("direct:sendHipchat").end()
		.choice().when(header("caller").isEqualTo("newsletter")).to("direct:aggregateAll").end();

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

//		     from("timer://runOnce?repeatCount=1&delay=5000")
//		     .to("direct:testFindAll");
//		     .to("direct:testInsert");
//		     .to("direct:testFindById");
		//     .to("direct:testRemove");
		//     .to("direct:mongoGetArtists");
		     
		HashMap<String,HashMap<String,String>> mongoTest = new HashMap<String,HashMap<String,String>>();
		HashMap<String,String> mongoTest2 = new HashMap<String,String>();
		mongoTest2.put("Foo", "Bar");
		HashMap<String,String> mongoTest3 = new HashMap<String,String>();
		mongoTest3.put("Fun", "Park");
		mongoTest.put("St. Pölten", mongoTest2);
		mongoTest.put("Wien", mongoTest3);
		mongoTest.put("New York", mongoTest2);	
		
		from("direct:testInsert")
		.setHeader("artist")
		.simple("blind guardian")
		.setHeader("type")
		.simple("schön")
		.setBody().constant(mongoTest)
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
		.simple("schön")    
//		.setHeader("location")
//		.simple("St. Pölten, Wien")
		.setBody()
		.simple("${header.type}")
		.process(new MongoFilterProcessor())
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=test&collection=${header.artist}&operation=findById"))
		.to("log:mongo:findById1?level=INFO")
		.process(new MongoResultProcessor())
		.to("log:mongo:findById2?level=INFO");

		String hin[] = new String[] {"test", "test3"};
		DBObject helper = BasicDBObjectBuilder.start().add("$in", hin).get();
		DBObject testQuerry = BasicDBObjectBuilder.start().add("_id", helper).get();		
		
		from("direct:testFindAll")
		.setHeader("artist")
		.simple("blind guardian")
//		.setBody().constant(testQuerry)
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=test&collection=${header.artist}&operation=findAll"))
		.to("log:mongo:findAll1?level=INFO")
		.split().body()
		.to("log:mongo:findAll2?level=INFO")   
		.process(new MongoResultProcessor())
		.aggregate(header("artist"), new MongoAggregationStrategy()).completionInterval(5000)
		.to("log:mongo:findAll3?level=INFO");    	

	}  
}
