package mit.six.ui.travelbook;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapEntryMarkers extends Overlay {

	private ArrayList<Entry> entries = new ArrayList<Entry>();
	Bitmap marker;
	
	double min_lat;
	double min_lon;
	double max_lat;
	double max_lon;
	
	public MapEntryMarkers(Bitmap marker) {
		this.marker = marker;
		min_lat = 99999;
		min_lon = 999999;
		max_lat = 0;
		max_lon = 0;
	}
	
	public void addMarker(Entry entry) {
		entries.add(entry);
		double lat = entry.getLatitude()*100000;
		double lon = entry.getLatitude()*100000;
		
		min_lat = Math.min(lat, min_lat);
		min_lon = Math.min(lon, min_lon);
		max_lat = Math.max(lat, max_lat);
		max_lon = Math.max(lon, max_lon);
	}

	public void clear() {
		entries.clear();
		min_lat = 0;
		min_lon = 0;
		max_lat = Double.MAX_VALUE;
		max_lon = Double.MAX_VALUE;
	}
	
	public double getLatSpanE6() {
		return max_lat - min_lat;
	}

	public double getLonSpanE6() {
		return max_lon - min_lon;
	};
	
	@Override
	public void draw(Canvas canvas, MapView mapv, boolean shadow) {
		float dp = JournalMapActivity.dp_scale;
		
		Paint paint = new Paint();
        paint.setStrokeWidth(2*dp);
        paint.setAntiAlias(true);
        
		//cycle through all entries to draw
        for (Entry entry : entries)
        {
            // Converts lat/lng-Point to coordinates on the screen
            GeoPoint point = new GeoPoint((int)(entry.getLatitude()*1000000.0f), (int)(entry.getLongitude()*1000000.0f));
            Point pt = new Point() ;
            mapv.getProjection().toPixels(point, pt);

            //Paint background circle for the symbol
            // radius = sqrt(2)*max(bitmap_width, bitmap_height)/2
            float radius = 1.41421356f * 0.5f * Math.max(marker.getWidth(),marker.getHeight());
            //float radius = 0.5f * Math.max(marker.getWidth(),marker.getHeight());
            
            //if (entry.selected == entry){
            //	paint.setARGB(255, 255, 0, 0);
            //} else {
            	paint.setARGB(255, entry.R, entry.G, entry.B);
            //}
            
            paint.setStyle(Style.FILL);
            canvas.drawCircle(pt.x, pt.y, radius, paint);
            paint.setStyle(Style.STROKE);
            paint.setARGB(255, entry.R/2, entry.G/2, entry.B/2);
            canvas.drawCircle(pt.x, pt.y, radius, paint);
            
            // Draw marker symbol
            // TODO: tint using the bitmap paint!
            canvas.drawBitmap(marker, pt.x-marker.getWidth()/2, pt.y-marker.getHeight()/2, null);
        }
	}
}
