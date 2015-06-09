package mongodb;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class MongoAggregationStrategy implements AggregationStrategy {
	 
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

       final HashMap<String, HashMap> fullArtist;
       HashMap grabberItem = new HashMap();
       
       if (oldExchange == null) {
    	   fullArtist = new HashMap<String, HashMap>();
       } else {
    	   fullArtist = (HashMap<String, HashMap>) oldExchange.getIn().getBody();	    	  
       }	   
       
       grabberItem = (HashMap) newExchange.getIn().getBody();
       
       fullArtist.put(grabberItem.get("_id").toString(), grabberItem);
	   newExchange.getIn().setBody(fullArtist);
	   return newExchange;
   }
}
