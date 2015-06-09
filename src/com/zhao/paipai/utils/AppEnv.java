package com.zhao.paipai.utils;

import java.io.File;

import com.zhao.paipai.R;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AppEnv {
	public final static boolean DEBUG = true;
	public static Activity AppContext = null;
	public static Toast toast = null;
	public static String PHOTO_PATH = ""; 
	public static int WIN_WIDTH;
	public static int WIN_HEIGHT;
	public static ProgressBar loading = null;
	
	public static void init(Activity context){
		AppEnv.AppContext = context;
		AppEnv.WIN_WIDTH = context.getWindowManager().getDefaultDisplay().getWidth();
		AppEnv.WIN_HEIGHT = context.getWindowManager().getDefaultDisplay().getHeight();
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera";
			File tmp = new File(path);
			if(!tmp.exists()) tmp.mkdirs();
			AppEnv.PHOTO_PATH = tmp.getAbsolutePath();
		}else{
			AppEnv.PHOTO_PATH = "mnt/sdcard/DCIM/Camera";
			File tmp = new File(AppEnv.PHOTO_PATH);
			if(!tmp.exists()){
				tmp.mkdirs();
			}
		}
		System.out.println("------------PHOTO_PATH="+PHOTO_PATH);
	}
	
	public static void DebugLog(String log){
		if(AppEnv.DEBUG && !TextUtils.isEmpty(log))  
		System.out.println(log);
	}
	
	public static void Log(String log){
		if(!TextUtils.isEmpty(log)) 
		System.out.println(log);
	}
	
	
	
	public static void DebugLog(String TAG,String msg){
		if(AppEnv.DEBUG && !TextUtils.isEmpty(msg))
		android.util.Log.d(TAG, msg);
		 
	}
	
	public static void Log(String TAG,String msg){
		if(!TextUtils.isEmpty(msg))
		android.util.Log.d(TAG, msg);
	}
	
	public static void ts(int resId){
		ts(AppContext.getString(resId));
	}
	
	public static void ts(String msg){
		if(msg==null || msg.length()==0) return;
		
		if(toast!=null) {
			toast.setText(msg);
		}else{
			toast = Toast.makeText(AppContext, msg, Toast.LENGTH_SHORT);
		}
		toast.show();
	}
	
	public static void showLoading(){
		if(loading!=null){
			loading.setVisibility(View.VISIBLE);
		}
	}
	
	public static void hideLoading(){
		if(loading!=null){
			loading.setVisibility(View.GONE);
		}
	}
	
	
}
