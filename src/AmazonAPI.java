import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Processor; 
// import org.apache.camel.component.http4.HttpOperationFailedException;

public class AmazonAPI {
	
	public static void main(String[] args) throws Exception {
    
		CamelContext context = new DefaultCamelContext();

		PropertiesComponent pc = new PropertiesComponent();
		pc.setLocation("file:smile.properties");
		
		context.setTracing(true);
		
		context.addComponent("properties", pc);
		
		context.addRoutes(new RouteBuilder() {
			
			public void configure() {				         				
				Processor buildAmazonURL = new BuildAmazonURL();
				
// Route to test the amazonAPI Route
// reads artist names from in/artists.txt, splits lines and calls amazonAPI route with artist name in body				
				from("file:in?fileName=artists.txt&noop=true")
                  .split(body().tokenize("\n"))
                  .to("direct:amazonAPI");
				
// amazonAPI Route
                from("direct:amazonAPI")
                  .process(buildAmazonURL)
                  //.setHeader(Exchange.HTTP_METHOD, constant("GET"))
                  .setHeader(Exchange.HTTP_URI, simple("${body}"))
       	     	  .to("http4://dummyhost?throwExceptionOnFailure=false") 
			      .to("file:out?fileName=amazon_${date:now:yyyyMMdd_HHmmssSSS}.xml");

// SWOBI: Camel Exception Handling (doTry - doCatch)				
//				  .doTry()
//			         .to("http4://" + properties.getProperty("amazon.endpoint") 
// 					                + "/onca/xml?" + signedParams)
//			         .to("file:out?fileName=amazon.xml");
//			      .doCatch(HttpOperationFailedException.class)
//			         .to("file:out?fileName=error.xml&allowNullBody=True")
//			      .end(); 				
			         
			}
		});
			
		context.start();
		Thread.sleep(10000);
		context.stop();
	    
	} 
	
}