package mongodb;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoByArtistProcessor implements Processor{
	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();

		String[] artists;
		artists = ((String) out.getHeader("artist")).split(",");


		DBObject searchObj = new BasicDBObject();
		if (artists.length > 1) {
			DBObject[] orArray = new BasicDBObject[artists.length];
			for (int i = 0; i < artists.length; i++) {
				DBObject DBObj = new BasicDBObject();
				System.out.println("by artist: " + artists[i].trim());
				DBObj.put("_id", artists[i].trim());
				orArray[i]=DBObj;
			}
			searchObj.put("$or", orArray);
			//		searchObj.put("artist", artist);
		}
		else {
			searchObj.put("artist", artists[0]);
		}
		out.setBody(searchObj);
		arg0.setOut(out);
	}
}
