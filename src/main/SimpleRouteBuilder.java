package main;

import java.util.Properties;

import lastFM.EventFinder;
import lastFM.LastFMProcessor;
import lastFM.LastFMSplitExpression;
import metrics.MetricsProcessor;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.metrics.MetricsConstants;
import org.apache.camel.component.metrics.routepolicy.MetricsRegistryService;
import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.spi.Registry;

import com.codahale.metrics.MetricRegistry;

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

		from("timer://foo2?repeatCount=5&delay=300")
		.to("metrics:timer:twitter-process.timer?action=start")
		.setBody(simple("select distinct(artist) from subscriptions"))
		.to("jdbc:accounts?outputType=StreamList")
		.split(body()).streaming()
		.setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
		.process(new ArtistFinder())
		.to("direct:twittergrabber")
		.to("metrics:timer:twitter-process.timer?action=stop");


		// we can only get tweets from the last 8 days here!!!
		from("direct:twittergrabber")
		.to("twitter://search?consumerKey="+ p.getProperty("twitter.consumerkey")+"&consumerSecret="+p.getProperty("twitter.consumersecret")+"&accessToken="+p.getProperty("twitter.accesstoken")+"&accessTokenSecret="+p.getProperty("twitter.accesstokensecret"))
		.process(new TweetProcessor())
		.to("metrics:counter:twitter-artists-processed.counter")
		.to("mock:mongo");

		// LastFM grabber starts here 
//				from("timer://foo?repeatCount=1&delay=0")
//				.to("metrics:timer:lastfm-process.timer?action=start")
//				.setBody(simple("select distinct(artist) from subscriptions"))
//			     .to("jdbc:accounts?outputType=StreamList")
//			     .split(body()).streaming()
//			     .setBody(body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
//			     .process(new ArtistFinder()).to("metrics:counter:lastfm-artists-found.counter")
//			     .to("direct:LastFM")
//			     .to("metrics:timer:lastfm-process.timer?action=stop");

		
//		from("direct:LastFM")
//		.process(new lastFM.LastFMProcessor("Ellie Goulding", "Vienna", ""+p.getProperty("lastFM.apiKey"))).split(new lastFM.LastFMSplitExpression())
//		.to("file:fm-out");

// ACHTUNG: direct.LastFM exisitiert so nicht mehr -> umbauen!!! 
				
		// LastFM grabber starts here 
				from("timer://foo?repeatCount=1&delay=0")
				.to("metrics:timer:lastfm-process.timer?action=start")
				.setBody(simple("SELECT subscriptions.artist, locations.location FROM subscriptions,locations WHERE subscriptions.email=locations.email "))
			     .to("jdbc:accounts?outputType=StreamList")
			     .split(body()).streaming()
			      .process(new EventFinder()).split(new lastFM.LastFMSplitExpression()).to("metrics:counter:lastfm-artists-found.counter")
			      .to("file:fm-out?fileName=lastFM_${date:now:yyyyMMdd_HHmmssSSS}.txt")
			      .to("metrics:timer:lastfm-process.timer?action=stop");
			//      .to("direct:LastFM");
			//		.to("mock:mongo"); // TODO: MongoDB Implementation

	
		// Youtube grabber starts here
		from("timer://foo?repeatCount=1&delay=0")
		.to("metrics:timer:youtube-process.timer?action=start")
		.setBody(simple("select distinct(artist) from subscriptions"))
		.to("jdbc:accounts?outputType=StreamList")
		.split(body())
		.streaming()
		.setBody(
				body().regexReplaceAll("\\{artist=(.*)(\\r)?\\}", "$1"))
				.process(new ArtistFinder()).to("direct:youtubeAPI")
				.to("metrics:timer:youtube-process.timer?action=stop");

		from("direct:youtubeAPI").process(new YoutubeChannelProcessor()).to("metrics:counter:YouTube-Playlists-generated.counter").to(
				"file:out?fileName=youtube_${date:now:yyyyMMdd_HHmmssSSS}.txt");
		
		//from("timer://foo1?repeatCount=30&delay=5000")
		
		from("jetty:http://localhost:12345/stats").process(new MetricsProcessor());
		
		//.to("file:out?fileName=metrics_${date:now:yyyyMMdd_HHmmssSSS}.json");
		
// Versuch Velocity //from("direct:a").to("velocity:org/apache/camel/component/velocity/letter.vm").to("mock:result");
		
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

		 * 
		 * */

	}  
}