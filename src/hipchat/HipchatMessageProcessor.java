package hipchat;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HipchatMessageProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(HipchatMessageProcessor.class);

	@Override
	public void process(Exchange arg0) throws Exception {
		String playlist = ((HashMap<String,String>) arg0.getIn().getBody()).get("playlist");

		Map<String,Object> headers = arg0.getIn().getHeaders();

		logger.info("#######################################Playlist: "+playlist + " for: " + (String) headers.get("subscriber"));

		String out = "YouTube playlist for subscriber: " + (String) headers.get("subscriber") + " and Artist: " + (String) headers.get("artist") + " " + playlist;

		arg0.getIn().setBody(out);

	}

}
