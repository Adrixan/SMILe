package helper;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class ArtistDefiner implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Message m = arg0.getIn();

		HashMap<String,String> body = (HashMap<String,String>) m.getBody();

		m.setHeader("artist", body.get("artist").trim());

	}

}
