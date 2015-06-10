package newsletter;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class HeaderChangerProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message out = exchange.getIn();
		
		String newBody="";
		System.out.println("------headerChanger -------"+out.getBody().toString());
		System.out.println("------headerChanger -------"+out.getBody().getClass().toString());
		
		HashMap<String, Object> oldBody = (HashMap<String, Object>) out.getBody();
		
		out.setHeader("artist", oldBody.get("artist"));
		out.setHeader("location", oldBody.get("location"));
		
		System.out.println("Header in HeaderChanger: "+out.getHeaders().toString());

	}

}
