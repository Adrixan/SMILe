package lastFM;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import main.Launcher;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.mortbay.log.Log;

public class EventFinder implements Processor {
	
	Properties p = Launcher.properties;

	@Override
	public void process(Exchange arg0) throws Exception {
		Message m = arg0.getIn();
		//System.out.println("Message: "+m);
		Log.info("Message: "+m);
		
		HashMap<String,String> body = (HashMap<String,String>) m.getBody();
        
			System.out.println("ARTIST:" + body.get("artist"));
		
		m.setHeader("artist", body.get("artist"));
		m.setHeader("type", "lastFM");
        
        System.out.println(""+p.getProperty("lastFM.apiKey"));
        
        LastFMService lfm = new LastFMService(body.get("artist"), p.getProperty("lastFM.apiKey"));
		List<pojo.Event> eventList = lfm.getUpcomingEvents(body.get("artist"), body.get("location"));
		//List<pojo.Event> eventList = lfm.getUpcomingEventsInGeo(body.get("artist"), body.get("location"));
		
		// TODO: Überprüfung, wenn keine Events vorhanden -> Dead Letter Channel
		
		if(eventList.isEmpty()==false && !eventList.equals(null)){
			System.out.println("Events gefunden");
		}
		else{
			System.out.println("Keine Events gefunden");
		}
		
		//body setzen -> HM <Location, HM<Name von Event: Datum>>
		HashMap<String,HashMap<String,String>> eventsForMongo = new HashMap<String,HashMap<String,String>>();
		String location="";
		
		for(pojo.Event e: eventList){
			location = e.getCity();
			HashMap<String, String> hashi;
			
			if(eventsForMongo.containsKey(location)){
				hashi = eventsForMongo.get(location);
				System.out.println("Contains: "+e.getTitle().toString() + " "+e.getDate().toString()+ " "+e.getWebsite().toString());
				hashi.put(e.getTitle().toString(), e.getDate().toString());
			}else{
				hashi = new HashMap<String, String>();
				System.out.println("Not contains: "+e.getTitle().toString() + " "+e.getDate().toString()+ " "+e.getWebsite().toString());
				hashi.put(e.getTitle().toString(), e.getDate().toString());
			}
			System.out.println("location: "+location);
			
			eventsForMongo.put(location, hashi);
			
		}
		
		
		
		m.setBody(eventsForMongo);
		
		arg0.setIn(m);
		
	}

}
