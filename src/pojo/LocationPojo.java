package pojo;

import java.util.ArrayList;

public class LocationPojo {
	
	private String locationName;
	private ArrayList<String> events;
	
	public LocationPojo(){
		
	}
	
	public LocationPojo(String locName){
		this.locationName = locName+":";
	}
	
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public ArrayList<String> getEvents() {
		return events;
	}
	public void setEvents(ArrayList<String> events) {
		this.events = events;
	}

	public void setLocationsEmpty() {
		this.locationName = "No upcoming events!";
	}

}
