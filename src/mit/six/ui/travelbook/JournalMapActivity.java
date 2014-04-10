package mit.six.ui.travelbook;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.OverlayItem;

public class JournalMapActivity extends MapActivity implements ScrollViewListener {

	private static final int UI_CONTEXT_SUMMARY_LENGTH = 10;
	private static final int UI_BUTTON_HEIGHT = 40;
	private static final int UI_ADD_BUTTON_HEIGHT = 40;

	JournalMapView map_view;
	FancyScrollView scroll_view;
	LinearLayout entry_list;
	AddButton add_button;
	ScrollDummyView spacer;
	int spacer_height;
	
	private MapEntryMarkers markers;
	private MapArrow arrow;
	
	public static float dp_scale;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check screen orientation
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.map_horizontal);
		} else {
			setContentView(R.layout.map);
		}

		// Get handles and parameters
		dp_scale = getResources().getDisplayMetrics().density;
		entry_list = (LinearLayout) findViewById(R.id.entry_list);
		map_view = (JournalMapView) findViewById(R.id.map_view);
		scroll_view = (FancyScrollView) findViewById(R.id.scroll_view);
		spacer_height = getWindowManager().getDefaultDisplay().getHeight() / 5;
		
		// Initialize reusable elements
		add_button = new AddButton(this);
		spacer = new ScrollDummyView(this);
		
		// Populate UI
		scroll_view.post(new Runnable() {
			public void run() {
				ui_update();
				scroll_view.fullScroll(FancyScrollView.FOCUS_DOWN);
			}
	    });
		scroll_view.setScrollViewListener(this);
		
		map_view.setBuiltInZoomControls(true);
		
		//ui_update();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		// Note: Icons! menu.setHeaderIcon(iconRes);
		String t = ((EntryButton) v).get_entry().getText();
		if (t.length() > UI_CONTEXT_SUMMARY_LENGTH) {
			t = t.substring(0, 9) + " ...";
		}
		menu.setHeaderTitle(t);
		menu.add(Menu.NONE, R.id.menu_share, 0, "Share");
		menu.add(Menu.NONE, R.id.menu_view, 1, "View");
		menu.add(Menu.NONE, R.id.menu_edit, 2, "Edit");
		menu.add(Menu.NONE, R.id.menu_delete, 3, "Delete");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		EntryButton eb = (EntryButton) item.getMenuInfo();
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_share:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, eb.get_entry().getText());
			startActivity(Intent.createChooser(intent, "Share journal entry with..."));
			return true;
		case R.id.menu_view:
			view_entry(eb.get_entry());
			return true;
		case R.id.menu_edit:
			edit_entry(eb.get_entry());
			return true;
		case R.id.menu_delete:
			delete_entry(eb.get_entry());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void new_entry() {
		Entry new_entry = new Entry();
		TravelBookData.getDB().add(new_entry);

		Entry entry = TravelBookData.getDB().getEntry(
				new_entry.getTimestamp().getTime());
		if (null != entry) {
			Intent start_activity = new Intent(this, ViewEditActivity.class);
			start_activity.putExtra("id", entry.getID());
			start_activity.putExtra("view", false);
			startActivity(start_activity);
			finish(); // ensure this view isn't reachable by "back" button
						// (view/edit activity will create a new journal map
						// view)
		}
	}

	public void edit_entry(Entry entry) {
		Intent start_activity = new Intent(this, ViewEditActivity.class);
		start_activity.putExtra("id", entry.getID());
		start_activity.putExtra("view", false);
		startActivity(start_activity);
		finish(); // ensure this view isn't reachable by "back" button
					// (view/edit activity will create a new journal map view)
	}

	public void view_entry(Entry entry) {
		Intent start_activity = new Intent(this, ViewEditActivity.class);
		start_activity.putExtra("id", entry.getID());
		start_activity.putExtra("view", true);
		startActivity(start_activity);
		finish(); // ensure this view isn't reachable by "back" button
					// (view/edit activity will create a new journal map view)
	}

	public void delete_entry(Entry entry) {
		TravelBookData.getDB().delete(entry);

		ui_update();
	}

	public void ui_update() {
		ui_update_list();
		ui_update_map();
	}

	public void ui_update_list() {
		entry_list.removeAllViews();
		ui_add_entries(TravelBookData.getDB().getEntries());
		ui_add_add_button();
	}

	public void ui_add_entries(ArrayList<Entry> entries) {
		for (Entry entry : entries) {
			ui_add_entry_button(entry);
		}
	}

	public void ui_add_entry_button(Entry entry) {
		EntryButton eb = new EntryButton(this, entry);
		entry_list.addView(eb, new LayoutParams(LayoutParams.MATCH_PARENT,
				(int) (UI_BUTTON_HEIGHT * dp_scale)));

		// listeners for context menu
		registerForContextMenu(eb);
	}
	
	public void ui_add_add_button() {
		entry_list.addView(add_button, new LayoutParams(
				LayoutParams.MATCH_PARENT,
				(int) (UI_ADD_BUTTON_HEIGHT * dp_scale)));
		entry_list.addView(spacer, new LayoutParams(LayoutParams.FILL_PARENT,
				spacer_height));
	}
	
	public void onScrollChanged(FancyScrollView scrollView, int x, int y, int oldx, int oldy) {
		ui_update_map();
	}
	
	/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()){
		case MotionEvent.ACTION_UP:
			Entry.selected = null;
			break;
		}
		return super.onTouchEvent(event);
	}
	*/
	
	private void ui_update_map() {	
		// get entries (weighted) visible in the list
		int scrolled_pixels = scroll_view.getScrollY();
		int scroll_view_height = scroll_view.getHeight();
		int entry_height = (int)(UI_BUTTON_HEIGHT * dp_scale);
		int start_index = scrolled_pixels / entry_height;
		int max_item_count = (int)Math.ceil(((double)scrolled_pixels - (start_index*entry_height) + scroll_view_height) / entry_height);
		
		Log.w("MAP", "scroll: " + scrolled_pixels);
		
		float weight_first = ((float)(scrolled_pixels % entry_height)) / entry_height;
		
		// TODO: fix this at 1 if the last entry is completely shown!
		float weight_last = ((float)((scrolled_pixels+scroll_view_height) % entry_height)) / entry_height;
		
		ArrayList<Entry> db_entries = TravelBookData.getDB().getEntries();
		if (start_index+max_item_count >= db_entries.size()){
			weight_last = 1;	
		}
		
		ArrayList<Entry> visible_entries = new ArrayList<Entry>();
		visible_entries.addAll(db_entries.subList(start_index, Math.min(db_entries.size(), start_index + max_item_count)));
		
		// calculate map center
		// (weighted average of visible entries)
		// This will break horribly if near the ares of discontinuity for latitude or longitude!
		// But fuck it - use a simple average.
		float lat = 0;
		float lon = 0;
		float total_weight = 0;
		
		if (visible_entries.size() > 1){
			for (int i = 0; i<visible_entries.size(); i++){
				Entry e = visible_entries.get(i);
				
				float weight = 1;
				if (i == 0){
					weight = weight_first;
				} else if (i == visible_entries.size()-1) {
					weight = weight_last;
				}
				lat += weight * e.getLatitude();
				lon += weight * e.getLongitude();
				total_weight += weight;
			}
			
			GeoPoint center = new GeoPoint((int)(1000000*lat/total_weight), (int)(1000000*lon/total_weight));
			map_view.mc.animateTo(center);
		} else if (visible_entries.size() == 1){
			Entry e = visible_entries.get(0);
			GeoPoint center = new GeoPoint((int)(1000000*e.getLatitude()), (int)(1000000*e.getLongitude()));
			map_view.mc.animateTo(center);
		}
		
		// Assign entry colors
		if (visible_entries.size() > 1){
			float nw = 0;
			for (int i = 0; i<visible_entries.size(); i++){
				Entry e = visible_entries.get(i);
				
				float weight = 1;
				if (i == 0){
					weight = weight_first;
				} else if (i == visible_entries.size()-1) {
					weight = weight_last;
				}
				
				nw += weight;
				float cw = nw/total_weight;
				float acw = 1.0f-cw;
				e.R = (int)(0x91*cw + 0xD9*acw);
				e.G = (int)(0xD9*cw + 0x1A*acw);
				e.B = (int)(0x1A*cw + 0x91*acw);
			}
		} else if (visible_entries.size() == 1){
			Entry e = visible_entries.get(0);
			e.R = 0x91;
			e.G = 0xD9;
			e.B = 0x1A;
		}
		
		// Mark the entries
		// use overlay
		map_view.getOverlays().clear();
		markers = new MapEntryMarkers(((BitmapDrawable)this.getResources().getDrawable(R.drawable.star)).getBitmap());
		map_view.getOverlays().add(markers);
		
		for (Entry entry : visible_entries){
			markers.addMarker(entry);
		}
		
		// calculate appropriate zoom
		if (visible_entries.size() > 1 ) {
			//GeoPoint center = new GeoPoint((int) entries.get(0).loc_latitude 1000000, (int) entries.get(0).loc_longitude * 1000000);
			//markers.
			int lat_span = (int)(markers.getLatSpanE6()*1.6);
			int lon_span = (int)(markers.getLonSpanE6()*1.6);
			
			while ((lat_span > map_view.getLatitudeSpan()) || (lon_span > map_view.getLongitudeSpan())){
				map_view.mc.zoomOut();
			}
			
			while ((lat_span*2 < map_view.getLatitudeSpan()) && (lon_span*2 < map_view.getLongitudeSpan())){
				map_view.mc.zoomIn();
			}
		} else if (visible_entries.size() == 1){
			map_view.mc.setZoom(7);
		} else if (visible_entries.size() == 0){
			map_view.mc.setZoom(2);
		}
		
		// Compute anchor points for arrow curve
		// If there are more than 2 points visible
		

		if (visible_entries.size() > 2){
			// use 2nd degree Bernstein polynomials for weights
			
			// Start point: linear between first 2 points
			float start_lat = 0;
			float start_lon = 0;
			
			Entry e0 = visible_entries.get(0);
			Entry e1 = visible_entries.get(1);
			
			start_lat = weight_first*e1.getLatitude() + (1.0f-weight_first)*e0.getLatitude();
			start_lon = weight_first*e1.getLongitude() + (1.0f-weight_first)*e0.getLongitude();
			
			// Control point: 2t*(1-t)
			float s_t = 0;
			float w_t = 0;
			float control_lat = 0;
			float control_lon = 0;
			for (int i = 0; i<visible_entries.size(); i++){
				Entry e = visible_entries.get(i);
				
				float weight = 1;
				if (i == 0){
					weight = weight_first;
				} else if (i == visible_entries.size()-1) {
					weight = weight_last;
				}
				
				s_t += weight;
				
				float t = s_t/total_weight;
				float tw = (2*t)*(1.0f-t);
				control_lat += tw * weight * e.getLatitude();
				control_lon += tw * weight * e.getLongitude();
				w_t += tw*weight;
			}
			control_lat /= w_t;
			control_lon /= w_t;
			
			// Endpoint: linear between last 2 points
			float end_lat = 0;
			float end_lon = 0;
			
			Entry ei = visible_entries.get(visible_entries.size()-1);
			Entry ei1 = visible_entries.get(visible_entries.size()-2);
			
			if (visible_entries.size() == 2){
				end_lat = 0.5f*ei.getLatitude() + 0.5f*ei1.getLatitude();
				end_lon = 0.5f*ei.getLongitude() + 0.5f*ei1.getLongitude();
			} else {
				end_lat = weight_last*ei.getLatitude() + (1.0f-weight_last)*ei1.getLatitude();
				end_lon = weight_last*ei.getLongitude() + (1.0f-weight_last)*ei1.getLongitude();
			}
			
			// Draw arrow
			arrow = new MapArrow(start_lat, start_lon, control_lat, control_lon, end_lat, end_lon);
			//arrow = new MapArrow(visible_entries.get(0).getLatitude(), visible_entries.get(0).getLongitude(), center.getLatitudeE6()/1000000.0f, center.getLongitudeE6()/1000000.0f, visible_entries.get(visible_entries.size()-1).getLatitude(), visible_entries.get(visible_entries.size()-1).getLongitude());
			//arrow = new MapArrow(start_lat, start_lon, control_lat, control_lon, visible_entries.get(0).getLatitude(), visible_entries.get(0).getLongitude());
			
			map_view.getOverlays().add(arrow);
		} else if (visible_entries.size() == 2){
			float center_lat = 0.5f*(visible_entries.get(0).getLatitude()+visible_entries.get(1).getLatitude());
			float center_lon = 0.5f*(visible_entries.get(0).getLongitude()+visible_entries.get(1).getLongitude());
			arrow = new MapArrow(	visible_entries.get(0).getLatitude(), visible_entries.get(0).getLongitude(),
									center_lat, center_lon,
									visible_entries.get(1).getLatitude(), visible_entries.get(1).getLongitude());
			map_view.getOverlays().add(arrow);
		}
		
		// Force redraw!
		map_view.invalidate();
	}

	
}
