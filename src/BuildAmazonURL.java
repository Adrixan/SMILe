import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class BuildAmazonURL implements Processor {

	public BuildAmazonURL () {		  
	}

	public void process(Exchange exchange) throws Exception {
		System.out.println("Body contains: " + exchange.getIn().getBody());

		final Map<String, String> amazonParams = new HashMap<>();

		// Operation:		
		//  ItemSearch ... Find items that are sold on Amazon	
		//  ItemLookup ... returns descriptions of specified items
		//
		// ResponseGroup (control the kind of information returned by the request)
		//  Large, Medium, Small	
		//
		// Debug Paramter: Validate = True	
		amazonParams.put("AssociateTag", "none");
		amazonParams.put("Service", "AWSECommerceService");
		amazonParams.put("Operation", "ItemSearch");
		amazonParams.put("SearchIndex","Music");  // combined SearchIndex of "Classical", "DigitalMusic" and "MusicTracks"
		amazonParams.put("Artist", exchange.getIn().getBody().toString());
		// amazonParams.put("ResponseGroup", "Large");
		// amazonParams.put("Keywords","");

		SignedRequestsHelper signHelper = null;

		try {
			signHelper = new SignedRequestsHelper();
		} catch (InvalidKeyException | NoSuchAlgorithmException| UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String signedParams = signHelper.sign (amazonParams);

		exchange.getIn().setBody("http://{{amazon.endpoint}}/onca/xml?" + signedParams);
		System.out.println("Body contains now: http://{{amazon.endpoint}}/onca/xml?" + signedParams);

	}
}