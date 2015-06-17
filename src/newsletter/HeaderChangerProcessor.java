package newsletter;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

/* HeaderChanger um artist und location fuer message zu aendern bzw. hinzuzufuegen
 * (Format -> artist=X, location=Y,Z)
 * wichtig fuer MongoDB und last.fm-Objekte 
 */
public class HeaderChangerProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message out = exchange.getIn();
		
		String newBody="";
		
		HashMap<String, Object> oldBody = (HashMap<String, Object>) out.getBody();
		
		out.setHeader("artist", oldBody.get("artist"));
		out.setHeader("location", oldBody.get("location"));
		
		//System.out.println("Header in HeaderChanger: "+out.getHeaders().toString());

	}

}
