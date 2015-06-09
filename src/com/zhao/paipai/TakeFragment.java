package com.zhao.paipai;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhao.paipai.camera.CameraPreview;
import com.zhao.paipai.camera.RecorderPreview;
import com.zhao.paipai.camera.UiCallback;
import com.zhao.paipai.utils.AppEnv;

public class TakeFragment extends Fragment implements OnClickListener {
 
	private CameraPreview cameraPreview;
	private RecorderPreview recordPreview;
	private RelativeLayout rl_main;
	private Animation takeAnim;
	private ImageButton ib_recordVideo;
	private ImageButton ib_takePhoto;
	private ImageButton ib_switchCamera;
 
	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(com.zhao.paipai.R.layout.fragment_take, null);
		initView(v);
		return v;
	}
	
 
	private void initView(View v) {
		rl_main = (RelativeLayout) v.findViewById(R.id.rl_main);
		fl_preview = (FrameLayout) v.findViewById(R.id.fl_camera_view);
		cameraPreview = new CameraPreview(getActivity(),cameraCallback);
		//preview.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		fl_preview.addView(cameraPreview);
		takeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.takephoto);
		ib_takePhoto = (ImageButton) v.findViewById(R.id.ib_takePic);
		ib_recordVideo = (ImageButton) v.findViewById(R.id.ib_record_video);
		ib_switchCamera = (ImageButton) v.findViewById(R.id.ib_switch);
		ib_recordVideo.setEnabled(false);
		ib_takePhoto.setEnabled(false);
		ib_switchCamera.setEnabled(false);
		ib_takePhoto.setOnClickListener(this);
		ib_recordVideo.setOnClickListener(this);
		ib_switchCamera.setOnClickListener(this);
	  
		if(Camera.getNumberOfCameras()>=1){
			ib_recordVideo.setEnabled(true);
			ib_takePhoto.setEnabled(true);
			ib_takePhoto.setClickable(true);
			ib_switchCamera.setClickable(true);
		}
		if(Camera.getNumberOfCameras()>=2){
			ib_switchCamera.setEnabled(true);
			ib_switchCamera.setClickable(true);
		}else{
			ib_switchCamera.setEnabled(false);
		}
	}
	
	@Override
	public void onClick(View v) {
		 
		if(v.getId()==R.id.ib_takePic){
			String flg = v.getTag()+"";
			if(flg.equals("1")){
				if(cameraPreview==null){
					AppEnv.showLoading();
					createCameraPreview(1);
				}else{
					AppEnv.showLoading();
					cameraPreview.takePhoto();
				}
			}else if(flg.equals("0")){
				if(recordPreview!=null)
					recordPreview.stopRecord();
				ib_recordVideo.setImageResource(R.drawable.video_selector);
				ib_recordVideo.setClickable(true);
				ib_takePhoto.setImageResource(R.drawable.camera_selector);
				ib_takePhoto.setTag("1");
			}
			  
		}else if(v.getId()==R.id.ib_record_video){
			AppEnv.showLoading();
			createRecordPreview(1);
			
		}else if(v.getId()==R.id.ib_switch){
			if(Camera.getNumberOfCameras()>1){
				AppEnv.showLoading();
				cameraPreview.switchCamera();
			}
		}
	}
	
	public void stopPreview(){
		if(cameraPreview!=null){
			cameraPreview.stopPreview();
		}
	}
	
	public void startPreview(){
		if(cameraPreview!=null){
			cameraPreview.startPreview();
		}
	}
	
	private Handler handler = new Handler();
	private UiCallback cameraCallback = new UiCallback(){

		@Override
		public void OnStateChanged(int state,Object o) {
			AppEnv.Log("----camera state----"+state);
			if(state==UiCallback.STATE_TAKE_START){
				AppEnv.showLoading();
			}else if(state==UiCallback.STATE_TAKE_END){
				
			}else if(state==UiCallback.STATE_YUV420SP_DECODED){
				List<Fragment> fgs = ((MainActivity)getActivity()).mFragments;
				((PicsFragment)fgs.get(1)).addPic(new File(String.valueOf(o)));
			}else if(state==UiCallback.STATE_CAMERA_CHANGED){
				AppEnv.hideLoading();
				if(String.valueOf(o).equals("record")){
					if((recordPreview.getTag()+"").equals("1")){
						AppEnv.hideLoading();
						recordPreview.setTag("0");
						recordPreview.recordVideo();
					}
				}else{
					if((cameraPreview.getTag()+"").equals("1")){
						AppEnv.hideLoading();
						cameraPreview.setTag("0");
						SystemClock.sleep(10);
						cameraPreview.takePhoto();
					}
				}
			}else if(state==UiCallback.STATE_RECORDER_START){
				 
			}else if(state==UiCallback.STATE_RECORDER_END){
				createCameraPreview(0);
				timer.cancel();
				timer=null;
				task=null;
			}
			
		}

		@Override
		public void OnError(Object err) {
			AppEnv.Log((String)err);
			AppEnv.ts((String)err);
			if(recordPreview!=null){
				ib_recordVideo.setImageResource(R.drawable.video_recording);
				ib_recordVideo.setTag("0");
				ib_recordVideo.setClickable(false);
				ib_takePhoto.setImageResource(R.drawable.ic_media_stop);
				ib_takePhoto.setTag("0");
				createCameraPreview(0);
			}
		}

		@Override
		public void OnTakedPicture(int state,Bitmap thumb) {
			if(state==UiCallback.STATE_TAKE_END){
				return ;
			}
			AppEnv.showLoading();
			final Bitmap bitmap = thumb;
			getActivity().runOnUiThread(new Runnable(){
				public void run(){
					final ImageView iv = new ImageView(getActivity());
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
					params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
					iv.setLayoutParams(params);
					iv.setBackgroundColor(0x00000000);
					iv.setImageBitmap(bitmap);
					iv.setVisibility(View.VISIBLE);
					iv.invalidate();
					
					iv.clearAnimation();
					rl_main.addView(iv,rl_main.getChildCount()-1);
					rl_main.invalidate();
					
					takeAnim.setAnimationListener(new AnimationListener(){
						
						@Override
						public void onAnimationStart(Animation animation) {
							AppEnv.DebugLog("----------animation start-----------");
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							AppEnv.DebugLog("----------animation end-----------");
							handler.post(new Runnable() {
								public void run() {
									iv.destroyDrawingCache();
									rl_main.removeView(iv);
									bitmap.recycle();
								}
							});
							AppEnv.hideLoading();
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
							
						}
					});
					
					if(takeAnim==null) 
						takeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.takephoto);
					iv.startAnimation(takeAnim);
					
				};
			});
			
		}
		
		private Timer timer = null;
		private TimerTask task = null;
		@Override
		public void OnRecordVideo(int state,String vpath){
			if(state==UiCallback.STATE_RECORDER_END){
				if(timer!=null){
					timer.cancel();
					timer=null;
					task=null;
					handler.post(new Runnable(){
						public void run(){
							ib_recordVideo.setImageResource(R.drawable.video_selector);
							ib_recordVideo.setClickable(true);
							ib_takePhoto.setImageResource(R.drawable.camera_selector);
							ib_takePhoto.setTag("1");
						}
					});
				}
				return;
			}
			timer = new Timer();
			task = new TimerTask(){
				public void run(){
					handler.post(new Runnable(){
						public void run(){
							ib_recordVideo.setImageResource(R.drawable.video_recording);
							ib_recordVideo.setTag("0");
							ib_recordVideo.setClickable(false);
							ib_takePhoto.setImageResource(R.drawable.ic_media_stop);
							ib_takePhoto.setTag("0");
						}
					});
					handler.post(new Runnable(){
						public void run(){
							String flg = ib_recordVideo.getTag()+"";
							if(flg.equals("0")){
								ib_recordVideo.setImageResource(R.drawable.video_recording_2);
								ib_recordVideo.setTag("1");
							}else{
								ib_recordVideo.setImageResource(R.drawable.video_recording);
								ib_recordVideo.setTag("0");
							}
						}
					});
				}
			};
			timer.schedule(task, 0, 300);
			
		}
		
	};
	private FrameLayout fl_preview;
	
  
	public void onDestroy() {
		AppEnv.Log("----takeFragment destory----");
		if(cameraPreview!=null){
			cameraPreview.onDestroy();
		}
		super.onDestroy();
	}

	public void createCameraPreview(final int tag){
		handler.post(new Runnable(){
			public void run(){
				if(recordPreview!=null){
					recordPreview.onDestroy();
					fl_preview.removeAllViews();
					fl_preview.invalidate();
					recordPreview = null;
				}
				if(cameraPreview==null){
					cameraPreview = new CameraPreview(getActivity(),cameraCallback);
					fl_preview.addView(cameraPreview);
					cameraPreview.setTag(""+tag);
				}
			}
		});
	}
	
	public void createRecordPreview(final int tag){
		handler.post(new Runnable(){
			public void run(){
				if(cameraPreview!=null){
					cameraPreview.onDestroy();
					fl_preview.removeAllViews();
					fl_preview.invalidate();
					cameraPreview = null;
				}
				if(recordPreview==null){
					recordPreview = new RecorderPreview(getActivity(),cameraCallback);
					fl_preview.addView(recordPreview);
					recordPreview.setTag(""+tag);
				}
			}
		});
	}

	
	
	 
	
}
