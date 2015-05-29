import java.util.Properties;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;


@Component
public class SimpleRouteBuilder extends RouteBuilder {
    
    
    @Override
    public void configure() throws Exception {
        // TODO Auto-generated method stub
    	
//    	LastFMService lfm = new LastFMService();
    	//lfm.getUpcomingEvents("Ellie Goulding", "St. Pölten");
 //   	lfm.getUpcomingEvents("Lady Gaga", "Vienna");
    	
 //   	lfm.getUpcomingEventsInGeo("Ellie Goulding", "St. Pölten");
    	
//    	LastFMService lfm1 = new LastFMService();
//    	lfm1.getUpcomingEvents("Simple Plan", null);
    	
    	Properties p = Main.properties;
            	
    	/**
		 **--------------------
		 **SubscribeRoute
		 **--------------------
		 */
    	

        from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
        .process(new EmailToSqlProcessor()).split(new SqlSplitExpression()).wireTap("file:out").end()
        .to("jdbc:accounts").end();
        
        // Helfercode
    	from("timer://foo?repeatCount=1")	// 
        .to("direct:LastFM");
    	
//    	from("file:fm-in?fileName=artists.txt&noop=true")
//        .split(body().tokenize("\n"))
//        .to("direct:LastFM");

    	log.debug("------------------------  KEY ----------------------------"+p.getProperty("lastFM.apiKey"));
    	
//    	from("direct:LastFM")
//    	.process(new LastFMProcessor("Ellie Goulding", "St. Pölten", ""+p.getProperty("lastFM.apiKey"))).split(new LastFMSplitExpression())
//    	.to("file:fm-out");
    	//.to("file:fm-out?fileName=lastFM_${date:now:yyyyMMdd_HHmmssSSS}.txt");
    	
    	from("direct:LastFM")
    	.process(new LastFMProcessor("Ellie Goulding", "Vienna", ""+p.getProperty("lastFM.apiKey"))).split(new LastFMSplitExpression())
    	.to("file:fm-out");
         
     }  
}