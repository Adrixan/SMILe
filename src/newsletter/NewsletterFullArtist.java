package newsletter;

import java.util.HashMap;
import java.util.Properties;

import main.Launcher;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsletterFullArtist implements AggregationStrategy {

	Properties p = Launcher.properties;
	private static final Logger logger = LoggerFactory.getLogger(NewsletterFullArtist.class);

    public Exchange aggregate(Exchange OldExchange, Exchange NewExchange) {
        
		String sFirstTitel = "";
		String sSecTitel = "";
		String firstMessage = "";
		String secMessage = "";
		String sNewsletter = "";

		if (OldExchange != null) {
			firstMessage = OldExchange.getIn().getBody(String.class);
			Log.info("firstMessage :" + firstMessage.toString());
		}
		secMessage = NewExchange.getIn().getBody(String.class);
		Log.info("secondMessage :" + secMessage.toString());

		sNewsletter = firstMessage + '\n' + secMessage;
		Log.info("BAM BAM : Newsletter" + sNewsletter.toString());

		NewExchange.getIn().setBody(sNewsletter);
		NewExchange.getIn().setHeader("artist",
				NewExchange.getIn().getHeader("artist"));

		Log.info("NewExhchange Header: "
				+ NewExchange.getIn().getHeader("artist"));

		return NewExchange;
    	
//		final HashMap<String, HashMap> fullArtist;
//		HashMap grabberItem = new HashMap();
//
//		if (oldExchange == null) {
//			fullArtist = new HashMap<String, HashMap>();
//		} else {
//			fullArtist = (HashMap<String, HashMap>) oldExchange.getIn()
//					.getBody();
//		}
//
//		grabberItem = (HashMap) newExchange.getIn().getBody();
//
//		fullArtist.put(grabberItem.get("_id").toString(), grabberItem);
//		newExchange.getIn().setBody(fullArtist);
//		return newExchange;
    }
}
