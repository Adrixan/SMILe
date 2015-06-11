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

		HashMap<String, ?> hm;
		HashMap body = new HashMap();
		
		if (arg0.getIn().getBody() != null) {
		
			hm = (HashMap) ((DBObject) arg0.getIn().getBody()).toMap();
			hm.forEach( (k,v) -> body.put(k.toString().replaceAll("\\[p\\]", "."), v));
			body.forEach( (k,v) -> {
				if (v.toString().contains("{")){
					body.put(k, (HashMap) ((DBObject) v).toMap());
				}
			});
		}

		String artist;		
		artist = (String) out.getHeader("artist");
		artist = artist.replaceAll("_", " ");  		
		out.setHeader("artist", artist);
		out.setBody(body);
		arg0.setOut(out);
	}
}
