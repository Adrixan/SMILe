package mongodb;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.oracle.xmlns.internal.webservices.jaxws_databinding.ExistingAnnotationsType;

public class MongoFilterProcessor implements Processor{
	
	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();

		if (arg0.getIn().getHeader("location") != null) {
			String location = arg0.getIn().getHeader("location").toString();
			location = location.replaceAll("\\.", "[p]");
			String[] locations = location.split(",");
			
			DBObject fieldFilter = BasicDBObjectBuilder.start().add("_id", 1).get();

			for (int i = 0; i < locations.length; i++) {
				System.out.println(locations[i]);
				fieldFilter.put(locations[i].trim(), 1);
			}
			
			out.setHeader("CamelMongoDbFieldsFilter", fieldFilter);
		}
		
		arg0.setIn(out);
	}
}
