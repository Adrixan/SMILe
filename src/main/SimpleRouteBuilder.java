package main;


import helper.ArtistDefiner;
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
import mongodb.UniqueHashHeaderProcessor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import lastFM.EventFinder;
import metrics.MetricsProcessor;
import newsletter.ArtistPojoProcessor;
import newsletter.HeaderChangerProcessor;
import newsletter.NewsletterAggregationStrategy;
import newsletter.SubscriberLocationProcessor;

import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.processor.idempotent.jdbc.JdbcMessageIdRepository;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import amazon.AmazonAggregationStrategy;
import amazon.AmazonRequestCreator;
import subscriptionhandler.EmailModifyProcessor;
import subscriptionhandler.EmailSubscribeProcessor;
import subscriptionhandler.EmailUnsubscribeProcessor;
import subscriptionhandler.SqlSplitExpression;
import twitter.ArtistFinder;
import twitter.TweetProcessor;
import youtube.YoutubeChannelProcessor;

import org.apache.commons.dbcp2.BasicDataSource;

public class SimpleRouteBuilder extends RouteBuilder {


	@Override
	public void configure() throws Exception {
		Properties p = Launcher.properties;
		//Set DeadLetterChannel Route
		errorHandler(deadLetterChannel("direct:DLCRoute"));
		
		/**
		 **--------------------
		 **Dead Letter Channel
		 **--------------------
		 */
		//DeadLetterChannel
		from("direct:DLCRoute")
		.process(new DeadLetterProcessor())
		.to("file:dlc?fileName=exception_${date:now:yyyyMMdd_HHmmssSSS}.txt");
		
		// Enable global metrics support
		MetricsRoutePolicyFactory mrpf = new MetricsRoutePolicyFactory();
		this.getContext().addRoutePolicyFactory(mrpf);
		
		//TODO: Find a way to get datasource from Launch
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://" + p.getProperty("rdbm.host") + "/" + p.getProperty("rdbm.database") +"?"
				+ "user=" + p.getProperty("rdbm.user") +"&password=" + p.getProperty("rdbm.password"));
		
		JdbcMessageIdRepository repo = new JdbcMessageIdRepository(ds, "mongodb");
		
		
		
		from("direct:wiretapLogging").setBody(simple("${headers}\n\n${body}")).convertBodyTo(String.class).to("file:out?fileName=WiretapLogging_${date:now:yyyyMMdd_HHmmssSSS}.txt");

		/**
		 **--------------------
		 **Subscription-Route
		 **--------------------
		 */
		// Subscription handling
		// Polling Consumer
		from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
		.choice()
		.when(header("Subject").isEqualTo("subscribe")).to("direct:subscribe")
		.when(header("Subject").isEqualTo("unsubscribe")).to("direct:unsubscribe")
		.when(header("Subject").isEqualTo("modify")).to("direct:modify")
		.end();

		// Content-based Router
		from("direct:subscribe").process(new EmailSubscribeProcessor()).to("metrics:counter:subscribe.counter").to("direct:writedb");
		from("direct:unsubscribe").process(new EmailUnsubscribeProcessor()).to("metrics:counter:unsubscribe.counter").to("direct:writedb");
		from("direct:modify").process(new EmailModifyProcessor()).to("metrics:counter:modify.counter").to("direct:writedb");

		from("direct:writedb").to("metrics:timer:RDBM-write.timer?action=start")
		.split(new SqlSplitExpression()).to("jdbc:accounts").to("metrics:counter:RDBM-write.counter").end()
		.to("metrics:timer:RDBM-write.timer?action=stop");
		
		/**
		 **--------------------
		 **all Grabbers
		 **--------------------
		 */
		
		// Launch all grabbers
		from("jetty:http://localhost:12345/grabber").multicast().to("direct:startGrabbers","direct:startLastFM");
		
		from("direct:startGrabbers")
		.to("metrics:timer:all-grabbers.timer?action=start")
		.setBody(simple("select distinct(artist) from subscriptions"))
		.to("jdbc:accounts")
		.split(body())
		.process(new ArtistDefiner()).wireTap("direct:wiretapLogging").throttle(1).timePeriodMillis(1000)
		.multicast().parallelProcessing()
		.to("direct:twittergrabber", "direct:youtubeAPI", "direct:amazon")
		.to("metrics:timer:all-grabbers.timer?action=stop");


