package lastFM;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;


public class LastFMProcessor implements Processor {

	private String artist;
	private String location;
	private String API_KEY; 
	

	public LastFMProcessor(){
		
	}
	public LastFMProcessor(String artist, String location, String apiKey){
		this.artist = artist; 
		this.location = location;
		this.API_KEY = apiKey;
	}
	
	@Override
	public void process(Exchange arg0) throws Exception {
		
		
		Message out = arg0.getIn().copy();
		System.out.println("Artist: "+this.artist);
		
		
		String body = "";
		
		LastFMService lfm = new LastFMService(this.artist, this.API_KEY);
	//	List<pojo.Event> eventList = lfm.getUpcomingEvents(this.artist, this.location);
	//	List<pojo.Event> eventList = lfm.getUpcomingEvents("Ellie Goulding", "Vienna");
		List<pojo.Event> eventList = lfm.getUpcomingEventsInGeo(this.artist, this.location);
    	
  //  	lfm.getUpcomingEventsInGeo("Ellie Goulding", "St. Pölten");
		body +="Artist "+"Eventname "+"City-Location "+"Country-Location "+"Date\n";
		
//		System.out.println("GRoESSe: "+eventList.size());
		
		for(pojo.Event e:eventList){
			body += e.getArtist()+
					" - "+e.getTitle()+
					" - "+e.getCity()+
					" - "+e.getCountry()+
					" - "+e.getDate()+
					"\n";
		}

		out.setBody(body);
		arg0.setOut(out);
	}

}
