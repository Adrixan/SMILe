package mongodb;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class MongoInsertProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();

		System.out.println("insert artist: " + out.getHeader("artist"));
		System.out.println("insert type: " + out.getHeader("type"));

		DBObject insertObj = new BasicDBObject();
		insertObj.put( "_id", (String) out.getHeader("type"));
		
		System.out.println(arg0.getIn().getBody());
		String json = "";
		try {
			 
			ObjectMapper mapper = new ObjectMapper();

			HashMap hm = new HashMap();
			hm = (HashMap) arg0.getIn().getBody();
	 
			//convert map to JSON string
			json = mapper.writeValueAsString(hm);
		
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		insertObj.put("list", json);
		out.setBody(insertObj);
		arg0.setOut(out);
	}

}
