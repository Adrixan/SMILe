import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class Main {


	private static void testsimplejms(){
		try
		{       
			CamelContext context = new DefaultCamelContext();


			context.addRoutes(new TestSimpleBuilder());

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
		testsimplejms();
	}

}
