package org.myftp.kss;

/**
 * Station bean for encapsulate essential parameters
 */
public class StationBean {
	private String bid;
	private String bname;
	private int blatitude;
	private int blongitude;
	
	public StationBean(String bid, String bname, int blatitude,
			int blongitude) {
		super();
		this.bid = bid;
		this.bname = bname;
		this.blatitude = blatitude;
		this.blongitude = blongitude;
	}
	public String getBid() {
		return bid;
	}
	public void setBid(String bid) {
		this.bid = bid;
	}
	public String getBname() {
		return bname;
	}
	public void setBname(String bname) {
		this.bname = bname;
	}
	public int getBlatitude() {
		return blatitude;
	}
	public void setBlatitude(int blatitude) {
		this.blatitude = blatitude;
	}
	public int getBlongitude() {
		return blongitude;
	}
	public void setBlongitude(int blongitude) {
		this.blongitude = blongitude;
	}

	
}
