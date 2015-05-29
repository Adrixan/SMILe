import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.ExpressionAdapter;


public class LastFMSplitExpression extends ExpressionAdapter {

	@Override
	public Object evaluate(Exchange arg0) {
		ArrayList<Message> col = new ArrayList<Message>();
		
		for(String s : arg0.getIn().getBody().toString().split("\n"))
		{
			Message m = arg0.getIn().copy();
			m.setBody(s);
			col.add(m);
		}
		
		return col;
	}

}
