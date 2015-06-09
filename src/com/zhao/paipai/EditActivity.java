package com.zhao.paipai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zhao.paipai.bianhuan.Basebh;
import com.zhao.paipai.bianhuan.Crop;
import com.zhao.paipai.provider.CancelActionProvider;
import com.zhao.paipai.provider.CloseActionProvider;
import com.zhao.paipai.provider.SaveActionProvider;
import com.zhao.paipai.utils.AppEnv;
import com.zhao.paipai.view.EditSurfaceView;
import com.zhao.paipai.view.UiCallback;

public class EditActivity extends SherlockActivity implements OnClickListener,OnTouchListener,UiCallback {
	
	private EditSurfaceView editor;
	private FrameLayout fl_preview;
	private TextView tv_select_operation;
	private BroadcastReceiver editReceiver ;
	private ProgressBar pb_loading;
	private View curEditView = null;
	private RelativeLayout rl_confirm; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_edit);
		 
		initView();
	  
		initActionBar();
		
		
	}
	
	private void initView() {
		fl_preview = (FrameLayout) findViewById(R.id.fl_view);
		rl_confirm = (RelativeLayout) findViewById(R.id.rl_confirm);
		pb_loading = (ProgressBar) findViewById(R.id.pb_edit_loading);
		tv_select_operation = (TextView) findViewById(R.id.tv_select_operation);
		tv_select_operation.setOnClickListener(this);
		
		String filename = getIntent().getExtras().getString("picture");
		if(TextUtils.isEmpty(filename)){
			AppEnv.ts("the picture cann't found .");
			finish();
		}else{
			editor = new EditSurfaceView(this,filename);
			fl_preview.addView(editor,0);
			editor.setOnClickListener(this);
		}
		findViewById(R.id.iv_cfm_cancel).setOnTouchListener(this);
		findViewById(R.id.iv_cfm_ok).setOnTouchListener(this);
	}

	private void initActionBar(){
		if(getSupportActionBar()!=null){
			getSupportActionBar().setDisplayShowCustomEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			getSupportActionBar().setHomeButtonEnabled(false);
		}else{
			AppEnv.Log("----no actionbar----");
		}
	}
	
	@Override
	protected void onStart() {
		initReceiver();
		AppEnv.loading = pb_loading;
		super.onStart();
	}
 
	@Override
	protected void onStop() {
		if(editReceiver!=null){
			unregisterReceiver(editReceiver);
		}
		editReceiver = null;
		super.onPause();
	}
	
	private void initReceiver(){
		if(editReceiver==null){
			editReceiver = new EditBroadcastReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(EditBroadcastReceiver.ACTION_SAVE);
			filter.addAction(EditBroadcastReceiver.ACTION_REVERT);
			filter.addAction(EditBroadcastReceiver.ACTION_CLOSE);
			registerReceiver(editReceiver, filter);
		}
	}
	
	void exit(){
		if(editor.isSaved()){
			finish();
		}else{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setMessage("尚未保存，是否关闭？");
			alert.setPositiveButton("确定",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handler.post(new Runnable(){
						public void run(){
							finish();
						}
					});
				}
			});
			alert.setNegativeButton("取消", null);
			alert.show();
		}
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case R.id.menu_save_hide:
			SaveActionProvider save = (SaveActionProvider) item.getActionProvider();
			save.performAction();
			break;
		case R.id.menu_cancel_hide:
			CancelActionProvider cancel = (CancelActionProvider) item.getActionProvider();
			cancel.performAction();
			break;
		case R.id.menu_close_hide:
			CloseActionProvider close = (CloseActionProvider) item.getActionProvider();
			close.performAction();
			break;
		}
		 
		return super.onOptionsItemSelected(item);
	}
	
	PopupWindow popwin = null;
	void showPopwindow(){
		closePopwindow();
		popwin = new PopupWindow(this);
		int[] loc = new int[2];
		tv_select_operation.getLocationInWindow(loc);
		View view = getLayoutInflater().inflate(R.layout.popup_win,null);
		view.findViewById(R.id.tv_bianhuan).setOnClickListener(this);
		view.findViewById(R.id.tv_fengge).setOnClickListener(this);
		view.findViewById(R.id.tv_xiaoguo).setOnClickListener(this);
		popwin.setContentView(view);
		popwin.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		popwin.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		popwin.showAtLocation(tv_select_operation, Gravity.NO_GRAVITY, loc[0]+20, loc[1]-32);
		//popwin.showAsDropDown(tv_select_operation);
		handler.sendEmptyMessageDelayed(MESSAGE_HIDE_POP, 3*1000);
	}
	
	void  closePopwindow(){
		if(popwin!=null){
			popwin.dismiss();
		}
		popwin = null;
	}
 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu3, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
 

	@Override
	public void onClick(View v) {
		 if(v.getId()==R.id.tv_select_operation){
			 if(rl_confirm.getVisibility()==View.VISIBLE) return;
			 if(popwin==null){
				 showPopwindow();
			 }else{
				 closePopwindow();
			 }
		 }else if(v.getId()==R.id.tv_bianhuan){
			 closePopwindow();
			 tv_select_operation.setText(((TextView)v).getText()+"");
		 }
		 else if(v.getId()==R.id.tv_fengge){
			closePopwindow();
			tv_select_operation.setText(((TextView)v).getText()+"");
		 }
		 else if(v.getId()==R.id.tv_xiaoguo){
			 closePopwindow();
			 tv_select_operation.setText(((TextView)v).getText()+"");
		 }else if(v.getId()==1000){
			 closePopwindow();
		 }else if(v.getId()==R.id.iv_cfm_cancel){
			 rl_confirm.setVisibility(View.GONE);
			 clearEditView();
		 }else if(v.getId()==R.id.iv_cfm_ok){
			 if(curEditView instanceof Basebh){
				 Basebh bh = ((Basebh)curEditView);
				 bh.perform(0);
			 }
			 clearEditView();
		 }else if(v.getId()==R.id.ib_crop){
			 clearEditView();
			 curEditView = new Crop(this,editor.getOpBitmap(),bhCallback);
			 fl_preview.addView(curEditView, 1);
			 
		 }
  
	}
	
	private com.zhao.paipai.bianhuan.UiCallback bhCallback = new com.zhao.paipai.bianhuan.UiCallback(){

		@Override
		public void OnStateChanged(int state, Object o) {
			if(state==com.zhao.paipai.bianhuan.UiCallback.STATE_CHANGED){
				rl_confirm.setVisibility(View.VISIBLE);
			}else if(state==com.zhao.paipai.bianhuan.UiCallback.STATE_PERFORMED){
				rl_confirm.setVisibility(View.GONE);
				editor.addOp((Bitmap)o);
			}
		}

		@Override
		public void OnError(String err) {
			 AppEnv.ts(err);
		}
		
	};
	
	void clearEditView(){
		if(curEditView!=null){
			fl_preview.removeView(curEditView);
			fl_preview.invalidate();
		}
		curEditView = null;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public static final int MESSAGE_SAVE = 1;
	public static final int MESSAGE_REVERT = 2;
	public static final int MESSAGE_CLOSE = 3;
	public static final int MESSAGE_HIDE_POP = 4;
	public static final int MESSAGE_HIDE_MENU = 5;
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what==MESSAGE_SAVE){
				if(editor!=null){
					editor.save();
				}
			}else if(msg.what==MESSAGE_REVERT){
				if(editor!=null){
					editor.revert();
				}
			}else if(msg.what==MESSAGE_CLOSE){
				exit();
			}else if(msg.what==MESSAGE_HIDE_POP){
				closePopwindow();
			}
		}
	};
	
	public class EditBroadcastReceiver extends BroadcastReceiver{
		public static final String ACTION_SAVE = "EDITOR_SAVE";
		public static final String ACTION_REVERT = "EDITOR_REVERT";
		public static final String ACTION_CLOSE = "EDITOR_CLOSE";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			 if(ACTION_SAVE.equals(intent.getAction())){
				 handler.obtainMessage(MESSAGE_SAVE).sendToTarget();
				 
			 }else if(ACTION_REVERT.equals(intent.getAction())){
				 handler.obtainMessage(MESSAGE_REVERT).sendToTarget();
				 
			 }else if(ACTION_CLOSE.equals(intent.getAction())){
				 handler.obtainMessage(MESSAGE_CLOSE).sendToTarget();
			 }else{
				 
			 }
		}
		
	}

	
	
	//editor callback-----
	@Override
	public void OnStateChanged(int state, Object o) {
		if(state==UiCallback.STATE_SAVED){
			Intent intent = new Intent();
			intent.putExtra("savepath", (String)o);
			setResult(Activity.RESULT_OK, intent);
		}
		
	}

	@Override
	public void OnError(String err) {
		 AppEnv.ts(err);
	}
	//end ------------
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		 
		return false;
	}
 
}
