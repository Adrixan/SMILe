import java.util.Properties;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;


@Component
public class SimpleRouteBuilder2 extends RouteBuilder {
    
    
    @Override
    public void configure() throws Exception {
        // TODO Auto-generated method stub
    	
    	
    	LastFMService lfm1 = new LastFMService();
//    	lfm1.getUpcomingEvents("Ellie Goulding", "Austria");
    	
    	Properties p = Main.properties;
            	
    	/**
		 **--------------------
		 **SubscribeRoute
		 **--------------------
		 */

        from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
        .process(new EmailToSqlProcessor()).split(new SqlSplitExpression()).wireTap("file:out").end()
        .to("jdbc:accounts").end();
         
     }  
}