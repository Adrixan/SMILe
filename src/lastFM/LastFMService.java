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
// import com.sepm08.playITsmart.dao.IArtistDAO



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
		
		System.out.println("------------------------------------Artist " +this.artist);
		Collection<Event> elist = Artist.getEvents(this.artist, this.API_KEY).getPageResults();

		System.out.println("Null oder nicht fuer Artist " +this.artist+ ": " +elist.size());

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
				log.debug("!!Datum: " + event.getDate());

				retlist.add(event);

				System.out.println("Event fuer "+this.artist+ ": "+event.toString()+" und Location = "+event.getCountry());

				// TODO: setCountry und setCity umändern!!!

			}

		}

		// = Artist.getInfo(artistOrMbid, locale, apiKey)
		//(List<Event>)
		//Collection<net.roarsoftware.lastfm.Event> eventGeo = Geo.getAllEvents("Wien", 50, API_KEY);






		//		for (Event e : elist) {
		//			if (e.getVenue().getCountry().equals(country) || country == null) {
		//				sepm08.playITsmart.domain.Event event = new sepm08.playITsmart.domain.Event();
		//
		//				event.setId(e.getId());
		//				event.setTitle(e.getTitle());
		//				event.setCountry(e.getVenue().getCity() + ", " + e.getVenue().getCountry());
		//				event.setWebsite(e.getWebsite());
		//
		//				event.setDate(new Date(e.getStartDate().getTime()));
		//				log.debug("!!Datum: " + event.getDate());
		//				retlist.add(event);
		//			}
		//		}
		//
		Collections.sort(retlist);
		return retlist;

	}

	public List<pojo.Event> getUpcomingEventsInGeo(String artist, String location) {


		//	Collection<net.roarsoftware.lastfm.Event> elist =  Artist.getEvents(artist, API_KEY);
		//	Collection<net.roarsoftware.lastfm.Event> geoList = Geo.getAllEvents(location, "20", API_KEY);


		PaginatedResult<Event> geoList; //= Geo.getEvents(location, "65", this.API_KEY);
		Iterator<Event> eventList ;//= geoList.getPageResults().iterator();
		List<pojo.Event> retlist = new ArrayList<pojo.Event>();; 
		for(int i=1; i<100; i++){
			// Geo.getEvents(location, distance, page, apiKey)
			geoList = Geo.getEvents(location, "65", i, this.API_KEY);
			eventList = geoList.getPageResults().iterator();
			
		//	retlist

			//	Iterator<Event> eventList = list.iterator();

				while(eventList.hasNext()) {
					Event e = eventList.next();

					//if (e.getVenue().getCity()!=null && e.getVenue().getCity().equals(location)) {		//city
		// TODo: Vgl mit oberen Methode!!!			
					Collection<String> artists = e.getArtists();
					
					Iterator<String> artistList = artists.iterator();

					while(artistList.hasNext()) {
						
						if(artistList.next().equals(artist)){ //&& (e.getVenue().getCountry().equals(location)||e.getVenue().getCountry().equals(location) )){
							pojo.Event event = new pojo.Event();
							event.setArtist(this.artist);
							event.setId(e.getId());
							event.setTitle(e.getTitle());
							event.setCountry(e.getVenue().getCountry());
							event.setCity(e.getVenue().getCity());
							event.setWebsite(e.getWebsite());

							event.setDate(new Date(e.getStartDate().getTime()));

							retlist.add(event);

							System.out.println("----------GeoEvent fuer "+artist+ ": "+event.toString()+" und Location = "+event.getCountry());

						}
					}
					
					//artists.iterator().next().equals(artist);
						
						

				//	}

				}
		}
		Collections.sort(retlist);
		System.out.println("Groesse der Liste: "+retlist.size());
		
		return retlist;
		
//		PaginatedResult<Event> geoList = Geo.getEvents(location, "50",2, this.API_KEY);
		//		PaginatedResult<net.roarsoftware.lastfm.Event> geoList =  Geo.getEvents(48.204722, 15.626667, 10, API_KEY);
		//		PaginatedResult<Event> geoListN =  Geo.getEvents(15.626667, 48.204722, 100, API_KEY);
		//48.204722,15.626667

		//Collection<Event> list = geoList.getPageResults();

		
			//Iterator<Event> eventList = geoList.getPageResults().iterator();

		
		//		mArtistIterator = mArtistResults.getPageResults().iterator();
//        while (mArtistIterator.hasNext()) {
//            mArtistImageURL = mArtistIterator.next().getImageURL(
//                    ImageSize.ORIGINAL);
//        }

	//	System.out.println("-----------Geoevents ------------ " +artist+ ": " +list.size());

		

		// = Artist.getInfo(artistOrMbid, locale, apiKey)
		//(List<Event>)
		//Collection<net.roarsoftware.lastfm.Event> eventGeo = Geo.getAllEvents("Wien", 50, API_KEY);






		//		for (Event e : elist) {
		//			if (e.getVenue().getCountry().equals(country) || country == null) {
		//				sepm08.playITsmart.domain.Event event = new sepm08.playITsmart.domain.Event();
		//
		//				event.setId(e.getId());
		//				event.setTitle(e.getTitle());
		//				event.setCountry(e.getVenue().getCity() + ", " + e.getVenue().getCountry());
		//				event.setWebsite(e.getWebsite());
		//
		//				event.setDate(new Date(e.getStartDate().getTime()));
		//				log.debug("!!Datum: " + event.getDate());
		//				retlist.add(event);
		//			}
		//		}
		//
		

	}

	//	public String getAlbumCover(String artist, String album) {
	//		if (artist == null || album == null)
	//			throw new NullPointerException();
	//
	//		Album albi = Album.getInfo(artist, album, API_KEY);
	//
	//		if (albi != null) {
	//			return albi.getImageURL(ImageSize.LARGE);
	//		}
	//		return null;
	//	}

}
