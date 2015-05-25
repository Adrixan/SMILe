import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;


public class EmailToSqlProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Map<String,Object> headers = arg0.getIn().getHeaders();
		for(String s : headers.keySet())
			System.out.println(s + " " + headers.get(s).toString() + "\n");
		Message out = arg0.getIn().copy();
		
		out.setHeader("Action", headers.get("Subject"));
		out.setBody("");
		
		String email = headers.get("From").toString();
		
		Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(email);
		m.find();
		email = m.group();
		
		String body = "";
		
		body +="Insert into subscriber (email) values ('" + email +"');\n";
		
		for(String s : arg0.getIn().getBody().toString().split("\n"))
		{
			if(s.startsWith("Location:"))
				body +="Insert into locations (email, location) values ('" + email +"','" + s.split(":")[1] + "');\n";
			if(s.startsWith("Artist:"))
				body +="Insert into subscriptions (email, artist) values ('" + email +"','" + s.split(":")[1] + "');\n";
		}

		out.setBody(body);
		arg0.setOut(out);
	}

}
