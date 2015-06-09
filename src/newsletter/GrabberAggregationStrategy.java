package newsletter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import main.Launcher;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GrabberAggregationStrategy implements AggregationStrategy{

	Properties p = Launcher.properties;
	private static final Logger logger = LoggerFactory.getLogger(GrabberAggregationStrategy.class);
	
	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		
		HashMap<String, String> input = ((HashMap<String,String>) oldExchange.getIn().getBody());
		HashMap<String, String> grabberList = ((HashMap<String, String>) newExchange.getIn().getBody());
		
		Map<String,Object> headers = oldExchange.getIn().getHeaders();
		
		Map<String,String> map = new HashMap<String,String>();
		ObjectMapper mapper = new ObjectMapper();
	 
		try {
	 
			//convert JSON string to Map
		//	map = mapper.readValue(input, 
		//	    new TypeReference<HashMap<String,String>>(){});
	 
		} catch (Exception e) {
			e.printStackTrace();
		}
		String playlist = map.get("playlist");
		
		String out = "YouTube playlist for subscriber: " + (String) headers.get("subscriber") + " and Artist: " + (String) headers.get("artist") + " " + playlist;
		
		oldExchange.getIn().setBody(out);

		
//		List<Flightoffer> foList = (List<Flightoffer>)oldExchange.getIn().getBody();
//		List<Hotel> hotelList = (List<Hotel>) newExchange.getIn().getBody();
//		
//		for(Flightoffer fo : foList)
//		{
//			for(Hotel item : hotelList)
//			{
//				if (fo.getToIataCode().equals(item.getDestinationAirport().getIataCode()))
//					fo.getHotels().add(item);
//			}
//		}
//       
//        oldExchange.getOut().setBody(foList);
        
		return oldExchange;
	}

}
