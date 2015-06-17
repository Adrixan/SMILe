package mongodb;

import java.util.HashMap;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class MongoSeperateInsertProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();

		System.out.println("insert artist: " + out.getHeader("artist"));
		System.out.println("insert type: " + out.getHeader("type"));
		HashMap hm = new HashMap();
		DBObject insertObj = new BasicDBObject();
		
		hm = (HashMap) arg0.getIn().getBody();
		HashMap insertMap = new HashMap();
		
		hm.forEach( (k,v) -> {
			insertMap.put("data", v);
			insertMap.put("_id", k);
		});
		insertMap.put("type", (String) out.getHeader("type"));
		insertObj = new BasicDBObject(insertMap);
		
		out.setBody(insertObj);
		arg0.setOut(out);
	}

}
