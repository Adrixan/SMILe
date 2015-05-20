package youtubeAPI;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Processor; 
// import org.apache.camel.component.http4.HttpOperationFailedException;

public class YoutubeAPI {
	
	public static void main(String[] args) throws Exception {
    
		CamelContext context = new DefaultCamelContext();
		
		final Properties properties;
		
		try {
			properties = new Properties();
			properties.load(new FileInputStream("smile.properties"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		context.setTracing(true);
		
		context.addRoutes(new RouteBuilder() {
			
			public void configure() {				         				
				Processor channelProcessor = new YoutubeChannelProcessor(properties);
				
// Route to test the amazonAPI Route
// reads artist names from in/artists.txt, splits lines and calls amazonAPI route with artist name in body				
				from("file:in?fileName=artists.txt&noop=true")
                  .split(body().tokenize("\n"))
                  .to("direct:youtubeAPI");
				
// amazonAPI Route
                from("direct:youtubeAPI")
                  .process(channelProcessor)
			      .to("file:out?fileName=youtube_${date:now:yyyyMMdd_HHmmssSSS}.xml");

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