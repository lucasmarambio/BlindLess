package com.BlindLess;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
	
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CommandCamera onTakePic;
    private boolean initTouch = true;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    
	private boolean cameraConfigured = false;
	
	public CameraPreview(Context context, SurfaceView surfaceView, Camera camera, CommandCamera callbackOnTakePic) {
        super(context);
        mCamera = camera;
        onTakePic = callbackOnTakePic;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = surfaceView.getHolder();
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
    
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
           mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
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
        	Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        	mCamera.setParameters(parameters);
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
    				int returnMode = onTakePic.runCommand(data, camera);
    				
    				switch (returnMode) {
						case 1: //Reconoció bien el billete o texto, no tiene que sacar foto hasta tocar pantalla.
							initTouch = true; //Permitimos que vuelva a sacar foto.
							break;
						case 0: //Falló el reconocimiento de billete, sacamos otra!
							break;

					default:
						break;
					}
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

    public void setCamera(Camera camera) {
    	mCamera = camera;
    	if (mCamera != null) {
    		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
    		requestLayout();

    		// get Camera parameters
    		Camera.Parameters params = mCamera.getParameters();

    		List<String> focusModes = params.getSupportedFocusModes();
    		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
    			// set the focus mode
    			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    			// set Camera parameters
    			mCamera.setParameters(params);
    		}
    	}
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }
   
}
