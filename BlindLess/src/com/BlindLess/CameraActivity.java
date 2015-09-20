package com.BlindLess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;


public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    
	//text-to-speech fields
    public Speaker speaker; 
    private static final int TTS_CHECK = 10;
    
    //Speech recognition fields
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private boolean mIslistening;  
    private Map<String, Command> commandDictionary = new HashMap<String, Command>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        
		//Init command dictionary
		initDictionary();
		
		//Text to speech
		Intent check = new Intent();
	    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(check, TTS_CHECK);

	    //Speech Recognition
		initializeSpeech();

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
              
		preview.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mCamera.takePicture(mShutterCallback, null, mPictureCallback);
				return false;
			}			

		});     
        
    }
    
    
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first

	    // Save the note's current draft, because the activity is stopping
	    // and we want to be sure the current note progress isn't lost.
	    if(mCamera != null) mCamera.release();
	    if(mSpeechRecognizer != null) cleanSpeecher();
	    if(speaker != null) speaker.destroy();
	}
	
    
    private void cleanSpeecher() {
    	if(mSpeechRecognizer !=null){
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              

	    }
	    mSpeechRecognizer = null;
	}


	private OnTouchListener touchListener = new OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) 
        {
           // take a picture
           mCamera.takePicture(null, null, mPictureCallback);
           return false;
        } // end method onTouch
     }; // end touchListener  
	
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    	Log.e("CameraActivity",e.getMessage().toString());
	    }
	    return c; // returns null if camera is unavailable
	}
	
	private PictureCallback mPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.e("TAG",
						"Error creating media file, check storage permissions: pictureFile== null");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
				mCamera.startPreview();
			} catch (FileNotFoundException e) {
				Log.e("TAG", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.e("TAG", "Error accessing file: " + e.getMessage());
			}
		}
	};
	
	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.e("TAG",
						"Error creating media file, check storage permissions: pictureFile== null");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
				mCamera.startPreview();
			} catch (FileNotFoundException e) {
				Log.e("TAG", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.e("TAG", "Error accessing file: " + e.getMessage());
			}
		}
		
	};
	


	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "BlindLess Pics");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.e("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN)
				.format(new java.util.Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {
			// Do nothing
		}
	};

	
	//Speech Recognition necessary methods
	private void initializeSpeech() {
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(CameraActivity.this);
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				"es-ES");


		SpeechRecognitionListener listener = 
				new SpeechRecognitionListener(mSpeechRecognizer, commandDictionary, new Command() {
										public void runCommand() { 
											mSpeechRecognizer.destroy();
											initializeSpeech();
											startRecognition();
										};
        });
		mSpeechRecognizer.setRecognitionListener(listener);
	}
	
	
	private void initDictionary() {
		
		commandDictionary.put("volver", new Command() {
            public void runCommand() { 
            	speaker.speak("Dijiste volver"); 
            	setResult(Activity.RESULT_OK);
            	finish();
            	};
        });
		
		commandDictionary.put("nada", new Command() {
            public void runCommand() { speaker.speak("Comando de voz no reconocido"); startRecognition(); };
        });
		
	}
	
	public void startRecognition(){
		Log.i("Speech", "StartRecognition call");
		if (!mIslistening)
		{
			Log.i("Speech", "Starting listening");
		    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}
	}

	
	//Text-to-Speech necessary method to initialize for each activity.
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);

     switch (requestCode) {
     case TTS_CHECK:{
    	 Log.i("Camera Activity", "TTS_Check");
    	 if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
    		speaker = new Speaker(this, "");
 		    speaker.runOnInit = new Command() {
	            public void runCommand() { startRecognition(); };
	        };
         }else {
             Intent install = new Intent();
             install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
             startActivity(install);
         }
    	 break;
         }
     }
   }
	
}
