package youtube;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Processor; 
// import org.apache.camel.component.http4.HttpOperationFailedException;

public class YoutubeAPITest {

	public static Properties properties;

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();

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
				Processor channelProcessor = new YoutubeChannelProcessor();
				
				errorHandler(deadLetterChannel("direct:myDLC"));

				// Route to test the youtubeAPI
				// reads artist names from in/artists.txt, splits lines and calls youtubeAPI route with artist name in body				
				from("file:in?fileName=artists.txt&noop=true")
				.split(body().tokenize("\n"))
				.to("direct:youtubeAPI");

				// youtubeAPI Route
				from("direct:youtubeAPI")
				.process(channelProcessor)
				.process(new HashProcessor())
				.to("file:out?fileName=youtube_${date:now:yyyyMMdd_HHmmssSSS}.txt");
				
				//DeadLetterChannel
				from("direct:myDLC")
				.process(new DLCProcessor());

			}
		});

		context.start();
		Thread.sleep(10000);
		context.stop();

	} 
	
	private static class HashProcessor implements Processor {

		@Override
		public void process(Exchange exchange) throws Exception {
			Message m = exchange.getIn();
			
			StringBuilder builder = new StringBuilder();
			HashMap<String,String> playlistinfo = (HashMap<String, String>) m.getBody();
			for (Entry<String, String> e : playlistinfo.entrySet()) {
				builder.append(e.getKey() + " +++++++++ " + e.getValue()+"\n");
			}
			m.setBody(builder.toString());
			exchange.setIn(m);
		}
		
	}
	
	private static class DLCProcessor implements Processor {

		@Override
		public void process(Exchange exchange) throws Exception {
			Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
			System.out.println("DLC Excpetion caught. Has Message: "+e.getMessage());
			System.out.println("DLC Exchange Message Body: "+ exchange.getIn().getBody());
			
		}
		
	}

}