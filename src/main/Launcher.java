package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.camel.Main;
import org.apache.camel.component.metrics.routepolicy.MetricsRegistryService;
import org.apache.commons.dbcp2.BasicDataSource;

import com.codahale.metrics.MetricRegistry;

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
		// Registry necessary so metrics are properly kept in memory
		MetricRegistry metricRegistry = new com.codahale.metrics.MetricRegistry();
		main.bind("metricRegistry", metricRegistry);
        // add routes
		SimpleRouteBuilder r = new SimpleRouteBuilder();
        main.addRouteBuilder(r);
 
        // run until you terminate the JVM
        System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
        main.run();
        
//		for(int sleep = 0; sleep < 20 ; sleep++){
//			
//			MetricsRegistryService regSvc = r.getContext().hasService(MetricsRegistryService.class);
//			if(regSvc != null) {
//				// Dump the statistics to the log in JSON format.
//				regSvc.setPrettyPrint(true);
//				System.out.println(regSvc.dumpStatisticsAsJson());
//
//				// Do it again, retrieving the map of counters from the MetricRegistry object.
//				//            LOG.info("----------");
//				//            MetricRegistry reg = regSvc.getMetricsRegistry();
//				//            SortedMap<String, Counter> counters = reg.getCounters();
//				//            LOG.info("Keys found {{}]", counters.keySet().size());
//				//            for(String key : counters.keySet()) {
//				//                LOG.info("--- key [{}] - value [{}]", key, counters.get("key").getCount());
//				//            }
//			} else {
//				System.out.println("Couldn't find MetricsRegisteryService instance");
//			}
//			Thread.sleep(10000);
//		}
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
