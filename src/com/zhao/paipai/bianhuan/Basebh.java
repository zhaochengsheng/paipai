package com.zhao.paipai.bianhuan;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

public abstract class Basebh extends View {

	public Basebh(Context context) {
		super(context);
	}
	public Basebh(Context context,AttributeSet attr) {
		super(context,attr);
	}
	
	public abstract void resize(int l,int t,int r,int b);
	public abstract Bitmap perform(int radiua);
	

}
