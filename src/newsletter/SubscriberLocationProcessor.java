package newsletter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class SubscriberLocationProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {


		Message out = exchange.getIn();
		
		String newBody="";
		System.out.println("------Nachricht -------"+out.getBody().toString());
		System.out.println("------Nachricht -------"+out.getBody().getClass().toString());
		//System.out.println("------Nachricht Header -------");
		
	//	HashMap<String, String> input = ((HashMap<String,String>) exchange.getIn().getBody());
	//	System.out.println("Input"+input.toString());
		
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) out.getBody();
			
			HashSet<String> artists = new HashSet<String>();
			HashSet<String> locations = new HashSet<String>();
			
			for ( HashMap<String, Object> item : list) {
				artists.add((String) item.get("artist"));
				locations.add((String) item.get("location"));
			}
			
			System.out.println("Artists: "+artists.toString());
			System.out.println("Locations: "+locations.toString());
			
			StringBuilder newLocation = new StringBuilder();
			
			Iterator<String> iterator = locations.iterator();
			newLocation.append(iterator.next());
			
			while(iterator.hasNext()){
				
				newLocation.append(","+iterator.next());
			}
			
		
			System.out.println("Location: "+newLocation.toString());

			ArrayList<HashMap<String, Object>> betterList = new ArrayList<HashMap<String, Object>>();
			
			for(String a: artists){
				HashMap<String, Object> eintrag = new HashMap<String, Object>();
				
				eintrag.put("artist", a);
				eintrag.put("location", newLocation.toString());
				
				betterList.add(eintrag);
			}
			
			System.out.println("BetterList: "+betterList.toString());
			
			out.setBody(betterList);
			exchange.setIn(out);	//message wieder vom exchange setzen
			
			
			
			
			
//		for (String s : list){
//			System.out.println(s);
//		}

	}

}
