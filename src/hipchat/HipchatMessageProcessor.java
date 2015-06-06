package hipchat;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HipchatMessageProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		String input = ((HashMap<String,String>) arg0.getIn().getBody()).get("list");
		
		Map<String,Object> headers = arg0.getIn().getHeaders();
		
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
