package youtubeAPI;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelContentDetails.RelatedPlaylists;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

public class KeywordSample {
	
	private static YouTube youtube;
	private static String key;
	private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
	
	/** Global instance of the HTTP transport. */
	private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	  
	/** Global instance of the JSON factory. */
	private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
	
	private static final HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
        public void initialize(HttpRequest request) throws IOException {
        }
    };

	
	public static String searchChannelTest(String channelKeyword, Properties properties) {
		
		String retString = "";
		key = properties.getProperty("youtube.key");
		
		try{
		// This object is used to make YouTube Data API requests. The last
        // argument is required, but since we don't need anything
        // initialized when the HttpRequest is initialized, we override
        // the interface and provide a no-op function.
        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, httpRequestInitializer)
        .setApplicationName("SMILe").build();


        // Define the API request for retrieving search results.
        YouTube.Search.List search = youtube.search().list("id,snippet");

        // Set your developer key from the Google Developers Console for
        // non-authenticated requests. See:
        // https://console.developers.google.com/
        search.setKey(key);
        search.setQ(channelKeyword);
        
        // Restrict the search results to only include videos. See:
        // https://developers.google.com/youtube/v3/docs/search/list#type
        search.setType("channel");

        // To increase efficiency, only retrieve the fields that the
        // application uses.
        //search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
        
        // Call the API and print results.
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();
        if (searchResultList != null) {
            retString = prettyPrint(searchResultList.iterator(), channelKeyword);
        }

		} catch(Exception e){
			
		}

		return retString;
	}
	
	/*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    private static String prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

    	StringBuilder builder = new StringBuilder();
    	builder.append("Artist Name: " + query+"\n");
//        System.out.println("\n=============================================================");
//        System.out.println(
//                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
//        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }
        
        Channel bestMatch = null;

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleChannel = iteratorSearchResults.next();
            ResourceId rId = singleChannel.getId();
            

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#channel")) {
                Thumbnail thumbnail = singleChannel.getSnippet().getThumbnails().getDefault();

                                
//                System.out.println(" Channel URL: "+ "http://www.youtube.com/channel/" + rId.getChannelId());
//                System.out.println(" Title: " + singleChannel.getSnippet().getTitle());
//                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                
                try {
    				YouTube.Channels.List channelSearch = youtube.channels().list("contentDetails,statistics,snippet");
    				channelSearch.setKey(key);
    				channelSearch.setId(rId.getChannelId());
    				ChannelListResponse response = channelSearch.execute();
    				List<Channel> resultList = response.getItems();
    				for (Channel c : resultList) {
    					ChannelContentDetails cont = c.getContentDetails();
    					RelatedPlaylists pls = cont.getRelatedPlaylists();
//    					System.out.println(" Channel Playlist: "+"https://www.youtube.com/playlist?list="+pls.getUploads());
//    					System.out.println(" Channel Subscribers: "+c.getStatistics().getSubscriberCount());
    					if (bestMatch == null) {
    						bestMatch = c;
    					} else if (bestMatch.getStatistics().getSubscriberCount().compareTo(
    							c.getStatistics().getSubscriberCount()) < 0) {
    						bestMatch = c;
    					}
    				}
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            }
            
            
        }
        
        System.out.println("//////////////////////////////////////////////////////////////////////////////");
        System.out.println("/////// BEST MATCH");
        System.out.println("//////////////////////////////////////////////////////////////////////////////\n");
        appendPrintln(builder,"Channel URL: "+ "http://www.youtube.com/channel/" + bestMatch.getId());
        appendPrintln(builder,"Title: " + bestMatch.getSnippet().getTitle());
        appendPrintln(builder,"Channel Subscribers: "+bestMatch.getStatistics().getSubscriberCount());
        appendPrintln(builder,"Channel Playlist: "+"https://www.youtube.com/playlist?list="+bestMatch.getContentDetails().getRelatedPlaylists().getUploads());
        System.out.println("\n-------------------------------------------------------------\n");
        
        return builder.toString();
    }



    private static void appendPrintln(StringBuilder builder, String string) {
    	builder.append(string+"\n");
        System.out.println(string);
    }

}
