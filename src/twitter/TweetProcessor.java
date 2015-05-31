package twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import main.Launcher;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import twitter4j.Status;


public class TweetProcessor implements Processor {
	
	Properties p = Launcher.properties;

	@Override
	public void process(Exchange arg0) throws Exception {
		Message m = arg0.getIn();
		
		ArrayList<Status> body = (ArrayList<Status>) m.getBody();
		
		m.setHeader("type", "twitter");
		
		System.out.println("ARTIST:" + m.getHeader("artist"));
		
		HashMap<String,String> out = new HashMap<String,String>();
		
		for(Status s : body)
			out.put(Long.toString(s.getId()), s.getText() + " ++++++ " + s.getUser().getScreenName());
		
		m.setBody(out);
		
		arg0.setIn(m);
	}

}
