package newsletter;

import java.util.Properties;

import main.Launcher;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class EnrichWithSubscribers implements Processor {
	
	Properties p = Launcher.properties;
	private static final Logger logger = LoggerFactory.getLogger(EnrichWithSubscribers.class);
	
	@Override
	public void process(Exchange arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	

}
