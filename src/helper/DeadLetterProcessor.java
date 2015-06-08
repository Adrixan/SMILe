package helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class DeadLetterProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		Message m = exchange.getIn();
		String body = m.getBody().toString();
		m.setBody("//////////////////////////////\r\n"+
				  "///// Exception Message\r\n"+
				  "//////////////////////////////\r\n"+
				  e.getMessage()+"\r\n\r\n"+
				  
				  "//////////////////////////////\r\n"+
				  "///// Message Body\r\n"+
				  "//////////////////////////////\r\n"+
				  body+"\r\n\r\n"+
				  
				  "//////////////////////////////\r\n"+
				  "///// Exception Stacktrace\r\n"+
				  "//////////////////////////////\r\n"+
				  writer.toString()
				  );
		exchange.setIn(m);
	}

}
