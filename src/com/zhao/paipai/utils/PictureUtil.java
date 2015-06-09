package com.zhao.paipai.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;

public class PictureUtil {
	private static final String TAG = "PictureUtil";
	public static final String insertImage(Bitmap bm,File dest) {
		FileOutputStream fos = null;
		String url = null;
		try {
			// 先保存相片
			fos = new FileOutputStream(dest);
			Matrix m = new Matrix();
			m.setRotate(90, bm.getWidth()/2, bm.getHeight()/2);
			bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, false);
			bm.compress(CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
			// 其次把文件插入到系统图库
		    try {
		        url = MediaStore.Images.Media.insertImage(AppEnv.AppContext.getContentResolver(),
						dest.getAbsolutePath(), dest.getName(), null);
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		    }
		    // 最后通知图库更新
		    AppEnv.AppContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(url)));
		    
		} catch (Exception e) {
			AppEnv.Log(TAG, e.getMessage());
			e.printStackTrace();
		}finally{
			SystemClock.sleep(2);
			bm.recycle();
			fos = null;
		}
		
		return url;
	}
	
	
}
