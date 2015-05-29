import java.awt.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class MongoProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();
    	


    	 DBObject obj1 = new BasicDBObject();
    	 obj1.put( "foo1", "bar1" );
    	 
    	 DBObject obj2 = new BasicDBObject();
    	 obj2.put( "foo2", obj1 );
    	   	 

     	ArrayList<DBObject> arlist = new ArrayList<DBObject>();
     	arlist.add(obj1);
     	arlist.add(obj2);    	 

//     	System.out.println(obj2);
     	
	   	ObjectId id= new ObjectId("555c7aeb20be320f90a739e4");   
        BSONObject bson = new BasicBSONObject();
        bson.put("_id", id);
        
	   	 DBObject objid = new BasicDBObject();
	   	 objid.put( "_id", id );        
     	
		out.setBody(objid);
		arg0.setOut(out);
	}

}
