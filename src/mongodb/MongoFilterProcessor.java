package mongodb;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class MongoFilterProcessor implements Processor{
	
	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();
		
		if (arg0.getIn().getHeader("location").toString() != null) {
			String location = arg0.getIn().getHeader("location").toString();
			location = location.replaceAll("\\.", "[p]");
			DBObject fieldFilter = BasicDBObjectBuilder.start().add("_id", 1).add(location, 1).get();
			out.setHeader("CamelMongoDbFieldsFilter", fieldFilter);
		}
		
		arg0.setOut(out);
	}
}
