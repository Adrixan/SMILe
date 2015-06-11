package mongodb;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class MongoFixArtistString implements Processor{

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();
		String artist;		
		artist = (String) out.getHeader("artist");
		artist = artist.replaceAll(" ", "_");    	

		out.setHeader("artist", artist);
		arg0.setOut(out);
	}
}
