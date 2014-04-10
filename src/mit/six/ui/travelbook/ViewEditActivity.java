package mit.six.ui.travelbook;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Contacts.People;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ViewEditActivity extends Activity {
	Entry entry;
	boolean viewMode = true;
	ViewPager postPager;
	String pending_text;
	ArrayList<Bitmap> pending_pictures;
	ArrayList<Bitmap> all_pictures;
	
	ArrayList<ArrayList<String> > pending_contacts;
	boolean edit_pending;
	
	private static final int CAMERA_PIC_REQUEST = 1337;
	private static final int CONTACT_PICKER_RESULT = 1001;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (pending_pictures == null){
			pending_pictures = new ArrayList<Bitmap>();
		}
		
		int id = getIntent().getExtras().getInt("id");
		boolean view = getIntent().getExtras().getBoolean("view");
		entry = TravelBookData.getDB().getEntry(id);
		
		if (all_pictures == null) {
			all_pictures = new ArrayList<Bitmap>();
			for (int i = 0; i < entry.get_pictures().size(); i++) {
				all_pictures.add(entry.get_pictures().get(i));
			}
		}
		if (pending_contacts == null) {
			pending_contacts = new ArrayList<ArrayList<String> >();

		}
		


		if (view) {
			view_entry(entry);
		} else {
			edit_entry(entry);
		}
	}
	
	public void view_entry(final Entry entry) {
		setContentView(R.layout.view);
		
		postPager = (ViewPager)findViewById(R.id.navigate_between_posts);
		postPager.setAdapter(new NavigatePosts(this, TravelBookData.getDB()));
		postPager.setCurrentItem(TravelBookData.getDB().get_ordering(entry));
	}
	
	public void edit_entry(final Entry entry) {
		viewMode = false;
		edit_pending = false;
		
		setContentView(R.layout.edit);

		Button photo_button = (Button) findViewById(R.id.photo_button);
		photo_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
			}
		});

		TextView entry_date_edit = (TextView) (findViewById(R.id.entry_date_edit));
		entry_date_edit.setText(entry.formatDate() + ", " + entry.formatTime() + " " + entry.formatLocation(this));

		EditText entry_text_edit = (EditText) (findViewById(R.id.entry_text_edit));
		entry_text_edit.setText(entry.getText());
		entry_text_edit.setFocusable(true);
		entry_text_edit.requestFocus();
		
		LinearLayout photo_list = (LinearLayout) findViewById(R.id.photo_list);
		update_photos(photo_list);
		LinearLayout contacts_list = (LinearLayout) findViewById(R.id.contacts_list);
		update_contacts(contacts_list);
	}

	public class getContactInfo implements View.OnClickListener {
		private String number;
		public getContactInfo(String n) {
			this.number = n;
		}
		public void onClick(View v) {

//			Intent intent = new Intent();
//			intent.setAction(Intent.ACTION_VIEW);
			
			// TODO: What?
			Uri lookupUri = Uri.parse("tel:" + this.number);
			Intent intent = new Intent(Intent.ACTION_VIEW, lookupUri);
			startActivity(intent);

		}
		public String getNumber() {
			return number;
		}
	}
	
	private void update_photos(LinearLayout photo_list){
		photo_list.removeAllViews();
		
		int counter = 0;
		for (Bitmap pic : entry.get_pictures()){
			FancyImageView new_capture = new FancyImageView(this);
			new_capture.setId(counter);
			counter = counter + 1;
			new_capture.setImageBitmap(pic);
			new_capture.setPadding(10, 10, 10, 10);
			//if (!viewMode) new_capture.setOnCreateContextMenuListener(this);
			if (!viewMode) registerForContextMenu(new_capture);
			photo_list.addView(new_capture);
		}
		
		for (Bitmap pic : pending_pictures){
			FancyImageView new_capture = new FancyImageView(this);
			new_capture.setId(counter);
			counter = counter + 1;
			new_capture.setImageBitmap(pic);
			new_capture.setPadding(10, 10, 10, 10);
			//if (!viewMode) new_capture.setOnCreateContextMenuListener(this);
			if (!viewMode) registerForContextMenu(new_capture);
			photo_list.addView(new_capture);
		}
	}

	private void update_contacts(LinearLayout contacts_list){
		contacts_list.removeAllViews();
		
		for (ArrayList<String> contact : entry.get_contacts()){
			Button new_contact = new Button(this);
			new_contact.setText(contact.get(0) + " " + contact.get(1));
			getContactInfo c = new getContactInfo(contact.get(1));
			new_contact.setOnClickListener(c);
			new_contact.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			contacts_list.addView(new_contact);
		}
		
		for (ArrayList<String> contact : pending_contacts){
			Button new_contact = new Button(this);
			new_contact.setText(contact.get(0) + " " + contact.get(1));
			getContactInfo c = new getContactInfo(contact.get(1));
			new_contact.setOnClickListener(c);
			new_contact.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			contacts_list.addView(new_contact);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		// Note: Icons! menu.setHeaderIcon(iconRes);
		menu.setHeaderTitle("Photo...");
		menu.add(Menu.NONE, R.id.menu_delete, 3, "Delete");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//try{
			FancyImageView img = (FancyImageView) item.getMenuInfo();
			switch (item.getItemId()) {
			case R.id.menu_delete:
				int ID = img.getId();
				if (ID < entry.get_pictures().size()) {
					entry.delete_picture(entry.get_pictures().get(ID));
					TravelBookData.getDB().edit(entry);
				} else {
					pending_pictures.remove(all_pictures.get(ID));
					all_pictures.remove(all_pictures.get(ID));
				}
				LinearLayout photo_list = (LinearLayout) findViewById(R.id.photo_list);
				update_photos(photo_list);
				return true;
			}
			//return false;
		//} catch (Exception e){
			// nothing
		//}
		return false;
	}

	/*
	private OnCreateContextMenuListener deletePicListener = new OnCreateContextMenuListener() {
	    public void onClick(View v) {
	      
	    }
	    
	    public boolean onContextItemSelected(MenuItem item) {
			ImageView img = (ImageView) item.getMenuInfo();

			switch (item.getItemId()) {
			case R.id.menu_delete:
				int ID = img.getId();
				if (ID < entry.get_pictures().size()) {
					entry.delete_picture(entry.get_pictures().get(ID));
					TravelBookData.getDB().edit(entry);
				} else {
					pending_pictures.remove(all_pictures.get(ID));
				}
				LinearLayout photo_list = (LinearLayout) findViewById(R.id.photo_list);
				update_photos(photo_list);
				return true;
			}
			return false;
		}

		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.add(Menu.NONE, R.id.menu_delete, 3, "Delete");
		}
	};
	*/


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Cursor cursor = null;
				try {
					Uri result = data.getData();
					cursor = managedQuery(result, null, null, null, null);

					int phoneIdx = cursor.getColumnIndex(Phone.DATA);
					int nameIdx = cursor
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
					if (cursor.moveToFirst()) {
						String phoneNumber = cursor.getString(phoneIdx);
						String name = cursor.getString(nameIdx);

						LinearLayout contacts_list = (LinearLayout) findViewById(R.id.contacts_list);
		//				String ID = cursor.getString(
		//                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
						ArrayList<String> new_contact = new ArrayList<String>();
						new_contact.add(name);
						new_contact.add(phoneNumber);
						pending_contacts.add(new_contact);
						update_contacts(contacts_list);
/*						new_contact.setText(name + " " + phoneNumber);
						new_contact.setOnClickListener(getContactInfo);
						new_contact.setLayoutParams(new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
						contacts_list.addView(new_contact);
*/

					}
				} catch (Exception e) {
				}

				break;
			case CAMERA_PIC_REQUEST:
				Bitmap capture = (Bitmap) data.getExtras().get("data");
				pending_pictures.add(capture);
				all_pictures.add(capture);
				LinearLayout photo_list = (LinearLayout) findViewById(R.id.photo_list);
				update_photos(photo_list);
				break;
			}
		}
	}
	
	public void doLaunchContactPicker(View view) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, R.id.menu_edit, 0, "Edit");
		menu.add(Menu.NONE, R.id.menu_share, 1, "Share");
		menu.add(Menu.NONE, R.id.menu_delete, 2, "Delete");

		return true;

	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!viewMode) {
			menu.removeItem(R.id.menu_edit);
			menu.removeItem(R.id.menu_share);
			menu.removeItem(R.id.menu_delete);

			menu.removeItem(R.id.menu_save);
			menu.add(Menu.NONE, R.id.menu_save, 0, "Save");
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (viewMode) {
			entry = TravelBookData.getDB().get_by_ordering(postPager.getCurrentItem());
		}
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_share:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, entry.getText());
			startActivity(Intent.createChooser(intent, "Share journal entry with..."));
			return true;
		case R.id.menu_edit:
			edit_entry(entry);
			return true;
		case R.id.menu_delete:
			TravelBookData.getDB().delete(entry);
			back_to_map();

			return true;
		case R.id.menu_save:
			save();
			back_to_map();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void save(){
		EditText entry_text_edit = (EditText) (findViewById(R.id.entry_text_edit));
		pending_text = entry_text_edit.getText().toString();
		entry.setText(pending_text);
		for (Bitmap pic : pending_pictures){
			entry.add_picture(pic);
		}
		for (ArrayList<String> contact : pending_contacts) {
			entry.add_contact(contact);
		}
		
		TravelBookData.getDB().edit(entry);
	}

	public void onBackPressed() {
		if (!viewMode) {
			// TODO some stuff here..
			
			EditText entry_text_edit = (EditText) (findViewById(R.id.entry_text_edit));
			edit_pending = !pending_pictures.isEmpty() | (!entry_text_edit.getText().toString().equals(entry.getText()));

			// If this is a new entry, and no input was given,
			// Delete it!
			
			if (entry.is_unedited() && !edit_pending){
				TravelBookData.getDB().delete(entry);
				back_to_map();
			} else {
				if (!edit_pending){
					back_to_map();
				} else {
					pending_text = entry_text_edit.getText().toString();
				
					// Ask the user to save or discard
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
					
					alert.setTitle("Unsaved changes!");
					alert.setMessage("What would you like to do with this unsaved journal entry?");
	
					alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							save();
							back_to_map();
						}
					});
	
					alert.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// Discarded.
							if (entry.is_unedited()){
								TravelBookData.getDB().delete(entry);
							}
							back_to_map();
						}
					});
	
					alert.show();
				}
			}
		} else {
			back_to_map();
		}
	}
	
	private void back_to_map(){
		Intent start_activity = new Intent(this, JournalMapActivity.class);
		startActivity(start_activity);
		finish(); // kill this view so it isn't reachable via the back button.
	}
	
	private class NavigatePosts extends PagerAdapter {
		TravelBookData db;
		Context context;

		public NavigatePosts(Context context, TravelBookData db) {
			super();
			this.context = context;
			this.db = db;
		}

		@Override
		public int getCount() {
			return db.getEntries().size();
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			/*
			<ScrollView view_scrollable>
				<LinearLayout entryview>
					<TextView date_info>
					<ScrollView photo_scrollable>
						<LinearLayout photo_list>
					</ScrollView>
					<TextView text>
				</LinearLayout>
			</ScrollView>
			*/
			
			// <ScrollView view_scrollable>
			ScrollView view_scroll = new ScrollView(context);
			view_scroll.setLayoutParams(new ScrollView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));

			// <LinearLayout entryview>
			LinearLayout entry_view = new LinearLayout(context);
			entry_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			entry_view.setOrientation(LinearLayout.VERTICAL);

			entry = db.get_by_ordering(position);
			
			// <TextView date_info>
			TextView entry_info = new TextView(context);
			entry_info.setText(entry.formatDate() + ", " + entry.formatTime() + " " + entry.formatLocation(context));
			entry_view.addView(entry_info);
			
			// <ScrollView photo_scrollable>
			ScrollView photo_scroll = new ScrollView(context);
			photo_scroll.setLayoutParams(new ScrollView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
			entry_view.addView(photo_scroll);
			
			// <LinearLayout photo_list>
			LinearLayout photo_list = new LinearLayout(context);
			photo_list.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			photo_list.setOrientation(LinearLayout.HORIZONTAL);
			update_photos(photo_list);
			photo_scroll.addView(photo_list);
			
			// <TextView text>
			TextView text = new TextView(context);
			text.setText(entry.getText());
			entry_view.addView(text);

			// <LinearLayout contacts_list>
			LinearLayout contacts_list = new LinearLayout(context);
			contacts_list.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			contacts_list.setOrientation(LinearLayout.VERTICAL);
			update_contacts(contacts_list);
			entry_view.addView(contacts_list);

			view_scroll.addView(entry_view);
			((ViewPager) collection).addView(view_scroll, 0);
			
			return view_scroll;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == ((View) arg1);

		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
	
	private class FancyImageView extends ImageView implements ContextMenuInfo{

		public FancyImageView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			// TODO Auto-generated constructor stub
		}
		
		public FancyImageView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		public FancyImageView(Context context) {
			super(context);
		}
		
		@Override
		protected ContextMenuInfo getContextMenuInfo() {
			return this;
		}
	}
}
