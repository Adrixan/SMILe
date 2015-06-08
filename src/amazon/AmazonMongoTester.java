package amazon;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class AmazonMongoTester implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		Message m = exchange.getIn();
		System.out.println("Aggregated Amazon Body contains: " + m.getBody());	
		System.out.println("-------------------------");
	}

}
