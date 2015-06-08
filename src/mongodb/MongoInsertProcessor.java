package mongodb;

import java.util.HashMap;
import java.util.Iterator;

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
		HashMap hm = new HashMap();
		
		hm = (HashMap) arg0.getIn().getBody();
		HashMap insertMap = new HashMap();
		
		hm.forEach( (k,v) -> insertMap.put(k.toString().replaceAll("\\.", "[p]"), v));
		insertMap.put("_id", (String) out.getHeader("type"));
		DBObject insertObj = new BasicDBObject(insertMap);

		out.setBody(insertObj);
		arg0.setOut(out);
	}

}
