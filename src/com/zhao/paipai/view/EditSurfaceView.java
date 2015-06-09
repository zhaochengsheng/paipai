package com.zhao.paipai.view;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.zhao.paipai.utils.AppEnv;

public class EditSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
 
	//private Context context;
	private String filename;
	private Paint paint;
	private boolean isSaved = true;
	private List<Bitmap> opStack = new ArrayList<Bitmap>(1);
	private UiCallback callback ;
	public EditSurfaceView(Context context,String filename) {
		super(context);
		//this.context = context;
		this.setId(1000);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		setLayoutParams(params);
		//setBackgroundColor(0xFFFFFF);
		this.filename = filename;
		this.callback = (UiCallback)context;
		Bitmap bm = BitmapFactory.decodeFile(filename);
		opStack.add(bm);
		this.paint = new Paint();
		this.paint.setColor(0xFF000000);
		getHolder().addCallback(this);
	}
	
	public boolean isSaved(){
		return isSaved;
	}
	
	public Bitmap getOpBitmap(){
		return opStack.get(opStack.size()-1);
	}
	
	public void save(){
		if(isSaved) return;
		String ext = filename.substring(filename.lastIndexOf("."));
		String savePath = filename.replace(ext, "")+"_1"+ext;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(savePath,true);
			Bitmap bm = opStack.get(opStack.size()-1);
			bm.compress(CompressFormat.PNG, 100, fos);
			fos.close();
			isSaved = true;
			callback.OnStateChanged(UiCallback.STATE_SAVED, savePath);
			AppEnv.ts("success to save.");
		} catch (Exception e) {
			AppEnv.ts("failed to save."+e.getMessage());
			e.printStackTrace();
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
	}
 
	public boolean revert(){
		if(opStack.size()>1){
			AppEnv.showLoading();
			opStack.remove(opStack.size()-1);
			callback.OnStateChanged(UiCallback.STATE_OP_REMOVE, opStack.size());
			draw();
			return true;
		}else{
			return true;
		}
		
	}
	
	public void addOp(Bitmap bm){
		isSaved = false;
		opStack.add(bm);
		callback.OnStateChanged(UiCallback.STATE_OP_ADD, opStack.size());
		draw();
	}
 
	public void draw(){
		if(getHolder()==null || getHolder().getSurface()==null) return;
		Canvas canvas = null ;
		try{
			canvas =  getHolder().lockCanvas();
			AppEnv.Log("----editor draw----");
			Bitmap bm = opStack.get(opStack.size()-1);
			Matrix m = new Matrix();
			m.setTranslate((getWidth()-bm.getWidth())/2, (getHeight()-bm.getHeight())/2) ;
			canvas.drawBitmap(bm, m, paint);
		}finally{
			if(canvas!=null)
			getHolder().unlockCanvasAndPost(canvas);
			AppEnv.hideLoading();
			
		}
	}
	
  
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		 callback.OnStateChanged(UiCallback.STATE_SURFACE_CREATED, null);
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		draw();
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		callback.OnStateChanged(UiCallback.STATE_SURFACE_DESTROY, null);
		
	}

}
