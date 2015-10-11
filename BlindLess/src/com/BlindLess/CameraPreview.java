package com.BlindLess;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CommandCamera onTakePic;
    private boolean initTouch = true;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    
	private boolean cameraConfigured = false;
	
	public CameraPreview(Context context, Camera camera, CommandCamera callbackOnTakePic) {
        super(context);
        mCamera = camera;
        onTakePic = callbackOnTakePic;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        this.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (initTouch) {
					mCamera.takePicture(null, null, mPictureCallback);
					initTouch = false;
				}
				return false;
			}		

		});   
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Error setting camera preview: " + e.getMessage());
        }
    }
 
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }
        
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
        	initPreview(w, h);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
//            mCamera.takePicture(null, null, mPictureCallback);

        } catch (Exception e){
            Log.d("ERROR", "Error starting camera preview: " + e.getMessage());
        }
    }
    
    private void initPreview(int width, int height) {
        if (mCamera != null && mHolder.getSurface() != null) {

            if (!cameraConfigured) {
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size size = getBestPreviewSize(0, width, height, parameters, 0);

                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    mCamera.setParameters(parameters);
                    cameraConfigured = true;
                }
            }

            try {
            	mCamera.setPreviewDisplay(mHolder);
//            	mCamera.setDisplayOrientation(180);

            } catch (Throwable t) {
                Log.e("TAG", "Exception in setPreviewDisplay()", t);
//                Toast.makeText(MainActivity.instance, t.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }
    
    private Camera.Size getBestPreviewSize(int displayOrientation,
            int width,
            int height,
            Camera.Parameters parameters,
            double closeEnough) {
    	double targetRatio=(double)width / height;
        Camera.Size optimalSize=null;
        double minDiff=Double.MAX_VALUE;

        if (displayOrientation == 90 || displayOrientation == 270) {
          targetRatio=(double)height / width;
        }

        List<Size> sizes=parameters.getSupportedPreviewSizes();

        Collections.sort(sizes,
                         Collections.reverseOrder(new SizeComparator()));

        for (Size size : sizes) {
          double ratio=(double)size.width / size.height;

          if (Math.abs(ratio - targetRatio) < minDiff) {
            optimalSize=size;
            minDiff=Math.abs(ratio - targetRatio);
          }

          if (minDiff < closeEnough) {
            break;
          }
        }

        return(optimalSize);	
    }
    
	/** Create a File for saving an image or video */
    
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            if (data != null){

            	mCamera.stopPreview();
//					mCamera.setPreviewDisplay(null);

    			try {
    				//Callback to handle the picture taken
    				onTakePic.runCommand(data, camera);
    				
    				//Restart camera preview
    				mCamera.setPreviewDisplay(mHolder);
    				mCamera.startPreview();
    			} catch (FileNotFoundException e) {
    				Log.e("TAG", "File not found: " + e.getMessage());
    			} catch (IOException e) {
    				Log.e("TAG", "Error accessing file: " + e.getMessage());
    			}
    		}      
        }
    };
    
    private static class SizeComparator implements
    Comparator<Camera.Size> {
	  @Override
	  public int compare(Size lhs, Size rhs) {
	    int left=lhs.width * lhs.height;
	    int right=rhs.width * rhs.height;
	
	    if (left < right) {
	      return(-1);
	    }
	    else if (left > right) {
	      return(1);
	    }
	
	    return(0);
	  }
}
   
}
