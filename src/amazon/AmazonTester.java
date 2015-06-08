package amazon;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.processor.aggregate.AggregationStrategy;
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
		// context.setTracing(true);

		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://" + properties.getProperty("rdbm.host") + "/" + properties.getProperty("rdbm.database") +"?"
				+ "user=" + properties.getProperty("rdbm.user") +"&password=" + properties.getProperty("rdbm.password"));
		registry.put("accounts", ds);

		context.addRoutes(new RouteBuilder() {

			public void configure() {				         				

				from("timer://foo?repeatCount=1&delay=0")
				.setBody(simple("select distinct(artist) from subscriptions"))
				.to("jdbc:accounts?outputType=StreamList")
				.split(body()).streaming()
				.setBody(body().regexReplaceAll("\\{artist= (.*)(\\r)?\\}", "$1"))
				.setHeader("artist").body()
				.to("direct:amazon");
				// .to("file:out?fileName=sqlresult.txt");

				// Amazon Route
				from("direct:amazon")
				.process(new AmazonRequestCreator())
				.recipientList(header("amazonRequestURL")).ignoreInvalidEndpoints()
				.split().tokenizeXML("Item").streaming()
//				.to("file:out?fileName=amazon_${date:now:yyyyMMdd_HHmmssSSS}.xml")
				.setHeader("amazon_uid").xpath("/Item/ASIN/text()", String. class)
				.setHeader("amazon_title").xpath("/Item/ItemAttributes/Title/text()", String. class)
                .setHeader("amazon_pageurl").xpath("/Item/DetailPageURL/text()", String. class)
                .setHeader("amazon_imageurl").xpath("/Item/LargeImage/URL/text()", String. class)
                .setHeader("amazon_price").xpath("/Item/OfferSummary/LowestNewPrice[1]/FormattedPrice/text()", String. class)
//                .filter(header("amazon_price").isNotEqualTo(""))
//                .setBody(simple("${in.header.amazon_title}\n${in.header.amazon_pageurl}\n" + 
//                                "${in.header.amazon_imageurl}\n${in.header.amazon_price}\n"))
                .aggregate(header("artist"), new AmazonAggregationStrategy()).completionTimeout(5000)
//               .completionSize(3)
                .process(new AmazonMongoTester());
//				.to("file:out?fileName=amazon_${date:now:yyyyMMdd_HHmmssSSS}.txt");
 				
			}
		});

		context.start();
		Thread.sleep(10000);
		context.stop();
	}

} 
	
	

