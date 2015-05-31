package mongodb;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import twitter4j.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoResultProcessor implements Processor{

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();
    	
		HashMap<String, String> body = new HashMap<String, String>();
		
		if (arg0.getIn().getBody() != null) {
			System.out.println("found artist: " + out.getHeader("artist"));
			JSONObject JSONBody = new JSONObject(arg0.getIn().getBody().toString());
			Iterator<?> keys = JSONBody.keys();
	//		sBody = arg0.getIn().getBody().toString();
	        while(keys.hasNext()){
	            String key = (String)keys.next();
	            String value = JSONBody.getString(key); 
	            body.put(key, value);
	
	        }		
		}
		
		String artist;		
		artist = (String) out.getHeader("artist");
		artist = artist.replaceAll(" ", "_");  		
		
		out.setHeader("artist", artist);
		out.setBody(body);
		arg0.setOut(out);
	}
}
