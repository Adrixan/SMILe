import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;


public class EmailUnsubscribeProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Map<String,Object> headers = arg0.getIn().getHeaders();
		Message out = arg0.getIn().copy();

		String email = headers.get("From").toString();
		
		Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(email);
		m.find();
		email = m.group();
		
		String body ="Delete from subscriber where email='" + email +"';\n";

		out.setBody(body);
		arg0.setOut(out);
	}

}
