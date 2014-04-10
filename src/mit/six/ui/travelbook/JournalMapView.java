package mit.six.ui.travelbook;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class JournalMapView extends MapView{
	MapController mc;
	
	public JournalMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		constructor();
	}
	
	public JournalMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		constructor();
	}
	
	public JournalMapView(Context context, String apiKey) {
		super(context, apiKey);
		
		constructor();
	}
	
	void constructor(){
		//float latitude = 42.299151f;
		//float longitude = -70.97683f;
		
		//GeoPoint loc = new GeoPoint((int) (latitude * 1000000), (int) (longitude * 1000000));
		setSatellite(false);
		
		mc = getController();
		//mc.setCenter(loc);
		//mc.setZoom(8); 
	}
	
	public void showEntries(ArrayList<Entry> entries){
		
	}
}
