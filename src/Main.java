import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.commons.dbcp2.BasicDataSource;

public class Main {

	public static Properties properties;


	private static void testsimplejms(){
		try
		{   
			Class.forName("com.mysql.jdbc.Driver");

			SimpleRegistry registry = new SimpleRegistry();
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName("com.mysql.jdbc.Driver");
			ds.setUrl("jdbc:mysql://" + properties.getProperty("rdbm.host") + "/" + properties.getProperty("rdbm.database") +"?"
					+ "user=" + properties.getProperty("rdbm.user") +"&password=" + properties.getProperty("rdbm.password"));
			registry.put("accounts", ds);

			CamelContext context = new DefaultCamelContext(registry);


			context.setTracing(true);
			context.addRoutes(new SimpleRouteBuilder());
//			context.addRoutes(new SimpleRouteBuilder2());
			
			//context.addRoutes(new SimpleRouteBuilder());

			context.start();
			Thread.sleep(10000);
			context.stop();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

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

		testsimplejms();
	}

}
