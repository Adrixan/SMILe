package mongodb;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Header;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;

public class HeaderSplitterBean {

	public List<Message> splitHeader(@Header(value = "artists") String header) {

		List<Message> answer = new ArrayList<Message>();
		String[] parts = header.split(",");
		for (String part : parts) {
			DefaultMessage message = new DefaultMessage();
			message.setHeader("artist", part.trim());
			answer.add(message);
		}
		return answer;
	}	
}
