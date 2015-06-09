package newsletter;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NewsletterProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(NewsletterProcessor.class);
	
	@Override
	public void process(Exchange arg0) throws Exception {
		String input = ((HashMap<String,String>) arg0.getIn().getBody()).get("list");
		
	//	String inputHeader = ((HashMap<String,String>) arg0.getIn().getBody()).get("_id");
		
	//	Log.info("################################inputHeader: "+inputHeader.toString());
		
		Map<String,Object> headers = arg0.getIn().getHeaders();
		
		if(headers.containsKey("youtube")){
			Log.info("################################Youtube ist enthalten");
		}
		
		Map<String,String> map = new HashMap<String,String>();
		ObjectMapper mapper = new ObjectMapper();
	 
		try {
	 
			//convert JSON string to Map
			map = mapper.readValue(input, 
			    new TypeReference<HashMap<String,String>>(){});
	 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		String playlist = map.get("playlist");
		
		String out = "YouTube playlist for subscriber: " + (String) headers.get("subscriber") + " and Artist: " + (String) headers.get("artist") + " " + playlist;
		
		arg0.getIn().setBody(out);

	}


}
