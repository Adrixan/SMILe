package lastFM;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Event;
import de.umass.lastfm.Geo;
import de.umass.lastfm.PaginatedResult;

// Grabber LastFM
public class LastFMService {

	private Log log = LogFactory.getLog(LastFMService.class);

	private String API_KEY;
	private String artist;


	public LastFMService(){

	}
	public LastFMService(String artist, String apiKey){
		this.artist = artist; 
		this.API_KEY = apiKey;
	}

	/*
	 * artist: Returns the events of this Artist , country: returns only Events
	 * of this country (null -> every Country)
	 */

	public List<pojo.Event> getUpcomingEvents(String artist, String city) { //city


		this.artist = artist;

		log.info("Artist fuer Last.fm " +this.artist);
		Collection<Event> elist = Artist.getEvents(this.artist, this.API_KEY).getPageResults();

		List<pojo.Event> retlist = new ArrayList<pojo.Event>();

		Iterator<Event> eventList = elist.iterator();

		while(eventList.hasNext()) {
			Event e = eventList.next();

			// country kann nicht null sein - muss immer country eingegeben werden
			if (e.getVenue().getCity()!=null && e.getVenue().getCity().equals(city)) {		//city
				pojo.Event event = new pojo.Event();
				event.setId(e.getId());
				event.setArtist(this.artist);
				event.setTitle(e.getTitle());
				event.setCountry(e.getVenue().getCountry());
				event.setCity(e.getVenue().getCity());
				event.setWebsite(e.getWebsite());

				event.setDate(new Date(e.getStartDate().getTime()));
				retlist.add(event);

				/*log.info("Event fuer "+this.artist+ ": "+event.toString()+" und Location = "+event.getCountry()+
						"\nWebseite: "+event.getWebsite());*/

			}

		}
		Collections.sort(retlist);
		return retlist;

	}

	/*
	 * artist: Returns the events of this Artist , location: city
	 * find all events for an artist with a distance of 65 km of the provieded location
	 * searching: 100 PageResults
	 */
	public List<pojo.Event> getUpcomingEventsInGeo(String artist, String location) {

		PaginatedResult<Event> geoList; 
		Iterator<Event> eventList ;
		List<pojo.Event> retlist = new ArrayList<pojo.Event>();; 
		for(int i=1; i<100; i++){
			// Geo.getEvents(location, distance, page, apiKey)
			geoList = Geo.getEvents(location, "65", i, this.API_KEY);
			eventList = geoList.getPageResults().iterator();

			while(eventList.hasNext()) {
				Event e = eventList.next();

				Collection<String> artists = e.getArtists();

				Iterator<String> artistList = artists.iterator();

				while(artistList.hasNext()) {

					if(artistList.next().equals(artist)){ 
						pojo.Event event = new pojo.Event();

						event.setArtist(this.artist);
						event.setId(e.getId());
						event.setTitle(e.getTitle());
						event.setCountry(e.getVenue().getCountry());
						event.setCity(e.getVenue().getCity());
						event.setWebsite(e.getWebsite());

						event.setDate(new Date(e.getStartDate().getTime()));

						retlist.add(event);

					}
				}

			}
		}
		Collections.sort(retlist);

		return retlist;

	}

}
