package twitter;

import java.util.Properties;

import main.Launcher;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class ArtistFinder implements Processor {

	Properties p = Launcher.properties;

	@Override
	public void process(Exchange arg0) throws Exception {
		Message m = arg0.getIn();

		String body = (String) m.getBody();

		m.setHeader("artist", body);


		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(p.getProperty("twitter.consumerkey"))
		.setOAuthConsumerSecret(p.getProperty("twitter.consumersecret"))
		.setOAuthAccessToken(p.getProperty("twitter.accesstoken"))
		.setOAuthAccessTokenSecret(p.getProperty("twitter.accesstokensecret"));

		Twitter twitter = new TwitterFactory(cb.build()).getInstance();

		ResponseList<User> users = twitter.searchUsers(body, 0);
		User u = users.get(0);

		m.setHeader("CamelTwitterKeywords", "from:" + u.getScreenName());

	}

}
