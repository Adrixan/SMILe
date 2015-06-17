package pojo;

// Album fuer Amazon Daten (pro Album ein Eintrag in einer Liste)
public class Album {
	private String title="";
	private String price="";
	private String imageurl="";
	private String pageurl="";
	
	
	public Album(){
		
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		if(price.equals("")){
			price="No price available!";
		}
		this.price = price;
	}

	public String getImageurl() {
		return imageurl;
	}

	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
	}

	public String getPageurl() {
		return pageurl;
	}

	public void setPageurl(String pageurl) {
		this.pageurl = pageurl;
	}


}
