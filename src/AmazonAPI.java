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
//import org.apache.camel.Exchange;
//import org.apache.camel.Processor;
//import org.apache.camel.component.file.FileComponent;
//import org.apache.camel.component.http4.*;
//import org.apache.camel.component.timer.*;


public class AmazonAPI {
	
	public static Properties properties;
	
	public static void main(String[] args) throws Exception {
	
		try {
			properties = new Properties();
			properties.load(new FileInputStream("smile.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final Map<String, String> myParams = new HashMap<>();
		
		myParams.put("AssociateTag", "none");
		myParams.put("Service", "AWSECommerceService");
		myParams.put("Operation", "ItemSearch");
		myParams.put("SearchIndex","Music");
		myParams.put("Artist","Mika Vember");
//		myParams.put("Keywords","");
		
	    SignedRequestsHelper signHelper = null;
        
		try {
			signHelper = new SignedRequestsHelper();
		} catch (InvalidKeyException | NoSuchAlgorithmException| UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
        final String signedURL = signHelper.sign (myParams);

        System.out.println(signedURL);
		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {
			
			public void configure() {				
				from("timer://foo?fixedRate=true&delay=0&period=10000").
				to(signedURL).
			    to("file:out?fileName=amazon.xml");
			}
		});
			
		context.start();
		Thread.sleep(10000);
		context.stop();
	    
	}    
}