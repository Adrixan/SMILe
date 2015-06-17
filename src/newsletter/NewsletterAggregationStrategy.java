package newsletter;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import pojo.ArtistPojo;

// AggregationsStrategy um Artist-messages (mehrere) von Subscriber zu mergen -> zu einer einzigen Nachricht
public class NewsletterAggregationStrategy implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

		final ArrayList<ArtistPojo> subscriberArtistList;
		ArtistPojo artist = null;

		if (oldExchange == null) {
			subscriberArtistList = new ArrayList<ArtistPojo>();
		} else {
			subscriberArtistList = (ArrayList<ArtistPojo>) oldExchange.getIn()
					.getBody();
		}

		artist = (ArtistPojo) newExchange.getIn().getBody();
		if (artist != null) {
			subscriberArtistList.add(artist);
		}
			
		newExchange.getIn().setBody(subscriberArtistList);
		return newExchange;
	}
}
