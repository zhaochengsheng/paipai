package com.zhao.paipai;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.zhao.paipai.provider.DeleteActionProvider;
import com.zhao.paipai.provider.EditActionProvider;
import com.zhao.paipai.provider.PhotoActionProvider;
import com.zhao.paipai.provider.ShareActionProvider;
import com.zhao.paipai.utils.AppEnv;

public class MainActivity extends SlidingFragmentActivity
{

	public ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	public List<Fragment> mFragments = new ArrayList<Fragment>();
	private Menu menu = null;
	private int curFrgIdx = 0;
	private SlidingMenu slidingMenu;
	private ProgressBar pb_loading;
	private View vi_mask;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		System.out.println("-------------onCreate-------------");
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_main);
		AppEnv.init(this);
		AppEnv.showLoading();
		// 初始化Actionbar
		initActionBar();
		// 初始化SlideMenu
		initLeftMenu();
		// 初始化ViewPager
		initViewPager();

		AppEnv.hideLoading();
	}

	private void initActionBar(){
		if(getSupportActionBar()!=null){
			getSupportActionBar().setDisplayShowCustomEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}else{
			AppEnv.Log("----no actionbar----");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
 
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			break;
		case R.id.menu_share_hide:
			ShareActionProvider share = (ShareActionProvider) item.getActionProvider();
			share.performAction();
			break;
			
		case R.id.menu_photos_hide:
			PhotoActionProvider photos = (PhotoActionProvider) item.getActionProvider();
			photos.performAction();
			break;
			
		case R.id.menu_delete_hide:
			DeleteActionProvider delete = (DeleteActionProvider) item.getActionProvider();
			delete.performAction();
			break;
		case R.id.menu_edit_hide:
			EditActionProvider edit = (EditActionProvider) item.getActionProvider();
			edit.setData(((PicsFragment)mFragments.get(1)).getCurPicture());
			edit.performAction();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart() {
		AppEnv.loading = pb_loading;
		super.onStart();
	}
 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(menu!=null) {
			this.menu = menu;
			this.menu.clear();
		}
		if(curFrgIdx==0){
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			getSupportMenuInflater().inflate(R.menu.menu1, menu);
			
		}else{
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			getSupportMenuInflater().inflate(R.menu.menu2, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	private void initViewPager()
	{
		pb_loading = (ProgressBar)findViewById(R.id.pb_main_loading);
		vi_mask = findViewById(R.id.vi_mask);
		mViewPager = (ViewPager) findViewById(R.id.vp_pager);
		//mViewPager.setLayoutParams(new LinearLayout.LayoutParams(WIDTH,HEIGHT));
		Fragment tab01 = new TakeFragment();
		Fragment tab02 = new PicsFragment();
		mFragments.add(tab01);
		mFragments.add(tab02);
  
		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
		{
			@Override
			public int getCount()
			{
				return mFragments.size();
			}

			@Override
			public Fragment getItem(int idx)
			{
				return mFragments.get(idx);
			}
		};
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int state) {}

			@Override
			public void onPageScrolled(int position, float percent, int px) {}

			@Override
			public void onPageSelected(int position) {
				curFrgIdx = position;
				onCreateOptionsMenu(menu);
				if(position==0){
					((TakeFragment)mFragments.get(0)).startPreview();
				}else if(position==1){
					((TakeFragment)mFragments.get(0)).stopPreview();
				}
			}
			
		});
	}

	private void initLeftMenu()
	{
		setBehindContentView(R.layout.left_menu);
		Fragment leftMenuFragment = new LeftFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.left_menu_frame, leftMenuFragment).commit();
		SlidingMenuListener slidinglst = new SlidingMenuListener();
		slidingMenu = getSlidingMenu();
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setOnOpenListener(slidinglst);
		slidingMenu.setOnOpenedListener(slidinglst);
		slidingMenu.setOnCloseListener(slidinglst);
		slidingMenu.setOnClosedListener(slidinglst);
		//slidingMenu.setMenu(R.layout.left_menu);
		// 设置触摸屏幕的模式
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
	 
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// 设置渐入渐出效果的值
		slidingMenu.setFadeDegree(0.35f);
 
	}
	
	Handler handler = new Handler();
	class SlidingMenuListener implements OnOpenListener,OnOpenedListener,OnCloseListener,OnClosedListener{

		@Override
		public void onOpen() {
		 
			 handler.post(new Runnable(){
				 public void run(){
					 vi_mask.setVisibility(View.VISIBLE);
				 }
			 });
		}
		
		@Override
		public void onOpened() {
			System.out.println("--------------onopened--------------");
			
		}
		
		@Override
		public void onClose() {
			 
			System.out.println("--------------onclose--------------");
			
		}

		@Override
		public void onClosed() {
			handler.post(new Runnable(){
				 public void run(){
					 vi_mask.setVisibility(View.GONE);
				 }
			 });
		}

	}
 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	 
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==10){
			((PicsFragment)mFragments.get(1)).isEditReturn = true;
			if(resultCode==Activity.RESULT_OK && data!=null){
				String savepath = data.getStringExtra("savepath");
				if(TextUtils.isEmpty(savepath)) return;
				((PicsFragment)mFragments.get(1)).addPic(new File(savepath));
			}
		}else{
			
		}
	}
	
	class MyActionModeCallback implements ActionMode.Callback{

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			AppEnv.Log("------------onCreateActionMode----------");
			//MainActivity.this.getSupportMenuInflater().inflate(R.menu.menu1, menu);
			mode.setTitle("拍照……");
			/*TextView view = new TextView(MainActivity.this);
			view.setText("照相中……");
			//view.setGravity(Gravity.CENTER);
			view.setVisibility(View.VISIBLE);
			mode.setCustomView(view);
			*/ 
			
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			AppEnv.Log("------------onActionItemClick----------");
			/*if(item.getActionProvider() instanceof MyActionProvider){
				MyActionProvider provider = (MyActionProvider) item.getActionProvider();
				provider.performAction();
			}*/
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			 
		}
		
	}
	
	
}

