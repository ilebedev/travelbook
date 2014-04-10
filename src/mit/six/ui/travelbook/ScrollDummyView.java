package mit.six.ui.travelbook;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class ScrollDummyView extends View{
	public ScrollDummyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public ScrollDummyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ScrollDummyView(Context context) {
		super(context);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// NOTHING AT ALL! This view is invisible
	}
}
