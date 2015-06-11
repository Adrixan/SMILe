package pojo;

import java.util.ArrayList;

public class ArtistPojo {

	private String artistName = "";

	// twitter-section
	private ArrayList<Object> twitterSection;

	// youtube-section
	private String yChannel = "";
	private String yPlaylist = "";
	private String yChannelName="";
	private String ySubscriber="";
	
	// amazon-section
	private String amazonSection = "";
	// last.fm-section
	private ArrayList<LocationPojo> lastFMSection = new ArrayList<LocationPojo>();

	public ArtistPojo() {

	}

	public ArtistPojo(String name) {
		this.artistName = name;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public ArrayList<Object> getTwitterSection() {
		return twitterSection;
	}

	public void setTwitterSection(ArrayList<Object> twitterSection) {
		if (twitterSection.isEmpty()) {
			twitterSection.add("No tweets available");
		}
		this.twitterSection = twitterSection;

	}

	public String getyChannel() {
		return yChannel;
	}

	public void setyChannel(String yChannel) {
		this.yChannel = yChannel;
	}

	public String getyPlaylist() {
		return yPlaylist;
	}

	public void setyPlaylist(String yPlaylist) {
		this.yPlaylist = yPlaylist;
	}

	public String getyChannelName() {
		return yChannelName;
	}

	public void setyChannelName(String yChannelName) {
		this.yChannelName = yChannelName;
	}

	public String getySubscriber() {
		return ySubscriber;
	}

	public void setySubscriber(String ySubscriber) {
		this.ySubscriber = ySubscriber;
	}

	public ArrayList<LocationPojo> getLastFMSection() {
		return lastFMSection;
	}

	public void setLastFMSection(ArrayList<LocationPojo> lastFMSection) {	
		System.out.println("++++++++++++++++++++++++++++++++++++++Last FM wird gesetzt"+lastFMSection.size());
		this.lastFMSection = lastFMSection;
	}

}
