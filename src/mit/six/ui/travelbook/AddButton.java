package mit.six.ui.travelbook;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class AddButton extends View{
	JournalMapActivity activity;
	
	float w = 0;
	float h = 0;
	float dp_scale = 0;
	
	Paint paint;
	
	public AddButton(JournalMapActivity context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		constructor();
	}
	
	public AddButton(JournalMapActivity context, AttributeSet attrs) {
		super(context, attrs);
		constructor();
	}
	
	public AddButton(JournalMapActivity context) {
		super(context);
		
		this.activity = context;
		constructor();
	}
	
	private void constructor(){
		// Initialize dpi scale for device independence
		dp_scale = getResources().getDisplayMetrics().density;
		
		// Initialize paints
		paint = new Paint();
		
		// Listeners
		
		setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.new_entry();
			}
		});
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// resize the off-screen buffer
		
		this.w = w;
		this.h = h;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		float margin = 5;
		float inset = 5;
		float rounding = 10;
		float plus_thickness = 8/2;
		
		float text_scale = 0.4f;
		float text_y_offset = -2;
		float text_x_offset = 20;
		
		canvas.drawColor(Color.WHITE);
		
		// Draw "+" background
		paint.setColor(0xFF91D91A);
		paint.setAntiAlias(true);
		RectF add_rect = new RectF(margin*dp_scale, margin*dp_scale, h-margin*dp_scale, h-margin*dp_scale);
		canvas.drawRoundRect(add_rect, rounding*dp_scale, rounding*dp_scale, paint);
		
		// Draw "+" foreground
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(false);
		canvas.drawRect((margin+inset)*dp_scale, (h/2)-plus_thickness*dp_scale, h-(margin+inset)*dp_scale, (h/2)+plus_thickness*dp_scale, paint);
		canvas.drawRect((h/2)-plus_thickness*dp_scale, (margin+inset)*dp_scale, (h/2)+plus_thickness*dp_scale, h-(margin+inset)*dp_scale, paint);
		
		// Draw "new entry" label
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(0);
		paint.setAntiAlias(true);
		paint.setTextSize(h*text_scale*dp_scale);
		canvas.drawText("(new entry)", h+text_x_offset*dp_scale, (3*h/4)+text_y_offset*dp_scale, paint);
		
		// Draw underline
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(2);
		canvas.drawLine(0, h-1*dp_scale, w, h-1*dp_scale, paint);
	}
}
