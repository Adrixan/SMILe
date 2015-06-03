package mongodb;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

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
    	insertObj.put("list", arg0.getIn().getBody());
    	insertObj.put("test", "TestCase: " + out.getHeader("artist"));
     	
		out.setBody(insertObj);
		arg0.setOut(out);
	}

}