		/**
		 **--------------------
		 **Twitter Grabber-Route
		 **--------------------
		 */
		// we can only get tweets from the last 8 days here!!!
		from("direct:twittergrabber")
		.setHeader("type",simple("twitter"))
		.wireTap("direct:wiretapLogging")
		.process(new ArtistFinder())
		.to("twitter://search?consumerKey="+ p.getProperty("twitter.consumerkey")+"&consumerSecret="+p.getProperty("twitter.consumersecret")+"&accessToken="+p.getProperty("twitter.accesstoken")+"&accessTokenSecret="+p.getProperty("twitter.accesstokensecret"))
		.process(new TweetProcessor())
		.to("metrics:counter:twitter-artists-processed.counter")
		.wireTap("direct:wiretapLogging")
		.to("direct:mongoInsert");

		/**
		 **--------------------
		 **Hipchat -Route
		 **--------------------
		 */
		// Hipchat playlist workflow
		from("jetty:http://localhost:12345/hipchat")
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

		/**
		 **--------------------
		 **LastFM Grabber-Route
		 **--------------------
		 */
		// LastFM grabber starts here 
		// uses Processor to find Events
		from("direct:startLastFM")
		.to("metrics:timer:lastfm-process.timer?action=start")
		.setBody(simple("SELECT subscriptions.artist, locations.location FROM subscriptions,locations WHERE subscriptions.email=locations.email "))
		.to("jdbc:accounts?outputType=StreamList")
		.split(body()).streaming()
		.process(new EventFinder())
		.to("metrics:counter:lastfm-artists-found.counter")
		.to("direct:mongoInsert")
		.to("metrics:timer:lastfm-process.timer?action=stop");

		/**
		 **--------------------
		 **Amazon Grabber-Route
		 **--------------------
		 */
		// Amazon grabber starts here
		// (uses Processor, Recipientlist, Splitter with Tokenizer, XPath, Custom Aggregation Strategy and WireTap)
		from("direct:amazon")
		.setHeader("type",simple("amazon"))
		.wireTap("direct:wiretapLogging")
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
		.wireTap("direct:wiretapLogging")
		.to("direct:mongoInsert");		
		
		// Amazon grabber starts here

				// Amazon Route
				from("direct:amazon")
				.setHeader("type",simple("amazon"))
				.wireTap("direct:wiretapLogging")
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
				.wireTap("direct:wiretapLogging")
			//	.process(new AmazonMongoTester())
				.to("direct:mongoInsert");	


		/**
		 **--------------------
		 **Youtube Grabber-Route
		 **--------------------
		 */
		// Youtube grabber starts here
		from("direct:youtubeAPI")
			.setHeader("type",simple("youtube"))
			.process(new YoutubeChannelProcessor())
			.to("metrics:counter:YouTube-Playlists-generated.counter")
			.to("direct:mongoInsert");

		/**
		 **--------------------
		 **Metrics -Route
		 **--------------------
		 */
		from("jetty:http://localhost:12345/stats").process(new MetricsProcessor());

		/**
		 **--------------------
		 **Newsletter-Mail-Route
		 **--------------------
		 */
		
		// alle Subscriber aus SQL-DB holen (pro Subscriber eine Message)
		// uses Processor (HeaderChanger, SubscriberLocation), Custom Aggregation Strategy (Message with more Artists for 1 Subscriber) 
		// and Content Enricher (Velocity and Message Merge)
		from("jetty:http://localhost:12345/newsletter")	//can be set to specific time "time=yyyy-MM-dd HH:mm:ss" or just set the period to one day "period=86400000"
		.log("--------------------Newsletter Generation starts--------------------------------")
		.setHeader("caller", simple("newsletter"))
		.setBody(simple("select email from subscriber"))
		.to("jdbc:accounts")
		.split(body())
		.setBody(body().regexReplaceAll("\\{email=(.*)(\\r)?\\}", "$1"))
		.to("direct:getArtistsForNewsletter");
		
