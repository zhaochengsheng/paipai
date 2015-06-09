package com.zhao.paipai.adapter;

import java.io.File;
import java.util.List;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
 
@SuppressWarnings("deprecation")
public class PictureAdapter extends BaseAdapter{
	
	private Context context;
	private List<File> list;
	public PictureAdapter(Context context,List<File> list){
		this.context = context;
		this.list = list;
	}
 
	@Override
	public int getCount() {
		if(list.size()==0) return 0 ;
		//return Integer.MAX_VALUE;
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		if(list.size()==0) return null ;
		int pos = position % list.size();
		return list.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(list.size()==0) return null ;
		int pos = position % list.size();
		File file = list.get(pos);
		ImageView iv = new ImageView(context);
		iv.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
		//iv.setScaleType(ImageView.ScaleType.FIT_XY);  
		LayoutParams params = new Gallery.LayoutParams(82, 82);
		iv.setLayoutParams(params);  
		iv.setPadding(2, 2, 2, 2);
		iv.setBackgroundColor(0x66000000);
		iv.setId(position);
		return iv;  
	}
	
}
