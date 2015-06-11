package pojo;

import java.util.ArrayList;

public class AlbumPojo {

	private String amazonUid="";
	
	private ArrayList<Album> alben = new ArrayList<Album>();
	
	public AlbumPojo(String key) {
		this.amazonUid = key;
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
	

}
