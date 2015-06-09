package com.zhao.paipai.bianhuan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class Crop extends Basebh {
	private Bitmap src = null;
	private Paint paint = null;
	private int left=-1,top=-1,right=-1,bottom=-1;
	private PointF P1,P2,P3,P4;
	private final int R = 5; 
	private int oLeft,oTop,oRight,oBottom;
	private int[] loc = new int[2];
	private UiCallback callback;
	@SuppressWarnings("deprecation")
	private GestureDetector gesture = new GestureDetector(new MyGestureListener()) ;
	
	public Crop(Context context,Bitmap bm,UiCallback callback){
		super(context);
		this.src = bm;
		this.paint = new Paint();
		this.callback = callback;
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
	}
	
	public Crop(Context context,AttributeSet attr){
		super(context,attr);
	}
	
	public void setSrc(Bitmap bm){
		this.src = bm;
		this.paint = new Paint();
	}
	
	@Override
	public void resize(int l,int t,int r,int b){
		if(r-l<=48 || b-t<=48 || l<oLeft || t<oTop || r>oRight || b>oBottom ) return;
		this.left=l;
		this.top=t;
		this.right=r;
		this.bottom=b;
		callback.OnStateChanged(UiCallback.STATE_CHANGED, null);
		postInvalidate();
	}

	@Override
	public Bitmap perform(int radius){
		Bitmap dest = null;
		dest = Bitmap.createBitmap(right-left, bottom-top, Config.ARGB_8888);
		if(radius<=0){
			Canvas canvas = new Canvas(dest);
			canvas.drawBitmap(src, new Rect(left,top,right,bottom), new Rect(0,0,right-left,bottom-top), paint);
		}
		callback.OnStateChanged(UiCallback.STATE_PERFORMED, dest);
		return dest;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gesture.onTouchEvent(event);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		
		this.oLeft=this.left=left;
		this.oTop=this.top=top;
		this.oRight=this.right=right;
		this.oBottom=this.bottom=bottom;
		getLocationOnScreen(loc);
		super.onLayout(changed, left, top, right, bottom);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float l=this.left+4, t=this.top+4, r=this.right-4, b=this.bottom-4;
		//half
		float hx = r - (r - l) * 1.0f / 2 - 2;
		float hy = b - (b - t) * 1.0f / 2 - 2; 
		//1/3
		float x1 = l + (r - l) * 1.0f / 3 ;
		float y1 = t + (b - t) * 1.0f / 3 ;
		//2/3
		float x2 = l + (r - l) * 2.0f / 3 ;
		float y2 = t + (b - t) * 2.0f / 3 ;
		
		P1 = new PointF(hx, t);
		P2 = new PointF(r, hy);
		P3 = new PointF(hx, b);
		P4 = new PointF(l, hy);
		paint.setStrokeWidth(1);
		
		//draw rect of out
		paint.setColor(Color.BLUE);
		paint.setStyle(Style.STROKE);
		canvas.drawRect(l, t, r, b, paint);
		
		//draw ctrl point
		paint.setColor(Color.RED);
		paint.setStyle(Style.FILL);
		canvas.drawCircle(P1.x,P1.y, R, paint);
		canvas.drawCircle(P2.x,P2.y, R, paint);
		canvas.drawCircle(P3.x,P3.y, R, paint);
		canvas.drawCircle(P4.x,P4.y, R, paint);
		
		//draw net
		paint.setColor(Color.CYAN);
		canvas.drawLine(x1, t, x1, b, paint);
		canvas.drawLine(x2, t, x2, b, paint);
		canvas.drawLine(l, y1, r, y1, paint);
		canvas.drawLine(l, y2, r, y2, paint);
	}
	
	
	class MyGestureListener extends SimpleOnGestureListener{
	 
		int dp=0;
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float dx, float dy) {
			if(dp==0) return false;
			
			if(dp==1){
				resize(left,top-(int)dy,right,bottom);
			}else if(dp==2){
				resize(left,top,right-(int)dx,bottom);
			}else if(dp==3){
				resize(left,top,right,bottom-(int)dy);
			}else if(dp==4){
				resize(left-(int)dx,top,right,bottom);
			}else if(dp==5){
				resize(left-(int)dx,top-(int)dy,right-(int)dx,bottom-(int)dy);
			}
			return true;
		}

		@Override 
		public boolean onDown(MotionEvent e) {
			dp = 0;
			float x=e.getRawX()-loc[0];
			float y=e.getRawY()-loc[1];
			if(Math.sqrt(Math.pow(x-P1.x, 2)+Math.pow(y-P1.y, 2))<=3*R){
				dp = 1;
			}else if(Math.sqrt(Math.pow(x-P2.x, 2)+Math.pow(y-P2.y, 2))<=3*R){
				dp = 2;
			}else if(Math.sqrt(Math.pow(x-P3.x, 2)+Math.pow(y-P3.y, 2))<=3*R){
				dp = 3;
			}else if(Math.sqrt(Math.pow(x-P4.x, 2)+Math.pow(y-P4.y, 2))<=3*R){
				dp = 4;
			}else if(Math.sqrt(Math.pow(x-(right-(right-left)/2), 2)+Math.pow(y-(bottom-(bottom-top)/2), 2))<=4*R){
				dp = 5;
			}
	 
			return true;
		}
 
	}
 
	
}
