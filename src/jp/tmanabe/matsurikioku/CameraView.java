package jp.tmanabe.matsurikioku;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
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
		// do nothing.
	} catch (Exception e) { 
	// do nothing.	
	}
	camera.startPreview();	
	}
}
