package lastFM;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
		String locationFromBody="";
		
		HashMap<String,String> body = (HashMap<String,String>) m.getBody();
		
		m.setHeader("artist", body.get("artist").trim());
		m.setHeader("type", "lastFM");
        
        LastFMService lfm = new LastFMService(body.get("artist"), p.getProperty("lastFM.apiKey"));
     /*   if(body.get("location").contains("oe")){
        	locationFromBody=body.get("location").replaceAll("oe", "�");
        }*/
		List<pojo.Event> eventList = lfm.getUpcomingEvents(body.get("artist"), body.get("location"));//locationFromBody);
		//List<pojo.Event> eventList = lfm.getUpcomingEventsInGeo(body.get("artist"), body.get("location"));
		
		// TODO: �berpr�fung, wenn keine Events vorhanden -> Dead Letter Channel
		
		if(eventList.isEmpty()==false && !eventList.equals(null)){
			Log.debug("Events gefunden");
		}
		else{
			Log.debug("Keine Events gefunden");
		}
		
		//body setzen -> HM <Location, HM<Name von Event: Datum>>
		// HM <Location, HM<Name von Event Datum : Webseite>>
		HashMap<String,HashMap<String,String>> eventsForMongo = new HashMap<String,HashMap<String,String>>();
		String location="";
		String website="";
		
		for(pojo.Event e: eventList){
			location = e.getCity();
			HashMap<String, String> hashi;
			
			if(eventsForMongo.containsKey(location)){
				hashi = eventsForMongo.get(location);
				
				if(e.getWebsite().equals("") || e.getWebsite()==null){
					website="No Website available";
				}else{
					website=e.getWebsite().toString();
				}
				
				hashi.put(e.getTitle().toString()+ " "+e.getDate().toString(), website);
			}else{
				hashi = new HashMap<String, String>();
				
				if(e.getWebsite().equals("") || e.getWebsite()==null){
					website="No Website available";
				}else{
					website=e.getWebsite().toString();
				}
				
				hashi.put(e.getTitle().toString()+" "+e.getDate().toString(), website);
			}
			
			// Ausgabe, welche Events f�r welchen Artist gespeicher wurden
			for (Entry<String, HashMap<String,String>> ent : eventsForMongo.entrySet()){
				Log.debug("Last.FM: "+ent.getKey()+" und Value"+ent.getValue().toString());
			}
			
			eventsForMongo.put(location, hashi);
			
		}
		
		
		
		m.setBody(eventsForMongo);
		
		arg0.setIn(m);
		
	}

}
