import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
// import org.apache.camel.component.http4.HttpOperationFailedException;

public class AmazonAPI {
	
	public static Properties properties;
	
	public static void main(String[] args) throws Exception {
	
		try {
			properties = new Properties();
			properties.load(new FileInputStream("smile.properties"));
		} catch (FileNotFoundException e) {
			System.out.println("FATAL: File smile.properties not found.");
			System.exit(1);
			// e.printStackTrace();
		} catch (IOException e) {
			System.out.println("FATAL: File smile.properties could not be read.");
			System.exit(1);
			// e.printStackTrace();
		}
		
		final Map<String, String> amazonParams = new HashMap<>();
		
		amazonParams.put("AssociateTag", "none");
		amazonParams.put("Service", "AWSECommerceService");
		amazonParams.put("Operation", "ItemSearch");
		amazonParams.put("SearchIndex","Music");
		amazonParams.put("Artist","Hubert von Goisern");
//		amazonParams.put("Keywords","");
		
	    SignedRequestsHelper signHelper = null;
        
		try {
			signHelper = new SignedRequestsHelper();
		} catch (InvalidKeyException | NoSuchAlgorithmException| UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
        final String signedParams = signHelper.sign (amazonParams);

		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {
			
			public void configure() {				
				from("timer://foo?fixedRate=true&delay=0")
				  .to("http4://" + properties.getProperty("amazon.endpoint") 
				   		         + "/onca/xml?throwExceptionOnFailure=false&" + signedParams) 
			      .to("file:out?fileName=amazon.xml");

// SWOBI: Camel Exception Handling (doTry - doCatch)				
//				  .doTry()
//			         .to("http4://" + properties.getProperty("amazon.endpoint") 
// 					                + "/onca/xml?" + signedParams)
//			         .to("file:out?fileName=amazon.xml");
//			      .doCatch(HttpOperationFailedException.class)
//			         .to("file:out?fileName=error.xml")
//			      .end(); 				
			         
			}
		});
			
		context.start();
		Thread.sleep(10000);
		context.stop();
	    
	}    
}