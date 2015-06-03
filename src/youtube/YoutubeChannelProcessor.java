package youtube;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class YoutubeChannelProcessor implements Processor {


	@Override
	public void process(Exchange exchange) throws Exception {
		//get the artist from the body
		String artist = exchange.getIn().getBody().toString();

		//System.out.println("Artist: "+artist);

		//Set the body to the playlist info
		PlaylistFinder finder = new PlaylistFinder(artist);
		String playlistinfo = finder.getPlaylistInfo();
		exchange.getIn().setBody(playlistinfo);

		//System.out.println(playlistinfo);
	}

}
