package com.zhao.paipai.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.zhao.paipai.utils.AppEnv;
 
public class RecorderPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final int CAMERA_BACK = 0;
	private static final int CAMERA_FRONT = 1;
	private int curCameraFaceing = CAMERA_BACK;
	private Camera camera = null;
	private Context context = null;
	private CameraCallback cameraCb = new CameraCallback();
	private UiCallback uiCallback = null;
	private MediaRecorder mediaRecorder;
	private boolean isRecording = false;
	
	public RecorderPreview(Context context,UiCallback uicallback) {
		super(context);
		setId(567);
		setTag("0");
		this.context = context;
		this.uiCallback = uicallback;
 
		initCamera(curCameraFaceing);
		getHolder().addCallback(this);
		getHolder().setFixedSize(176, 144);
		
		//setKeepScreenOn(true);
		//setDrawingCacheEnabled(true);
		//setDrawingCacheQuality(SurfaceView.DRAWING_CACHE_QUALITY_HIGH);
		//getHolder().setFormat(PixelFormat.JPEG);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
	}
	
	public void setCamera(Camera camera){
		this.camera = camera;
	}
	
	public Camera getCamera(){
		return this.camera;
	}
	
	private void initCamera(int pos){
		if(!checkCameraHardware()){
			uiCallback.OnError("no camera on this device.");
		}else{
			if(pos==CAMERA_BACK)
				getCameraBack();
			else if(pos==CAMERA_FRONT)
				getCameraFront();	
		}
		
	}
	
	private void getCameraBack(){
		try{
			for(int i=0;i<Camera.getNumberOfCameras();i++){
				CameraInfo info = new CameraInfo();
				Camera.getCameraInfo(i, info);
				if(info.facing==CameraInfo.CAMERA_FACING_BACK){
					camera = Camera.open(i);
					curCameraFaceing  = CAMERA_BACK;
					break;
				}
			}
			if(camera==null){
				getCameraFront();
			}
		}catch(Exception e){
			uiCallback.OnError("connection camera failed:"+e.getMessage());
		}
	}
	
	
	private void getCameraFront(){
		try{
			for(int i=0;i<Camera.getNumberOfCameras();i++){
				CameraInfo info = new CameraInfo();
				Camera.getCameraInfo(i, info);
				if(info.facing==CameraInfo.CAMERA_FACING_FRONT){
					camera = Camera.open(i);
					curCameraFaceing  = CAMERA_FRONT;
					break;
				}
			}
			if(camera==null){
				getCameraBack();
			}
		}catch(Exception e){
			uiCallback.OnError("connection camera failed:"+e.getMessage());
		}
	}
	
	public void switchCamera(){
		int N = Camera.getNumberOfCameras();
		if(N<=0) return;
		curCameraFaceing = curCameraFaceing+1%N;
		surfaceDestroyed(null);
		
		initCamera(curCameraFaceing);
	}
	
	private boolean checkCameraHardware() {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	public void stopPreview(){
		if(camera!=null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
		}
	}
	
	public void startPreview(){
		if(camera!=null) {
			camera.startPreview();
			camera.setPreviewCallback(null);
		}
	}
	
	
	 private void setCameraDisplayOrientation() {
	     CameraInfo info = new CameraInfo();
	             Camera.getCameraInfo(curCameraFaceing, info);
	     WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	     int rotation = wm.getDefaultDisplay().getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }

 
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(this.camera==null){
			initCamera(curCameraFaceing);
		}
		 
		if(this.camera==null){
			uiCallback.OnError("the camera is not opened.");
		}
		else{
			try {
				 
				this.camera.setPreviewDisplay(holder);
				startFaceDetection();
			} catch (IOException e) {
				uiCallback.OnError(e.getMessage());
				e.printStackTrace();
			}
		}
		uiCallback.OnStateChanged(UiCallback.STATE_CAMERA_CREATED,null);
	}

 
	@SuppressLint("NewApi")
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		 
		 if(this.camera==null || holder.getSurface()==null) return;
		 int bestWidth = 0; 
         int bestHeight = 0; 
         
		 Parameters params = camera.getParameters();
		 //params.setPictureFormat(PixelFormat.RGB_565);
		 //params.setJpegQuality(85);
		 //params.setPreviewFpsRange(1000, 1000);
 
         //如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择 
         List<Camera.Size> sizeList = params.getSupportedPreviewSizes(); 
         if(sizeList.size() > 0){ 
             Iterator<Camera.Size> itor = sizeList.iterator(); 
             while(itor.hasNext()){ 
                 Camera.Size cur = itor.next(); 
                 if((cur.width >= bestWidth && cur.height>=bestHeight) && cur.width <= AppEnv.WIN_WIDTH && cur.height <= AppEnv.WIN_HEIGHT){ 
                     bestWidth = cur.width; 
                     bestHeight = cur.height; 
                 }
             } 
              
         }
         //System.out.println("-------width，height-------"+bestWidth+","+bestHeight);
         //holder.setFixedSize(bestWidth, bestHeight);
		 //params.setPreviewSize(bestWidth, bestHeight);
		 //params.setPictureSize(bestWidth, bestHeight);
         //setLayoutParams(new FrameLayout.LayoutParams(bestHeight,bestWidth)); 
		 // check that metering areas are supported
		 /*if (android.os.Build.VERSION.SDK_INT>=14 && params.getMaxNumMeteringAreas() > 0){ 
			    List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

			    Rect areaRect1 = new Rect(-100, -100, 100, 100);    // specify an area in center of image
			    meteringAreas.add(new Camera.Area(areaRect1, 600)); // set weight to 60%
			    Rect areaRect2 = new Rect(800, -1000, 1000, -800);  // specify an area in upper right of image
			    meteringAreas.add(new Camera.Area(areaRect2, 400)); // set weight to 40%
			    params.setMeteringAreas(meteringAreas);
		 }*/
		 System.out.println("-------params=" + params.flatten());
		 try{
		 	this.camera.stopPreview();
		 }catch(Exception e){
			 
		 }
		 
		 try {
			this.camera.getParameters().setPreviewSize(176, 144);
			//this.camera.setParameters(params);
			this.camera.setPreviewDisplay(holder);
			setCameraDisplayOrientation();
			this.camera.autoFocus(cameraCb);
			this.camera.setAutoFocusMoveCallback(cameraCb);
			this.camera.setErrorCallback(cameraCb);
			if(this.camera.getParameters().isSmoothZoomSupported())
				this.camera.setZoomChangeListener(cameraCb);
 		
			camera.setPreviewCallback(null); 
			
			this.camera.startPreview();
			
			startFaceDetection();
			
			uiCallback.OnStateChanged(UiCallback.STATE_CAMERA_CHANGED,"record");
			
		 } catch (IOException e) {
			 uiCallback.OnError(e.getMessage());
			 e.printStackTrace();
			 camera.setPreviewCallback(null);  
			 camera.stopPreview();
			 camera.release();
			 camera=null ;
		 } 
		 
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		releaseMediaRecorder();
		releaseCamera();
		
		uiCallback.OnStateChanged(UiCallback.STATE_CAMERA_DESTROY,null);
	}
 
	public void onDestroy(){
		surfaceDestroyed(null);
		
	}
	
 
	@SuppressLint("NewApi")
	public void startFaceDetection(){
	    // Try starting Face Detection
	    Camera.Parameters params = camera.getParameters();

	    // start face detection only *after* preview has started
	    if (android.os.Build.VERSION.SDK_INT>=14 && params.getMaxNumDetectedFaces() > 0){
	        // camera supports face detection, so can start it:
	        camera.startFaceDetection();
	    }
	}
	
	
	public void recordVideo(){
		if(camera==null){
			uiCallback.OnError("----camera==null.");
			return;
		}
		if(getHolder().getSurface()==null){
			uiCallback.OnError("----surface==null.");
			return;
		}
 
		if(camera==null)  return ;
		 
		if(prepareVideoRecorder()){
			try{
				mediaRecorder.start();
				uiCallback.OnStateChanged(UiCallback.STATE_RECORDER_START, null);
				
			}catch(Exception e){
				uiCallback.OnError("failed to start recording." + e.getMessage());
				uiCallback.OnRecordVideo(UiCallback.STATE_RECORDER_END, null);
			}
			AppEnv.ts("start recording video.");
		}else{
			AppEnv.ts("failed to prepare the recorder.");
		}
			 
	}
	
	private boolean prepareVideoRecorder(){
	  
	    mediaRecorder = new MediaRecorder();
	    mediaRecorder.reset();
	    
	    int preHeight = camera.getParameters().getPreviewSize().height;
	   	int preWidth = camera.getParameters().getPreviewSize().width;
	   	System.out.println("-----perHeight="+preHeight+",preWidth="+preWidth);
	    mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener(){
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				AppEnv.Log("----MediaRecorder.OnError. code="+what+"-"+extra);
			}
	    	
	    });
	    
	    mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener(){
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				AppEnv.Log("----MediaRecorder.OnInfo. code="+what+"-"+extra);
			}
	    	
	    });
	    
	    // Step 1: Unlock and set camera to MediaRecorder
	    camera.unlock();
	    mediaRecorder.setCamera(camera);

	    // Step 2: Set sources
	    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    
	    // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
	    //mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

	    // Set output file format  
	    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);  
	    
	    // 这两项需要放在setOutputFormat之后  
	    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  
	    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
	    mediaRecorder.setVideoSize(320,240);
	    
	    // Step 4: Set output file
	    String vpath = getOutputMediaFile(MEDIA_TYPE_VIDEO).getAbsolutePath();
	    mediaRecorder.setOutputFile(vpath);

	    // Step 4.5: Set the video capture rate to a low number
	    mediaRecorder.setCaptureRate(12);  
	    mediaRecorder.setVideoEncodingBitRate(1*1024*1024);
	    
	    // Step 5: Set the preview output
	    mediaRecorder.setPreviewDisplay(getHolder().getSurface());
	    try {
	    	// Step 6: Prepare configured MediaRecorder
	        mediaRecorder.prepare();
	        uiCallback.OnRecordVideo(UiCallback.STATE_RECORDER_START, vpath);
	    } catch (IllegalStateException e) {
	    	e.printStackTrace();
	        AppEnv.DebugLog("IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	AppEnv.DebugLog("IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch(Exception e){
	    	e.printStackTrace();
	    	releaseMediaRecorder();
	    	return false;
	    }
	    return true;
	}
	
	private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (camera != null){
        	camera.stopPreview();
        	camera.setPreviewCallback(null);
        	camera.release();        // release the camera for other applications
        }
        camera = null;
    }
    
	public void stopRecord(){
		 
		try{
			mediaRecorder.stop();
			releaseMediaRecorder();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			uiCallback.OnStateChanged(UiCallback.STATE_RECORDER_END, null);
			uiCallback.OnRecordVideo(UiCallback.STATE_RECORDER_END,"finished");
		}
		mediaRecorder = null;
	}
	
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int MEDIA_TYPE_VIDEO = 2;
	private File getOutputMediaFile(int type){
	  
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.CHINA).format(System.currentTimeMillis());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(AppEnv.PHOTO_PATH + File.separator +
	        "PIC_"+ timeStamp + ".png");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(AppEnv.PHOTO_PATH + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
 
	@SuppressLint("NewApi")
	class CameraCallback implements  AutoFocusCallback,AutoFocusMoveCallback,
		ErrorCallback,OnZoomChangeListener{
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			AppEnv.Log("------------onAutoFocus------------"+success);
	 	
			
		}

		@Override
		public void onAutoFocusMoving(boolean start, Camera camera) {
			 AppEnv.Log("-----------onAutoFocueMoving--------");
			 
		}

		@Override
		public void onError(int error, Camera camera) {
			AppEnv.Log("------------error----------"+error);
		}
 

		@Override
		public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
			 
		}
		
	}
 
	
	class DecodeYUV420SP extends Thread {
		byte[] yuv420sp ;
		int width,height;
		public DecodeYUV420SP(byte[] yuv420sp,int width,int height){
			this.yuv420sp = yuv420sp;
			this.width = width;
			this.height = height;
		}
		
		public void rotateYUV420SP(byte[] src,byte[] des, int width,int height)
		{  
		  int wh = width * height;
		  //旋转Y
		  int k = 0;
		  for(int i=0;i<width;i++) {
			  for(int j=0;j<height;j++) 
			  {
			       des[k] = src[width*j + i];   
			       k++;
			  }
		  }
		  
		  for(int i=0;i<width;i+=2) {
			   for(int j=0;j<height/2;j++) 
			   { 
	               des[k] = src[wh+ width*j + i]; 
	               des[k+1]=src[wh + width*j + i+1];
			       k+=2;
			   }
		  }
		}
		
		public void decodeYUV420SP(int[] rgb,byte[] rgbBuf, byte[] yuv420sp, int width, int height) {  
	        final int frameSize = width * height;  
	        
	        for (int j = 0, yp = 0; j < height; j++) {
	        	isDecodeYUVing = true;
	            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  
	            for (int i = 0; i < width; i++, yp++) {
	                int y = (0xff & ((int) yuv420sp[yp])) - 16;  
	                if (y < 0) y = 0;  
	                if ((i & 1) == 0) {  
	                    v = (0xff & yuv420sp[uvp++]) - 128;  
	                    u = (0xff & yuv420sp[uvp++]) - 128;  
	                }  
	                  
	                int y1192 = 1192 * y;  
	                int r = (y1192 + 1634 * v);  
	                int g = (y1192 - 833 * v - 400 * u);  
	                int b = (y1192 + 2066 * u);  
	                
	                if (r < 0) r = 0; else if (r > 262143) r = 262143;  
	                if (g < 0) g = 0; else if (g > 262143) g = 262143;  
	                if (b < 0) b = 0; else if (b > 262143) b = 262143; 
	                
	                rgbBuf[yp * 3] = (byte)(r >> 10);  
	                rgbBuf[yp * 3 + 1] = (byte)(g >> 10);  
	                rgbBuf[yp * 3 + 2] = (byte)(b >> 10);  

	                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);  
	                
	            }
	        }
	    }
		
		public void run(){
			AppEnv.Log("----decodeyuv420sp begin----");
			byte[] rgbBuf = new byte[width * height * 3];
			int[] rgb = new int[width * height];
			//byte[] rotate = new byte[yuv420sp.length];
			decodeYUV420SP(rgb,rgbBuf,yuv420sp, width, height);
			File file = getOutputMediaFile(MEDIA_TYPE_IMAGE) ;
			FileOutputStream fos = null;
			Bitmap bm  = null;
			try {
				fos = new FileOutputStream(file);
				bm = Bitmap.createBitmap(rgb, width, height, Config.RGB_565);
				Matrix m = new Matrix();
				m.setRotate(90, bm.getWidth()/2, bm.getHeight()/2);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, false);
				bm.compress(CompressFormat.PNG, 100, fos);
				//fos.write(rgbBuf,0,rgbBuf.length);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				AppEnv.Log("----decodeyuv420sp end----");
				bm.recycle();
				SystemClock.sleep(2);
				//bm.recycle();
				fos = null;
			}
			uiCallback.OnStateChanged(UiCallback.STATE_YUV420SP_DECODED, file.getAbsoluteFile());
			isDecodeYUVing = false;
		}
	}
	
	boolean isDecodeYUVing = false,isTakePhotoing=false;
	class MyPreviewCallback implements PreviewCallback{
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if(data==null || data.length==0) return;
			if(!isTakePhotoing) return ;
			isTakePhotoing = false; 
			isDecodeYUVing = true; 
		    
            int previewWidth = camera.getParameters().getPreviewSize().width;  
            int previewHeight = camera.getParameters().getPreviewSize().height;  
            
            DecodeYUV420SP decode = new DecodeYUV420SP(data, previewWidth, previewHeight);
            decode.start();
            camera.setPreviewCallback(null);
            SystemClock.sleep(50);
            uiCallback.OnTakedPicture(UiCallback.STATE_TAKE_END, null);
		}
		
	}
 
	
}
