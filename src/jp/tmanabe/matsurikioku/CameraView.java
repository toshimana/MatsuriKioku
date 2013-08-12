package jp.tmanabe.matsurikioku;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback {
	private SurfaceHolder holder;
	private Camera camera;
	
	public CameraView(Context context) {
		super(context);
		
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void surfaceCreated(SurfaceHolder holder) {	
		try {
			camera = Camera.open();		
			camera.setDisplayOrientation(90);
			camera.setPreviewDisplay(holder);

			/*
			int cameraNum = Camera.getNumberOfCameras();
			CameraInfo camInfo = new CameraInfo();
			for(int i = 0; i < cameraNum; i++){
				Camera.getCameraInfo(i, camInfo);
				if(camInfo.facing==CameraInfo.CAMERA_FACING_FRONT){
					camera = Camera.open(i);		
					camera.setPreviewDisplay(holder);
					break;
				}
			}
			*/
		} catch (Exception e) {
			// do nothing.
		}
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		camera.startPreview();
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			camera.takePicture(null, null, this);	
		}
		return true;
	}
	
	public void onPictureTaken(byte[] data, Camera camera) {
	try	{
		final String saveDir = Environment.getExternalStorageDirectory().getPath() + "/MatsuriKioku";
		File file = new File(saveDir);
		if (!file.exists()) {
			if (!file.mkdir()) {
				Log.e("Debug", "MakeDir Error");
			}
		}
		
		// convert byteArray to Bitmap.	
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

		// create filepath.
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String imgPath = saveDir + '/' + sdf.format(cal.getTime()) + ".jpg";
		
		FileOutputStream fos;
		try {
			// second argment is that file is appended or not.
			fos = new FileOutputStream(imgPath, true);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.close();
			
			
		} catch (Exception e) {
			Log.e("Error", "" + e.toString());
		}
		
	} catch (Exception e) { 
		// do nothing.	
	}
	camera.startPreview();	
	}

	/**
	 * アンドロイドのデータベースへ画像のパスを登録
	 * @param path 登録するパス
	 * http://androidguide.nomaki.jp/html/device/camera/camFileSave.html
	 */
	private void registAndroidDB(String path) {
	    // アンドロイドのデータベースへ登録
	    // (登録しないとギャラリーなどにすぐに反映されないため)
	    ContentValues values = new ContentValues();
	    ContentResolver contentResolver = this.getContext().getContentResolver();
	    values.put(Images.Media.MIME_TYPE, "image/jpeg");
	    values.put("_data", path);
	    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}
}

