package newsletter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.bcel.internal.generic.LSTORE;

import pojo.ArtistPojo;
import pojo.LocationPojo;

public class ArtistPojoProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ArtistPojoProcessor.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		
		Message out = exchange.getIn();
		
		String newBody="";
		System.out.println("------Nachricht ArtistPojoProcessor -------"+out.getBody().toString());
		System.out.println("------Nachricht ArtistPojoProcessor -------"+out.getBody().getClass().toString());
		
		HashMap<String, Object> bodyMap =  (HashMap<String, Object>) out.getBody();
		
		
		HashMap<String, Object> youtubeMap = (HashMap<String, Object>) bodyMap.get("youtube");
		HashMap<String, Object> twitterMap = (HashMap<String, Object>) bodyMap.get("twitter");
		HashMap<String, Object> lastFmMap = (HashMap<String, Object>) bodyMap.get("lastFM");
		
		lastFmMap.remove("_id");
		
		ArrayList<Object> tweets = new ArrayList<Object>(twitterMap.values());
		ArrayList<Entry<String,Object>> lastFMLocations = new ArrayList<Entry<String,Object>>(lastFmMap.entrySet());
		
		Map<String,Object> headers = exchange.getIn().getHeaders();
		
		ArtistPojo artistPojo = new ArtistPojo((String)headers.get("artist"));
		artistPojo.setTwitterSection(tweets);
		
		artistPojo.setyChannel((String)youtubeMap.get("channel"));
		artistPojo.setyChannelName((String)youtubeMap.get("title"));
		artistPojo.setyPlaylist((String)youtubeMap.get("playlist"));
		artistPojo.setySubscriber((String)youtubeMap.get("subscribers"));
		
		ArrayList<LocationPojo> locations = new ArrayList<LocationPojo>();
		
		for(Entry<String,Object> e:lastFMLocations){
			LocationPojo lp = new LocationPojo(e.getKey());
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"+lp.getLocationName());
			
			HashMap<String, Object> hashi = (HashMap<String, Object>) e.getValue(); //LocationName Datum: Webseite
			ArrayList<String> event = new ArrayList<String>();
			
			for(Entry<String,Object> eventEntry:hashi.entrySet()){
				String eventName = eventEntry.getKey();
				
				String eventWeb = (String) eventEntry.getValue();
				
				event.add(eventName + " - " + eventWeb);
			}
			/*	if(event.isEmpty()){
				event.add("No events are upcoming!");
			}*/
			lp.setEvents(event);
			locations.add(lp);
		}
		
		if(locations.isEmpty()){
			LocationPojo lp = new LocationPojo();
			lp.setLocationsEmpty();
			locations.add(lp);
		}
		
		artistPojo.setLastFMSection(locations);
		
		// Todo: AmazonSection !!!
		
		
		out.setBody(artistPojo);
		exchange.setIn(out);	
	}

}
