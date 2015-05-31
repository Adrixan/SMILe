package main;


import java.util.Properties;

import mongodb.HeaderSplitterBean;
import mongodb.MongoByArtistProcessor;
import mongodb.MongoFixArtistString;
import mongodb.MongoInsertProcessor;
import mongodb.MongoResultProcessor;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import subscriptionhandler.EmailModifyProcessor;
import subscriptionhandler.EmailSubscribeProcessor;
import subscriptionhandler.EmailUnsubscribeProcessor;
import subscriptionhandler.SqlSplitExpression;
import twitter.ArtistFinder;
import twitter.TweetProcessor;

public class SimpleRouteBuilder extends RouteBuilder {


	@Override
	public void configure() throws Exception {
		// TODO Auto-generated method stub
		Properties p = Launcher.properties;

//		from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
//		.choice()
//		.when(header("Subject").isEqualTo("subscribe")).to("direct:subscribe")
//		.when(header("Subject").isEqualTo("unsubscribe")).to("direct:unsubscribe")
//		.when(header("Subject").isEqualTo("modify")).to("direct:modify")
//		.end();
//
//		from("direct:subscribe").process(new EmailSubscribeProcessor()).to("direct:writedb");
//		from("direct:unsubscribe").process(new EmailUnsubscribeProcessor()).to("direct:writedb");
//		from("direct:modify").process(new EmailModifyProcessor()).to("direct:writedb");
//
//		from("direct:writedb").split(new SqlSplitExpression()).wireTap("file:out").end().to("jdbc:accounts").end();
//
//		from("timer://foo?repeatCount=1&delay=0")
//		   .setBody(simple("select distinct(artist) from subscriptions"))
//        .to("jdbc:accounts?outputType=StreamList")
//        .split(body()).streaming()
//        .setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
//        .process(new ArtistFinder())
//        .to("direct:twittergrabber");


		// we can only get tweets from the last 8 days here!!!
//		from("direct:twittergrabber")
//		.to("twitter://search?consumerKey="+ p.getProperty("twitter.consumerkey")+"&consumerSecret="+p.getProperty("twitter.consumersecret")+"&accessToken="+p.getProperty("twitter.accesstoken")+"&accessTokenSecret="+p.getProperty("twitter.accesstokensecret"))
//		.process(new TweetProcessor())
//		.to("mock:insert");
		
		// LastFM grabber starts here 
//		from("timer://foo?repeatCount=1&delay=0")
//		.setBody(simple("select distinct(artist) from subscriptions"))
//	     .to("jdbc:accounts?outputType=StreamList")
//	     .split(body()).streaming()
//	     .setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
//	     .process(new ArtistFinder())
//	     .to("direct:LastFM");
//
//
//    	log.debug("------------------------  KEY ----------------------------"+p.getProperty("lastFM.apiKey"));
    	
//    	from("direct:LastFM")
//    	.process(new LastFMProcessor("Ellie Goulding", "St. Pölten", ""+p.getProperty("lastFM.apiKey"))).split(new LastFMSplitExpression())
//    	.to("file:fm-out");
    	//.to("file:fm-out?fileName=lastFM_${date:now:yyyyMMdd_HHmmssSSS}.txt");
    	
//    	from("direct:LastFM")
//    	.process(new lastFM.LastFMProcessor("Ellie Goulding", "Vienna", ""+p.getProperty("lastFM.apiKey"))).split(new lastFM.LastFMSplitExpression())
//    	.to("file:fm-out");
    	      
       

       
       //Inserts data about artist into MongoDB overwrites if already existing
       from("direct:mongoInsert")
       .process(new MongoInsertProcessor())
       .process(new MongoFixArtistString())
       .recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=save"));       
       
       //gets one grabber from a single artist collection
       from("direct:mongoGetArtist")
       .setBody()
       .simple("${header.type}")
       .process(new MongoFixArtistString())
       .recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=findById"))
       .process(new MongoResultProcessor())
       .to("mock:sortArtists");
       
       //gets all data for a single Artist
       //atm still returns multiple messages
       from("direct:mongoGetFullArtist")
       .process(new MongoFixArtistString())
       .recipientList(simple("mongodb:mongoBean?database=smile&collection=${header.artist}&operation=findAll"))
       .split().body()
       .process(new MongoResultProcessor())
       .to("mock:sortArtists");       
       
       
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