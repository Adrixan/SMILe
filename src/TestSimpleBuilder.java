import org.apache.camel.builder.RouteBuilder;

public class TestSimpleBuilder extends RouteBuilder {
    
    
    @Override
    public void configure() throws Exception {
        // TODO Auto-generated method stub
        
        from("timer://foo?repeatCount=1")
            .setHeader("Header1", constant("Header1"))
            .setBody().simple("Test body")
        .to("file:incomingA");
                
          
        
        from("file:incomingA")
            .to("file:out");
         
     }  
}