import java.util.Properties;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class SimpleRouteBuilder extends RouteBuilder {
    
    
    @Override
    public void configure() throws Exception {
        // TODO Auto-generated method stub
    	Properties p = Main.properties;
        
        from("imaps://" + p.getProperty("email.host") + "?username=" + p.getProperty("email.user") +"&password=" + p.getProperty("email.password"))
        .process(new EmailToSqlProcessor()).split(new SqlSplitExpression()).wireTap("file:out").end()
        .to("jdbc:accounts").end();
        
	   	 DBObject insertObj = new BasicDBObject();
	   	 insertObj.put("test", "this");        
        
        from("timer://runOnce?repeatCount=1&delay=5000")
        .to("direct:findAll");
        
        from("direct:insert")
//        .process(new MongoProcessor())
        .setBody()
        .constant(insertObj)
        .to("mongodb:mongoBean?database=test&collection=test&operation=insert");
        
        from("direct:remove")
        .process(new MongoProcessor())
        .to("mongodb:mongoBean?database=test&collection=test&operation=remove");
        

        
	   	 DBObject obj2 = new BasicDBObject();
	   	 obj2.put("test", "this");
	   	 
	   	 DBObject obj3 = new BasicDBObject();
	   	 obj3.put("_id", obj2);
	   	ObjectId id= new ObjectId("555c7aeb20be320f90a739e4");   
	        BSONObject bson = new BasicBSONObject();
	        bson.put("_id", id);	   	 
        
        from("direct:findById")
//        .setBody()
//        .constant(bson)
        .process(new MongoProcessor())
        .to("mongodb:mongoBean?database=test&collection=test&operation=findById")
        .to("log:mongo:findById?level=INFO")
        .wireTap("file:out");
        
       from("direct:findAll")
      .setBody()
//      .constant(bson)
      .constant(obj2)
//      .process(new MongoProcessor())
      .to("mongodb:mongoBean?database=test&collection=test&operation=findAll")
      .to("log:mongo:findById?level=INFO")
      .wireTap("file:out");        
         
     }  
}