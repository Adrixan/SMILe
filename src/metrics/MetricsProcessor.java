package metrics;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.metrics.routepolicy.MetricsRegistryService;

public class MetricsProcessor implements Processor {

	@Override
	public void process(Exchange arg0) throws Exception {
		Message m = arg0.getIn();

		MetricsRegistryService metricRegistry = (MetricsRegistryService) arg0.getContext().hasService(MetricsRegistryService.class);

		metricRegistry.setPrettyPrint(true);

		m.setBody(metricRegistry.dumpStatisticsAsJson());

		arg0.setOut(m);
	}

}
