package pojo;

import java.util.ArrayList;

//Album-Pojo fuer Amazon Daten (pro Album ein Eintrag in einer Liste)
public class AlbumPojo {

	private String amazonUid="";
	
	private ArrayList<Album> alben = new ArrayList<Album>();
	
	public AlbumPojo(String key) {
		this.amazonUid = key;
	}
	
	public AlbumPojo() {
	}

	public String getAmazonUid() {
		return amazonUid;
	}

	public void setAmazonUid(String amazonUid) {
		this.amazonUid = amazonUid;
	}
	
	public ArrayList<Album> getAlben() {
		return alben;
	}

	public void setAlben(ArrayList<Album> alben) {
		this.alben = alben;
	}

	public void setAlbenEmpty() {
		this.amazonUid = "No alben available!";
		
	}
	

}
