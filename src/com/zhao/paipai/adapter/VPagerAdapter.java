package com.zhao.paipai.adapter;

import java.io.File;
import java.util.List;

import com.zhao.paipai.utils.AppEnv;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class VPagerAdapter extends PagerAdapter{
	private List<File> list = null;
	private Context context;
	
	public VPagerAdapter(Context context,List<File> list){
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
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0==arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View)object);
		//super.destroyItem(container, position, object);
	}
 
 
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(list.size()==0) return null ;
		int pos = position % list.size();
		File file = list.get(pos);
		ImageView iv = new ImageView(context);
		iv.setBackgroundColor(0x88000000);
		iv.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		iv.setLayoutParams(params); 
		container.addView(iv);
		return iv;  
		 
	}
	
	private int childCnt = 0;
	@Override
    public void notifyDataSetChanged() {         
          childCnt = getCount();
          super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object)   {          
          if (childCnt > 0) {
	          childCnt--;
	          return POSITION_NONE;
          }
          return super.getItemPosition(object);
    }
 
	
}
