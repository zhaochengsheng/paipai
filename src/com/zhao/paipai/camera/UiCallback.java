package com.zhao.paipai.camera;

import android.graphics.Bitmap;

public interface UiCallback{
	public final static int STATE_CAMERA_CREATED = 0;
	public final static int STATE_CAMERA_CHANGED = 1;
	public final static int STATE_TAKE_START = 2;
	public final static int STATE_TAKE_END = 3;
	public final static int STATE_YUV420SP_DECODED = 4;
	public final static int STATE_CAMERA_DESTROY = 5;
	public final static int STATE_RECORDER_START = 6;
	public final static int STATE_RECORDER_END = 7;
	
	public void OnStateChanged(int state,Object o);
	public void OnError(Object err);
	public void OnTakedPicture(int state,Bitmap thumb);
	public void OnRecordVideo(int state,String vpath);
}