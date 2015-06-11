package youtube;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class YoutubeChannelProcessor implements Processor {


	@Override
	public void process(Exchange exchange) throws Exception {
		Message m = exchange.getIn();
		//get the artist from the body
		String artist = (String) m.getHeader("artist");
		
		//System.out.println("Artist: "+artist);

		//Set the body to the playlist info
		PlaylistFinder finder = new PlaylistFinder(artist);

		HashMap<String,String> playlistinfo = finder.getPlaylistInfo();
		m.setBody(playlistinfo);
		exchange.setIn(m);

	}

}
