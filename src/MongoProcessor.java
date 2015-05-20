import java.awt.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class MongoProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Message out = arg0.getIn().copy();
		

    	String[] insert = new String[2];
    	insert[0] = "name";
    	insert[1] = "Hello World";
    	


    	 DBObject obj1 = new BasicDBObject();
    	 obj1.put( "foo1", "bar1" );
    	 
    	 DBObject obj2 = new BasicDBObject();
    	 obj2.put( "foo2", "bar2" );
    	   	 

     	ArrayList<DBObject> arlist = new ArrayList<DBObject>();
     	arlist.add(obj1);
     	arlist.add(obj2);    	 

     	System.out.println(arlist);
     	
		out.setBody(arlist);
		arg0.setOut(out);
	}

}
