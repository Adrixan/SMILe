import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.Processor; 
// import org.apache.camel.component.http4.HttpOperationFailedException;
import org.apache.commons.dbcp2.BasicDataSource;

public class AmazonAPIandDB {
	public static Properties properties;

	public static void main(String[] args) throws Exception {

		try {
			properties = new Properties();
			properties.load(new FileInputStream("smile.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Class.forName("com.mysql.jdbc.Driver");

		SimpleRegistry registry = new SimpleRegistry();

		CamelContext context = new DefaultCamelContext(registry);

		//		CamelContext context = new DefaultCamelContext();

		PropertiesComponent pc = new PropertiesComponent();
		pc.setLocation("file:smile.properties");

		context.setTracing(true);

		context.addComponent("properties", pc);

		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		//		ds.setUrl("jdbc:mysql://{{rdbm.host}}/{{rdbm.database}}?user={{rdbm.user}}&password={{rdbm.password}}");
		ds.setUrl("jdbc:mysql://" + properties.getProperty("rdbm.host") + "/" + properties.getProperty("rdbm.database") +"?"
				+ "user=" + properties.getProperty("rdbm.user") +"&password=" + properties.getProperty("rdbm.password"));

		registry.put("accounts", ds);

		context.addRoutes(new RouteBuilder() {

			public void configure() {				         				
				Processor buildAmazonURL = new BuildAmazonURL();

				// Route to test the amazonAPI Route
				// reads artist names from in/artists.txt, splits lines and calls amazonAPI route with artist name in body				
				//				from("file:in?fileName=artists.txt&noop=true")
				//                  .split(body().tokenize("\n"))
				//                  .to("direct:amazonAPI");


				from("timer://foo?repeatCount=1&delay=0")
				.setBody(simple("select distinct(artist) from subscriptions"))
				.to("jdbc:accounts?outputType=StreamList")
				.split(body()).streaming()
				.setBody(body().regexReplaceAll("\\{artist= (.*)(\\r)?\\}", "$1"))
				.to("direct:amazonAPI");
				//                  .to("file:out?fileName=sqlresult.txt");

				// amazonAPI Route
				from("direct:amazonAPI")
				.process(buildAmazonURL)
				//.setHeader(Exchange.HTTP_METHOD, constant("GET"))
				.setHeader(Exchange.HTTP_URI, simple("${body}"))
				.to("http4://dummyhost?throwExceptionOnFailure=false") 
				.to("file:out?fileName=amazon_${date:now:yyyyMMdd_HHmmssSSS}.xml");

				// SWOBI: Camel Exception Handling (doTry - doCatch)				
				//				  .doTry()
				//			         .to("http4://" + properties.getProperty("amazon.endpoint") 
				// 					                + "/onca/xml?" + signedParams)
				//			         .to("file:out?fileName=amazon.xml");
				//			      .doCatch(HttpOperationFailedException.class)
				//			         .to("file:out?fileName=error.xml&allowNullBody=True")
				//			      .end(); 				

			}
		});

		context.start();
		Thread.sleep(10000);
		context.stop();

	} 

}