		// Artist und Location Info aus SQL-DB holen (pro Artist eine Message)
		from("direct:getArtistsForNewsletter")
		.setHeader("subscriber", body())
		.setBody(simple("SELECT subscriptions.artist, locations.location FROM subscriptions,locations WHERE subscriptions.email='${body}' and locations.email = '${body}' "))
		.to("jdbc:accounts")
		.process(new SubscriberLocationProcessor())
		.split(body())
		.process(new HeaderChangerProcessor())
			// fuer Testzwecke
			//.convertBodyTo(String.class)
			//.to("file:fm-out?fileName=getArtistMessage_${date:now:yyyyMMdd_HHmmssSSS}.txt")
		.to("direct:mongoGetFullArtist");
		
	
		// Newsletter-Aggregation (mit allen Grabber-Daten wird Template befuellt)
		from("direct:aggregateAll")
	    .process(new ArtistPojoProcessor())
	    .aggregate(header("subscriber"), new NewsletterAggregationStrategy())
	    .completionInterval(5000)
	    .to("velocity:file:template/newsletter.vm").id("velocityTemplate")
	    .convertBodyTo(String.class)
	    .setHeader("Subject", constant("SMILe Newsletter"))
	    .process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setHeader("To", exchange.getIn().getHeader("subscriber"));
				System.out.println(exchange.getIn().getHeader("To"));
			}
	    })
	    // Newsletter per SMTP an Subscriber senden
	    .log("------------------Sending Newsletter to Subscriber")
		.to("smtps://"+p.getProperty("email.host")+"?username="+p.getProperty("email.user")
				+"&password="+p.getProperty("email.password")+"&From="+p.getProperty("email.user"));
		
		
		
		/**
		 **--------------------
		 **MongoDB -Routes
		 **--------------------
		 */
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
		.process(new UniqueHashHeaderProcessor())
		.idempotentConsumer(header("hash"), repo)
		.to("metrics:counter:mongo-getArtist.counter")
		.to("metrics:timer:mongo-getArtist.timer?action=stop")
		.to("direct:sendHipchat");

		
		//gets all data for a single Artist
		//routes to mongoGetTwitterYTAmazon and
		//mongoGetLastFM
		from("direct:mongoGetFullArtist")
				//.convertBodyTo(String.class)
				//.to("file:fm-out?fileName=getArtistMessageFullZeug_${date:now:yyyyMMdd_HHmmssSSS}.txt")
		.to("metrics:timer:mongo-getFullArtist.timer?action=start")
		.multicast().parallelProcessing().to("direct:mongoGetTwitterYTAmazon", "direct:mongoGetLastFM");
		
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
		.process(new UniqueHashHeaderProcessor())
		.idempotentConsumer(header("hash"), repo)
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
		.process(new UniqueHashHeaderProcessor())
		.idempotentConsumer(header("hash"), repo)		
		.to("direct:endMongoGetFullArtist");
		
		
		//Aggregates the messages for FullArtist 
		from("direct:endMongoGetFullArtist")
		.aggregate(header("artist").append(header("subscriber")), new MongoAggregationStrategy()).completionInterval(5000) //subscriber
		.to("metrics:counter:mongo-getFullArtist.counter")
	//	.to("mock:sortArtists")
		.to("metrics:timer:mongo-getFullArtist.timer?action=stop")
		.to("direct:aggregateAll");

		
		/****** TEST ROUTES FOR MONGO DB PLZ DONT DELETE *****/       

		// from("timer://runOnce?repeatCount=2&delay=5000")
		// .to("direct:testFindAll");
		// .to("direct:testInsert");
		// .to("direct:testFindById");
		// .to("direct:testRemove");
		// .to("direct:mongoGetArtists");
		     
		HashMap<String,HashMap<String,String>> mongoTest = new HashMap<String,HashMap<String,String>>();
		HashMap<String,String> mongoTest2 = new HashMap<String,String>();
		mongoTest2.put("Foo", "Bar");
		HashMap<String,String> mongoTest3 = new HashMap<String,String>();
		mongoTest3.put("Fun", "Park");
		mongoTest.put("St. P�lten", mongoTest2);
		mongoTest.put("Wien", mongoTest3);
		mongoTest.put("New York", mongoTest2);	
		
		from("direct:testInsert")
		.setHeader("artist")
		.simple("blind guardian")
		.setHeader("type")
		.simple("test")
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
		
		JdbcMessageIdRepository testRepo = new JdbcMessageIdRepository(ds, "test");
		
		from("direct:testFindById")
		.setHeader("artist").simple("blind guardian")
		.setHeader("type").simple("test")
		.setHeader("subscriber").simple("testsub")
		.setBody()
		.simple("${header.type}")
		.process(new MongoFilterProcessor())
		.process(new MongoFixArtistString())
		.recipientList(simple("mongodb:mongoBean?database=test&collection=${header.artist}&operation=findById"))
		.to("log:mongo:findById1?level=INFO")
		.process(new MongoResultProcessor())
		.process(new UniqueHashHeaderProcessor())
		.to("log:mongo:findById2?level=INFO")
		.idempotentConsumer(header("hash"), testRepo)
		.to("direct:wiretapLogging")
		.to("log:mongo:findById3?level=INFO");

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
