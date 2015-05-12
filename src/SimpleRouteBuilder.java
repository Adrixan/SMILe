import java.util.Properties;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;

public class SimpleRouteBuilder extends RouteBuilder {
    
    
    @Override
    public void configure() throws Exception {
        // TODO Auto-generated method stub
    	Properties p = Main.properties;
        
        from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
        .process(new EmailToSqlProcessor()).split(new SqlSplitExpression()).wireTap("file:out").end()
        .to("jdbc:accounts").end();
         
     }  
}