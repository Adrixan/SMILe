package mongodb;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoResultProcessor implements Processor{

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();

		HashMap body = new HashMap();
		DBObject DbObj = new BasicDBObject();
		if (arg0.getIn().getBody() != null) {
			System.out.println("found artist: " + out.getHeader("artist"));
			
//			StringToHashMap sthm = new StringToHashMap();
//			body = sthm.StringToHashMap(arg0.getIn().getBody().toString());
			
			DbObj = (DBObject) arg0.getIn().getBody();
			body = (HashMap) DbObj.toMap();
		}

		String artist;		
		artist = (String) out.getHeader("artist");
		artist = artist.replaceAll("_", " ");  		
//		System.out.println(body);
		out.setHeader("artist", artist);
		out.setBody(body);
		arg0.setOut(out);
	}
}
