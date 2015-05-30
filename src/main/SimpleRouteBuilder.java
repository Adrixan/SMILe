package main;

import java.util.Properties;

import lastFM.EventFinder;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;

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

		from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
		.choice()
		.when(header("Subject").isEqualTo("subscribe")).to("direct:subscribe")
		.when(header("Subject").isEqualTo("unsubscribe")).to("direct:unsubscribe")
		.when(header("Subject").isEqualTo("modify")).to("direct:modify")
		.end();

		from("direct:subscribe").process(new EmailSubscribeProcessor()).to("direct:writedb");
		from("direct:unsubscribe").process(new EmailUnsubscribeProcessor()).to("direct:writedb");
		from("direct:modify").process(new EmailModifyProcessor()).to("direct:writedb");

		from("direct:writedb").split(new SqlSplitExpression()).wireTap("file:out").end().to("jdbc:accounts").end();

		from("timer://foo?repeatCount=1&delay=0")
		   .setBody(simple("select distinct(artist) from subscriptions"))
        .to("jdbc:accounts?outputType=StreamList")
        .split(body()).streaming()
        .setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
        .process(new ArtistFinder())
        .to("direct:twittergrabber");


		// we can only get tweets from the last 8 days here!!!
		from("direct:twittergrabber")
		.to("twitter://search?consumerKey="+ p.getProperty("twitter.consumerkey")+"&consumerSecret="+p.getProperty("twitter.consumersecret")+"&accessToken="+p.getProperty("twitter.accesstoken")+"&accessTokenSecret="+p.getProperty("twitter.accesstokensecret"))
		.process(new TweetProcessor())
		.to("mock:mongo");
		
		// LastFM grabber starts here 
		from("timer://foo?repeatCount=1&delay=0")
		.setBody(simple("SELECT subscriptions.artist, locations.location FROM subscriptions,locations WHERE subscriptions.email=locations.email "))
	     .to("jdbc:accounts?outputType=StreamList")
	     .split(body()).streaming()
	      .process(new EventFinder()).split(new lastFM.LastFMSplitExpression())
	      .to("file:fm-out?fileName=lastFM_${date:now:yyyyMMdd_HHmmssSSS}.txt");
		//	.to("mock:mongo");

	}  
}