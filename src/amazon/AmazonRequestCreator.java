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
		
//		m.setHeader("artist", m.getBody());
		m.setHeader("type", "amazon");
		
		final Map<String, String> amazonParams = new HashMap<String, String>();

		amazonParams.put("AssociateTag", "none");
		amazonParams.put("Service", "AWSECommerceService");
		amazonParams.put("Operation", "ItemSearch");
		amazonParams.put("SearchIndex","Music");  // combined SearchIndex of "Classical", "DigitalMusic" and "MusicTracks"
		amazonParams.put("Artist", m.getBody().toString());
		amazonParams.put("Sort","publication_date");
		// amazonParams.put("Sort","releasedate");
		amazonParams.put("Condition","New");
		amazonParams.put("ResponseGroup", "Large");
		
		SignedRequestsHelper signHelper = null;

		try {
			signHelper = new SignedRequestsHelper();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		  catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
  		  }

		String signedParams = signHelper.sign (amazonParams);

//		m.setBody("http://" + Launcher.properties.getProperty("amazon.endpoint") + "/onca/xml?" + signedParams);
        m.setHeader("amazonRequestURL", "http4://" + Launcher.properties.getProperty("amazon.endpoint") + "/onca/xml?" + signedParams);
	}
}