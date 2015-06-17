package pojo;


import java.sql.Date;

// Events fuer Last.fm Daten
public class Event implements Comparable<Event> {

	private int id;
	private String title;
	private String artist; 
	private String country;
	private String city;
	private String website; // ?
	private Date date;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	@Override
	public String toString() {
		return title + " - " + date;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (id != other.getId())
			return false;
		if (title == null) {
			if (other.getTitle() != null)
				return false;
		} else if (!title.equals(other.getTitle()))
			return false;

		if (website == null) {
			if (other.getWebsite() != null)
				return false;
		} else if (!website.equals(other.getWebsite()))
			return false;

		if (country == null) {
			if (other.getCountry() != null)
				return false;
		} else if (!country.equals(other.getCountry()))
			return false;

		if (date == null) {
			if (other.getDate() != null)
				return false;
		} else if (!date.equals(other.getDate()))
			return false;

		return true;
	}

	@Override
	public int compareTo(Event e) {
		return date.compareTo(e.getDate());
	}




}