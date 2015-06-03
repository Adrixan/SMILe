package youtube;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import main.Launcher;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

public class PlaylistFinder {

	private final boolean DEBUG = false;

	private String artistName;

	private YouTube youtube;
	private String key;
	// private final long NUMBER_OF_CHANNELS_RETURNED = 25;

	/** Instance of the HTTP transport. */
	private final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Instance of the JSON factory. */
	private final JacksonFactory JSON_FACTORY = new JacksonFactory();

	private static final HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
		public void initialize(HttpRequest request) throws IOException {
		}
	};

	public PlaylistFinder(String artistName) {
		this.artistName = artistName;

		if (DEBUG)
			key = YoutubeAPITest.properties.getProperty("youtube.key");
		else
			key = Launcher.properties.getProperty("youtube.key");

		// This object is used to make YouTube Data API requests. The last
		// argument is required, but since we don't need anything
		// initialized when the HttpRequest is initialized, we override
		// the interface and provide a no-op function.
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				httpRequestInitializer).setApplicationName("SMILe").build();
	}

	public String getPlaylistInfo() throws IOException {

		String retString = "";

		// Define the API request for retrieving search results.
		YouTube.Search.List search = youtube.search().list("id,snippet");

		// Set your developer key from the Google Developers Console for
		// non-authenticated requests. See:
		// https://console.developers.google.com/
		search.setKey(key);
		search.setQ(artistName);

		// Restrict the search results to only include videos. See:
		// https://developers.google.com/youtube/v3/docs/search/list#type
		search.setType("channel");

		// Call the API and print results.
		SearchListResponse searchResponse = search.execute();
		List<SearchResult> searchResultList = searchResponse.getItems();
		if (searchResultList != null) {
			retString = createPlaylistString(searchResultList.iterator());
		}

		return retString;
	}

	/**
	 * Select the best fitting channel based on subscriber count and create an
	 * info string for its playlist
	 * 
	 * @param iteratorSearchResults
	 * @return
	 */
	private String createPlaylistString(
			Iterator<SearchResult> iteratorSearchResults) {

		StringBuilder builder = new StringBuilder();
		builder.append("Artist Name: " + artistName + "\n");

		if (!iteratorSearchResults.hasNext()) {
			// System.out.println(" There aren't any results for your query.");
			return "There aren't any results for your query.";
		}

		Channel bestMatch = null;

		while (iteratorSearchResults.hasNext()) {

			SearchResult singleChannel = iteratorSearchResults.next();
			ResourceId rId = singleChannel.getId();

			// Confirm that the result represents a channel. Otherwise, the
			// item will not contain a channel ID.
			if (rId.getKind().equals("youtube#channel")) {
				try {
					YouTube.Channels.List channelSearch = youtube.channels()
							.list("contentDetails,statistics,snippet");
					channelSearch.setKey(key);
					channelSearch.setId(rId.getChannelId());
					ChannelListResponse response = channelSearch.execute();
					List<Channel> resultList = response.getItems();
					for (Channel c : resultList) {
						// if the new channel has more subscribers than the old
						// one
						// make it the new best channel
						if (bestMatch == null) {
							bestMatch = c;
						} else if (bestMatch
								.getStatistics()
								.getSubscriberCount()
								.compareTo(
										c.getStatistics().getSubscriberCount()) < 0) {
							bestMatch = c;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		if (DEBUG) {
			System.out
			.println("//////////////////////////////////////////////////////////////////////////////");
			System.out.println("/////// BEST MATCH");
			System.out
			.println("//////////////////////////////////////////////////////////////////////////////\n");
		}
		appendPrintln(builder, "Channel URL: "
				+ "http://www.youtube.com/channel/" + bestMatch.getId());
		appendPrintln(builder, "Title: " + bestMatch.getSnippet().getTitle());
		appendPrintln(builder, "Channel Subscribers: "
				+ bestMatch.getStatistics().getSubscriberCount());
		appendPrintln(builder, "Channel Playlist: "
				+ "https://www.youtube.com/playlist?list="
				+ bestMatch.getContentDetails().getRelatedPlaylists()
				.getUploads());

		return builder.toString();
	}

	private void appendPrintln(StringBuilder builder, String string) {
		builder.append(string + "\n");
		if (DEBUG)
			System.out.println(string);
	}

}
