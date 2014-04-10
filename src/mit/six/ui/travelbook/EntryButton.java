package mit.six.ui.travelbook;

import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

public class EntryButton extends View implements ContextMenuInfo{
	JournalMapActivity activity;
	private Entry entry;
	android.text.StaticLayout text_layout;
	
	static Bitmap camera = null;
	
	float w = 0;
	float h = 0;
	float dp_scale = 0;
	
	float timestamp_width = 75;
	float margin = 5;
	float datetime_adjust_spacing = 0;
	float text_y_offset = 0;
	
	Paint paint;
	TextPaint text_paint;
	
	public EntryButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		entry = new Entry();
		constructor();
	}
	
	public EntryButton(JournalMapActivity context, Entry entry) {
		super(context);
		this.entry = entry;
		
		this.activity = context;
		constructor();
	}
	
	private void constructor(){
		// Initialize dpi scale for device independence
		dp_scale = getResources().getDisplayMetrics().density;
		
		float font_size = 16;
		
		// Initialize paints
		paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		
		paint.setTextSize(font_size*dp_scale);
		text_paint = new TextPaint(paint);
		
		// setup listeners
		/*
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				switch(arg1.getAction()){
				case MotionEvent.ACTION_DOWN:
					entry.select();
					break;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});
		*/
		
		setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.view_entry(entry);
			}
		});
		
		if (camera == null){
			camera = ((BitmapDrawable)this.getResources().getDrawable(R.drawable.camera)).getBitmap();
		}
	}
	
	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return this;
	};
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// resize the off-screen buffer
		
		this.w = w;
		this.h = h;
		
		// static layout for entry text
		//text_layout = new StaticLayout(entry.getText(), text_paint, w-(int)((timestamp_width+2*margin)*dp_scale), Layout.Alignment.ALIGN_NORMAL, 1f, 0, false);
		// String text = entry.getText().replaceAll("[\\r\\n]+", " ");
		// text_layout = new StaticLayout(text, 0, text.length(), text_paint, (int)(w-(timestamp_width+margin)*dp_scale), Layout.Alignment.ALIGN_NORMAL, 1, 0, false, TruncateAt.END, (int)(w-(timestamp_width+margin)*dp_scale));
		//text_layout = new StaticLayout(text, text_paint, (int)(w-(timestamp_width+margin)*dp_scale), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//if (entry.selected == entry){
		//	canvas.drawColor(0xFFDDDDCC);
		//} else {
			canvas.drawColor(Color.WHITE);
		//}
			
		// assign length of text shown
		String text = entry.getText().replaceAll("[\\r\\n]+", " ");
		//text_layout = new StaticLayout(text, 0, text.length(), text_paint, (int)(w-(timestamp_width+margin)*dp_scale), Layout.Alignment.ALIGN_NORMAL, 1, 0, false, TruncateAt.END, 10);	
		
		// Draw separator
		paint.setARGB(255, entry.R, entry.G, entry.B);
		paint.setStrokeWidth(8*dp_scale);
		canvas.drawLine(timestamp_width*dp_scale, 0.1f*h, timestamp_width*dp_scale, 0.9f*h, paint);
				
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(1);
		
		// Draw date & Time
		Date dt = entry.getTimestamp();
		
		int hours = dt.getHours();
		boolean pm = hours >= 12;
		if (hours > 12) {
			hours -= 12; // 22 -> 10 pm
		} else if (hours == 0){
			hours = 12; // 12 am
		}
		String time = String.format("%d:%02d %s", hours, dt.getMinutes(), pm? "pm" : "am");
		canvas.drawText(time, margin*dp_scale, (h/2)-datetime_adjust_spacing*dp_scale, paint);
		
		String date = String.format("%d/%d/%d", dt.getMonth(), dt.getDate(), dt.getYear()%100);
		canvas.drawText(date, margin*dp_scale, (7*h/8)+datetime_adjust_spacing*dp_scale, paint);
		
		// Draw underline
		canvas.drawLine(0, h-1*dp_scale, w, h-1*dp_scale, paint);
		
		canvas.translate((timestamp_width+margin)*dp_scale,h/4);
		
		if (entry.has_pictures()){
			canvas.drawBitmap(camera, 3*dp_scale, 3*dp_scale, null);
			canvas.translate(30*dp_scale,0);
		}
		
		// Draw entry snippet
		//if (text_layout != null){
			//text_layout.draw(canvas);
		if (text != null) {
			if (text.length() > 32) {
				canvas.drawText(text.substring(0, 29) + "...", 0, this.getHeight()*0.4f, text_paint);
			} else {
				canvas.drawText(text, 0, this.getHeight()*0.4f, text_paint);
			}
		}
	}
	
	public Entry get_entry(){
		return entry;
	}
}
