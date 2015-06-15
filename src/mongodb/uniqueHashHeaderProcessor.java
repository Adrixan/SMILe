package mongodb;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class uniqueHashHeaderProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();
		
		
		String body = out.getBody().toString();
		String subscriber = out.getHeader("subscriber").toString();
		String type = out.getHeader("type").toString();
		int hash = (body + subscriber + type).hashCode();

		out.setHeader("hash", hash);

		arg0.setOut(out);
	}

}
