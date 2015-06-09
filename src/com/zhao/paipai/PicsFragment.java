package com.zhao.paipai;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.CustViewPager;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import com.zhao.paipai.adapter.PictureAdapter;
import com.zhao.paipai.adapter.VPagerAdapter;
import com.zhao.paipai.utils.AppEnv;

@SuppressWarnings("deprecation")
public class PicsFragment extends Fragment implements OnItemClickListener,ViewPager.OnPageChangeListener{
 
	private Gallery ga_gallery;
	private CustViewPager vp_pager;
	public PictureAdapter picada;
	public VPagerAdapter pgada;
	public boolean isEditReturn = false;
	private View preView = null;
	private int curPicIdx = -1;
	private GestureDetector gesture = new GestureDetector(new MyGestureListener());
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(com.zhao.paipai.R.layout.fragment_pics, null);
		initView(v);
		return v;
	}
 
	private void initView(View v) {
		ga_gallery = (Gallery) v.findViewById(R.id.ga_gallery);
		vp_pager = (CustViewPager) v.findViewById(R.id.vp_pager);
		vp_pager.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		picada = new PictureAdapter(getActivity(),getList());
		ga_gallery.setOnItemClickListener(this);
		ga_gallery.setAdapter(picada);
		
		pgada = new VPagerAdapter(getActivity(),getList());
		vp_pager.setOnPageChangeListener(this);
		 
		vp_pager.setClickable(true);
		vp_pager.setAdapter(pgada);
		vp_pager.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gesture.onTouchEvent(event);
			}
			
		});
		
		setCurPicture(0, 0);
	}
 
	@SuppressLint("Recycle")
	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		if(preView!=null){
			preView.setBackgroundColor(0x66000000);
		}
		if(view!=null){
			view.setBackgroundColor(0xff778899);
			pgada.instantiateItem(vp_pager, position);
			vp_pager.setCurrentItem(position, true);
			preView = view;
		}else{
			view =(View) parent.findViewById(position);
			if(view!=null){
				view.setBackgroundColor(0xff778899);
				preView = view;
			}else{
				if(preView!=null)
				preView.setBackgroundColor(0xff778899);
			}
			
			if((position > 0 && position<picada.getCount() && (parent.findViewById(position+1)==null || parent.findViewById(position-1)==null))){
				MotionEvent e1 = MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(),MotionEvent.ACTION_DOWN ,100, 50, 0);
				MotionEvent e2 = MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(),MotionEvent.ACTION_DOWN ,200, 50, 0);
				ga_gallery.onFling(e1, e2, 1300*id, 0);
			}
			
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int position) {}

	@Override
	public void onPageScrolled(int pos, float percent, int px) {}

	@Override
	public void onPageSelected(int position) { 
		 curPicIdx = position;
		 picada.getView(position, null, ga_gallery);
	}

	
	private java.util.ArrayList<File> list = null;
	private void loadList(){
		File tmp = new File(AppEnv.PHOTO_PATH);
		File[] fs = tmp.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if(file.getName().startsWith("PIC_"))
					return true;
				return false;
			}
		});
		 
		if(list==null){
			list = new ArrayList<File>();
		}
		for(File f:fs){list.add(f);}
		Collections.sort(list, new Comparator<File>(){
			@Override
			public int compare(File l, File r) {
				return -l.getName().compareToIgnoreCase(r.getName());
			}
		});
		 
	}
	
	private Handler handler = new Handler();
	public void addPic(final File file){
		synchronized (this) {
			handler.post(new Runnable(){
				public void run(){
					for(File f :list){
						if(f.getName().equalsIgnoreCase(file.getName())){
							list.remove(f);
							break;
						}
					}
					list.add(0,file);
					picada.notifyDataSetChanged();
					pgada.notifyDataSetChanged();
					curPicIdx++;
					setCurPicture(curPicIdx,0);
				}
			});
		}
	}
	
	public void delPic(){
		if(isEditReturn) {
			isEditReturn = false;
			return;
		}
		synchronized (this) {
			if(curPicIdx<list.size()){
				handler.post(new Runnable(){
					public void run(){
						File tmp = list.get(curPicIdx);
						list.remove(curPicIdx);
						picada.notifyDataSetChanged();
						pgada.notifyDataSetChanged();
						curPicIdx = curPicIdx<list.size()?curPicIdx:curPicIdx>0?curPicIdx-1:-1;
						if(curPicIdx>-1){
							setCurPicture(curPicIdx, 0);
						}
						if(tmp!=null){
							tmp.delete();
							AppEnv.ts("success to delete the picture.");
						}
					}
				});
			}else{
				AppEnv.ts("failed to delete the picture.");
			}
		}
		
	}
	
	private void setCurPicture(int idx,int v){
		synchronized (this) {
			curPicIdx = idx;
			final int val = v;
			handler.post(new Runnable(){
				public void run(){
					vp_pager.setCurrentItem(curPicIdx, true);
					onItemClick(ga_gallery,null,curPicIdx,val);
				}
			});
		}
	}
	
	
	public String getCurPicture(){
		synchronized (this) {
			if(curPicIdx<list.size()){
				return list.get(curPicIdx).getAbsolutePath();
			}else{
				setCurPicture(0, 0);
				return list.get(0).getAbsolutePath();
			}
		}	
	}
	
	private ArrayList<File> getList(){
		if(list==null || list.size()==0){
			loadList();
		}
		return this.list;
	}
	
	class MyGestureListener extends SimpleOnGestureListener{
		 
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			AppEnv.Log("---------onScroll-------------");
			if(e1.getRawX()>=AppEnv.WIN_WIDTH*2/5 && e2.getRawX()>=AppEnv.WIN_WIDTH*2/5 ){
				if(distanceX>8){
					curPicIdx = vp_pager.getCurrentItem()<pgada.getCount()?(vp_pager.getCurrentItem()+1):vp_pager.getCurrentItem();
					setCurPicture(curPicIdx,-1);
				}else if(distanceX<-8){
					curPicIdx = vp_pager.getCurrentItem()>0?(vp_pager.getCurrentItem()-1):0;
					setCurPicture(curPicIdx,1);
				}
			}
			return false;
		}

		
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			AppEnv.Log("---------onSingleTapUp-------------");
			return true;
		}



		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			AppEnv.Log("---------onFling-------------");
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			AppEnv.Log("---------onDoubleTop-------------");
			return true;
		}
		 
	}
 
}
