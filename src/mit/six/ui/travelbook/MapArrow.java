package mit.six.ui.travelbook;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapArrow extends Overlay {
	GeoPoint start;
	GeoPoint control;
	GeoPoint end;
	
	public MapArrow(float start_lat, float start_lon, float control_lat, float control_lon, float end_lat, float end_lon) {
		start = new GeoPoint((int)(1000000*start_lat), (int)(1000000*start_lon));
		control = new GeoPoint((int)(1000000*control_lat), (int)(1000000*control_lon));
		end = new GeoPoint((int)(1000000*end_lat), (int)(1000000*end_lon));
	}
	
	public void draw(Canvas canvas, MapView mapv, boolean shadow){
        super.draw(canvas, mapv, false);
        
        float dp = JournalMapActivity.dp_scale;

        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        
        Point p_start = mapv.getProjection().toPixels(start, null);
        Point p_control = mapv.getProjection().toPixels(control, null);
        Point p_end = mapv.getProjection().toPixels(end, null);
        
        // perpendicular
        Point v_perp = new Point(p_end.y-p_control.y, p_control.x-p_end.x);
        float scale = (float)(6*dp*(1.0f/Math.sqrt(v_perp.x*v_perp.x+v_perp.y*v_perp.y)));
        v_perp.x = (int)(v_perp.x * scale);
        v_perp.y = (int)(v_perp.y * scale);
        
        Point v_arrow = new Point((int)(v_perp.x*1.5f), (int)(v_perp.y*1.5f));
        Point v_arrowhead = new Point(p_end.x-p_control.x, p_end.y-p_control.y);
        scale = (float)(12*dp*(1.0f/Math.sqrt(v_arrowhead.x*v_arrowhead.x+v_arrowhead.y*v_arrowhead.y)));
        v_arrowhead.x = (int)(v_arrowhead.x * scale);
        v_arrowhead.y = (int)(v_arrowhead.y * scale);
        
        
        Path p = new Path();
        p.reset();
        p.moveTo(p_start.x, p_start.y);
        p.cubicTo(p_control.x, p_control.y, p_control.x, p_control.y, p_end.x+v_perp.x, p_end.y+v_perp.y);
        p.lineTo(p_end.x+v_arrow.x, p_end.y+v_arrow.y);
        
        p.lineTo(p_end.x+v_arrowhead.x, p_end.y+v_arrowhead.y);
        
        p.lineTo(p_end.x-v_arrow.x, p_end.y-v_arrow.y);
        p.lineTo(p_end.x-v_perp.x, p_end.y-v_perp.y);
        p.cubicTo(p_control.x, p_control.y, p_control.x, p_control.y, p_start.x, p_start.y);
        
        canvas.drawPath(p, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFF1A91D9);
        mPaint.setShadowLayer(3.0f, 4, 4, 0x5A000000);
        canvas.drawPath(p, mPaint);
    }

}
