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
import android.graphics.PixelFormat;
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
import com.zhao.paipai.utils.PictureUtil;
 
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final int CAMERA_BACK = 0;
	private static final int CAMERA_FRONT = 1;
	private int curCameraFaceing = CAMERA_BACK;
	private Camera camera = null;
	private Context context = null;
	private CameraCallback cameraCb = new CameraCallback();
	private UiCallback uiCallback = null;
  
	public CameraPreview(Context context,UiCallback uicallback) {
		super(context);
		setId(789);
		setTag("0");
		this.context = context;
		this.uiCallback = uicallback;
 
		initCamera(curCameraFaceing);
		getHolder().addCallback(this);
		//setKeepScreenOn(true);
		//setDrawingCacheEnabled(true);
		//setDrawingCacheQuality(SurfaceView.DRAWING_CACHE_QUALITY_HIGH);
		//getHolder().setFixedSize(320, 240);
		/*
		getHolder().setFormat(PixelFormat.JPEG);*/
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
		 uiCallback.OnStateChanged(UiCallback.STATE_CAMERA_CHANGED,"camera");
		 if(this.camera==null || holder.getSurface()==null) return;
		 int bestWidth = 0; 
         int bestHeight = 0; 
         
		 Parameters params = camera.getParameters();
		 camera.getParameters().setPictureFormat(PixelFormat.RGB_565);
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
 
		releaseCamera();
		
		uiCallback.OnStateChanged(UiCallback.STATE_CAMERA_DESTROY,null);
	}
	
	public void takePhoto(){
		if(camera==null){
			uiCallback.OnError("----camera==null.");
			return;
		}
		
		if(getHolder().getSurface()==null){
			uiCallback.OnError("----surface==null.");
			return;
		}
  
		uiCallback.OnStateChanged(UiCallback.STATE_TAKE_START, null);
		camera.setPreviewCallback(new MyPreviewCallback());
		isTakePhotoing = true;
		camera.takePicture(null,null,cameraCb);
		
	}
	
	public void onDestroy(){
		surfaceDestroyed(null);
		
	}
	
 
	public void startFaceDetection(){
	    // Try starting Face Detection
	    Camera.Parameters params = camera.getParameters();

	    // start face detection only *after* preview has started
	    if (android.os.Build.VERSION.SDK_INT>=14 && params.getMaxNumDetectedFaces() > 0){
	        // camera supports face detection, so can start it:
	        camera.startFaceDetection();
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
 
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int MEDIA_TYPE_VIDEO = 2;
	private File getOutputMediaFile(int type){
	  
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.CHINA).format(System.currentTimeMillis());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(AppEnv.PHOTO_PATH + File.separator +
	        "PIC_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(AppEnv.PHOTO_PATH + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
 
	class CameraCallback implements ShutterCallback,PictureCallback,
		AutoFocusCallback,AutoFocusMoveCallback,ErrorCallback,OnZoomChangeListener{

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			
			 if(data==null || data.length==0) {
				 uiCallback.OnError("can not take a photo. data=null");
				 return;
			 } 
				 
			 try{
				 Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				 Matrix m = new Matrix();
				 m.setRotate(90, bm.getWidth()/2, bm.getHeight()/2);
				 Bitmap outThumb = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),m,true);
				 
				 uiCallback.OnTakedPicture(UiCallback.STATE_TAKE_START,outThumb);
				 
				 SystemClock.sleep(10);
				 camera.startPreview();
				 camera.autoFocus(cameraCb);
				 
			 }catch(Exception e){
				 uiCallback.OnError("takePhoto error:"+e.getMessage());
			 }
			 
		}

		@Override
		public void onShutter() {
		 
			AppEnv.Log("------------onShutter------------");
		}

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
			try {
				Bitmap bm = Bitmap.createBitmap(rgb, width, height, Config.RGB_565);
				PictureUtil.insertImage(bm, file);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				AppEnv.Log("----decodeyuv420sp end----");
				SystemClock.sleep(2);
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
