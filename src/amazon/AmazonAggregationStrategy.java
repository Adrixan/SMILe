package amazon;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class AmazonAggregationStrategy implements AggregationStrategy {
		 
	    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

	       final HashMap<String, HashMap<String, String>> amazonResults;
	       final HashMap<String, String> amazonItem = new HashMap<String, String>();
	       
	       amazonItem.put("title", newExchange.getIn().getHeader("amazon_title").toString());
	       amazonItem.put("pageurl", newExchange.getIn().getHeader("amazon_pageurl").toString());
	       amazonItem.put("imageurl", newExchange.getIn().getHeader("amazon_imageurl").toString());
	       amazonItem.put("price", newExchange.getIn().getHeader("amazon_price").toString());
	       
	       if (oldExchange == null) {
	          amazonResults = new HashMap<String, HashMap<String, String>>();
	       } else {
              amazonResults = oldExchange.getIn().getBody(HashMap.class);	    	  
	       }	   
	    	   
		   amazonResults.put(newExchange.getIn().getHeader("amazon_uid").toString(), amazonItem);
		   newExchange.getIn().setBody(amazonResults);
		   return newExchange;
	   }
}

