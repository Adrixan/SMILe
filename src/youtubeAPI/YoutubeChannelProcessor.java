package youtubeAPI;

import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class YoutubeChannelProcessor implements Processor {
	
	private Properties properties;
	
	public YoutubeChannelProcessor(Properties properties) {
		this.properties = properties;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String artist = exchange.getIn().getBody().toString();
		
		System.out.println("Artist: "+artist);
		
		String playlistinfo = KeywordSample.searchChannelTest(artist, properties);

		System.out.println(playlistinfo);
		exchange.getIn().setBody(playlistinfo);
	}

}
