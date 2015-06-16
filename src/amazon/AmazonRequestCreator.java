package amazon;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import main.Launcher;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class AmazonRequestCreator implements Processor {

	public AmazonRequestCreator () {		  
	}

	public void process(Exchange exchange) throws Exception {
		Message m = exchange.getIn();
		
		m.setHeader("type", "amazon");
        m.setBody(m.getHeader("artist"));
		
		final Map<String, String> amazonParams = new HashMap<String, String>();

		amazonParams.put("AssociateTag", "none");
		amazonParams.put("Service", "AWSECommerceService");
		amazonParams.put("Operation", "ItemSearch");
		amazonParams.put("SearchIndex","Music");  // combined SearchIndex of "Classical", "DigitalMusic" and "MusicTracks"
		amazonParams.put("Artist", m.getHeader("artist").toString());
		amazonParams.put("Sort","publication_date");
		amazonParams.put("Condition","New");
		amazonParams.put("ResponseGroup", "Large");
		
		SignedRequestsHelper signHelper = null;

		try {
			signHelper = new SignedRequestsHelper();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		  }
		  catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
		  }
		  catch (UnsupportedEncodingException e) {
			e.printStackTrace();
  		  }

		String signedParams = signHelper.sign (amazonParams);

        m.setHeader("amazonRequestURL", "http4://" + Launcher.properties.getProperty("amazon.endpoint") + "/onca/xml?" + signedParams);
	}
}