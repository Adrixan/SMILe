package amazon;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.Processor; 
import org.apache.commons.dbcp2.BasicDataSource;

public class AmazonTester {
	public static Properties properties;

	public static void main(String[] args) throws Exception {

		try {
			properties = new Properties();
			properties.load(new FileInputStream("smile.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SimpleRegistry registry = new SimpleRegistry();
		CamelContext context = new DefaultCamelContext(registry);
		// CamelContext context = new DefaultCamelContext();
		context.setTracing(true);

		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://" + properties.getProperty("rdbm.host") + "/" + properties.getProperty("rdbm.database") +"?"
				+ "user=" + properties.getProperty("rdbm.user") +"&password=" + properties.getProperty("rdbm.password"));
		registry.put("accounts", ds);

		context.addRoutes(new RouteBuilder() {

			public void configure() {				         				
				Processor AmazonProcessor = new AmazonProcessor();

				from("timer://foo?repeatCount=1&delay=0")
				.setBody(simple("select distinct(artist) from subscriptions"))
				.to("jdbc:accounts?outputType=StreamList")
				.split(body()).streaming()
				.setBody(body().regexReplaceAll("\\{artist= (.*)(\\r)?\\}", "$1"))
				.to("direct:amazon");
				// .to("file:out?fileName=sqlresult.txt");

				// Amazon Route
				from("direct:amazon")
				.process(AmazonProcessor)
				.setHeader(Exchange.HTTP_URI, simple("${body}"))
				.to("http4://dummyhost?throwExceptionOnFailure=false") 
				.to("file:out?fileName=amazon_${date:now:yyyyMMdd_HHmmssSSS}.xml");
 				
			}
		});

		context.start();
		Thread.sleep(10000);
		context.stop();

	} 

}