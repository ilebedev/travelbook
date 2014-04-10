package mit.six.ui.travelbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;

public class Entry {
	private int _id;
	private Date timestamp;
	float loc_latitude;
	float loc_longitude;
	float loc_accuracy;
	public int R, G, B;
	private String text;
	private ArrayList<Bitmap> pictures;
	private ArrayList<ArrayList<String> > contacts;
	boolean unedited;
	
	//public static Entry selected;
	
	static float lat = 37.299151f;
	static float lon = -85.97683f;
	
	public Entry(){
		_id = -1;
		timestamp = new Date();
		loc_latitude = lat;
		loc_longitude = lon;
		loc_accuracy = 5f;
		
		Random rand = new Random();
		lat += (rand.nextFloat()-0.2f)*2.0f;
		lon += (rand.nextFloat()-0.2f)*2.0f;
		
		pictures = new ArrayList<Bitmap>();
		contacts = new ArrayList<ArrayList<String> >();
		
		text = "";
		
		unedited = true;		
	}
	
	public Entry(int _id, Date timestamp, float latitude, float longitude, float accuracy, String text){
		this._id = _id;
		this.timestamp = timestamp;
		this.loc_latitude = latitude;
		this.loc_longitude = longitude;
		this.loc_accuracy = accuracy;
		this.text = text;
		
		pictures = new ArrayList<Bitmap>();
		contacts = new ArrayList<ArrayList<String> >();
		
		unedited = true;
	}
	
	public int getID(){
		return _id;
	}
	
	public void setID(int id){
		_id = id;
	}
	
	public String getText(){
		return text;
	}
	public void setText(String t) {
		edit();
		text = t;
	}
	
	public Date getTimestamp(){
		return timestamp;
	}
	
	public String formatDate(){
		return String.format("%d/%d/%d", timestamp.getMonth(), timestamp.getDate(), timestamp.getYear() % 100);
	}
	
	public String formatLocation(Context context){
		String loc;
		Geocoder gc = new Geocoder(context);
		try {
			List<Address> addresses = gc.getFromLocation(loc_latitude, loc_longitude, 1);
			if (addresses.size() > 0){
				loc = addresses.get(0).getLocality();
			} else {
				loc = "somewhere";
			}
		} catch (Exception e) {
			loc = "somewhere";
		}
		
		return "near " + loc;
	}
	
	public String formatTime(){
		int hours = timestamp.getHours();
		boolean pm = hours >= 12;
		if (hours > 12) {
			hours -= 12; // 22 -> 10 pm
		} else if (hours == 0) {
			hours = 12; // 12 am
		}
		String time = String.format("%d:%02d %s", hours, timestamp.getMinutes(), pm ? "pm" : "am");
		return time;
	}
	
	public float getLatitude(){
		return loc_latitude;
	}
	
	public float getLongitude(){
		return loc_longitude;
	}
	
	public float getAccuracy(){
		return loc_accuracy;
	}
	
	public boolean has_pictures(){
		return !pictures.isEmpty();
	}

	public boolean has_contact() {
		return !contacts.isEmpty();
	}
	
	private void edit(){
		unedited = false;
	}
	
	public boolean is_unedited(){
		return unedited;
	}
	
	public void add_picture(Bitmap pic){
		edit();
		pictures.add(pic);
	}
	
	public List<Bitmap> get_pictures(){
		return pictures;
	}
	
	public void delete_picture(Bitmap pic){
		pictures.remove(pic);
	}
	
	public void add_contact(ArrayList<String> id) {
		contacts.add(id);
	}
	
	public ArrayList<ArrayList<String> > get_contacts() {
		return contacts;
	}
	
	public void delete_contact(String id) {
		contacts.remove(id);
	}
	
	//public void select(){
	//	selected = this;
	//}
}
