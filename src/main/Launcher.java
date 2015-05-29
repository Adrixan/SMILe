package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Main;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.commons.dbcp2.BasicDataSource;

public class Launcher {

	public static Properties properties;
	private Main main;
	
    public void boot() throws Exception {
        // create a Main instance
        main = new Main();
        // enable hangup support so you can press ctrl + c to terminate the JVM
        main.enableHangupSupport();
        // bind MyBean into the registery
        BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://" + properties.getProperty("rdbm.host") + "/" + properties.getProperty("rdbm.database") +"?"
				+ "user=" + properties.getProperty("rdbm.user") +"&password=" + properties.getProperty("rdbm.password"));
		main.bind("accounts", ds);
        // add routes
        main.addRouteBuilder(new SimpleRouteBuilder());
 
        // run until you terminate the JVM
        System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
        main.run();
    }


	private static void testsimplejms(){
		try
		{   
			Class.forName("com.mysql.jdbc.Driver");
			Launcher l = new Launcher();
			l.boot();

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
