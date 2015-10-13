package com.BlindLess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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

	    initializeServices();
        
//		preview.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				mCamera.takePicture(mShutterCallback, null, mPictureCallback);
//				return false;
//			}		
//
//		});     
    }

	private FrameLayout initializeServices() {
        // Create an instance of Camera
		if (mCamera == null) mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
		if (mPreview == null) mPreview = new CameraPreview(this, mCamera, onTakePicture);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        //Speech Recognition
  		initializeSpeech();
        
		return preview;
	}
	
	private CommandCamera onTakePicture = new CommandCamera(){

		@Override
		public int runCommand(byte[] data, Camera camera) {
			
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.e("TAG",
						"Error creating media file, check storage permissions: pictureFile== null");
				return -1;
			}
			
			speaker.speak("Imagen capturada. Aguarde mientras se procesa.");
			
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
			} catch (FileNotFoundException e) {
				Log.e("TAG", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.e("TAG", "Error accessing file: " + e.getMessage());
			}
			Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
				
			Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getPath(), options );
			
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			TessBaseAPI baseApi = new TessBaseAPI();
			// DATA_PATH = Path to the storage
			// lang = for which the language data exists, usually "eng"
			
			baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmn�opqrstuvwxyzABCDEFGHIJKLMN�OPQRSTUVWXYZ0123456789%$@#");
			baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!^&*()_+=-[]}{;:'\"\\|~`,./<>?");
			
			baseApi.init("/storage/sdcard0/", "spa");
			baseApi.setImage(bitmap);
			String recognizedText = baseApi.getUTF8Text();
			speaker.speak(recognizedText);
			
			speaker.speak("Texto le�do.");
			return 0;
		}

		
	};
	
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
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
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
    
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first

	    //Release camera resources
	    if(mCamera != null) mCamera.release();
	    if(mSpeechRecognizer != null) cleanSpeecher();
	    if(speaker != null) speaker.destroy();
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();  // Always call the superclass method first	    
	}
    
    private void cleanSpeecher() {
    	if(mSpeechRecognizer !=null){
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              

	    }
	    mSpeechRecognizer = null;
	}
	
	public static Camera getCameraInstance(){
	    Camera camera = null;
	    
	    try {
	    	camera = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    	Log.e("CameraActivity",e.getMessage().toString());
	    }
	    
	  //set camera to continually auto-focus
	    Camera.Parameters params = camera.getParameters();
	    if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
	        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	    } else {
	        //Choose another supported mode
	    }
//	    camera.setDisplayOrientation(90);
	    camera.setParameters(params);
	    
	    return camera; // returns null if camera is unavailable
	}
	

	Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {
			// Do nothing
			Log.i("CameraActivity","onShutter");
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
		
		commandDictionary.put("ayuda", new Command() {
            public void runCommand() { 
            	if(speaker != null) 
            	speaker.speak("Dijiste ayuda");
            	speaker.speak("Sujetar firmemente el celular");
            	speaker.speak("Alinear y centrar con el objeto a escanear");
            	speaker.speak("Distanciar el celular del objeto entre 27 y 32 cent�metros");
            	speaker.speak("Aguardar la se�al de reconocimiento efectivo"); 
            	startRecognition();
            	};
        });
		
		commandDictionary.put("volver", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Dijiste volver"); 
            	setResult(Activity.RESULT_OK);
            	finish();
            	};
        });
		
		commandDictionary.put("salir", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Dijiste salir"); 
            	setResult(Activity.RESULT_CANCELED);
            	finish();
            	};
        });
		
		commandDictionary.put("nada", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Comando de voz no reconocido"); 
            	startRecognition(); 
            	};
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
	
//	@Override
//	protected void onPause() {
//	    super.onPause();
//	    try
//	    {    
//	        // release the camera immediately on pause event   
//	        //releaseCamera();
//	         mCamera.stopPreview(); 
//	         mCamera.setPreviewCallback(null);
//	         mPreview.getHolder().removeCallback(mPreview);
//	         mCamera.release();
//	         mCamera = null;
//
//	    }
//	    catch(Exception e)
//	    {
//	        e.printStackTrace();
//	    }
//	}
//	
//	@Override
//	protected void onResume()
//	{
//	    super.onResume();
//	    try
//	    {
//	        mCamera.setPreviewCallback(null);
//	        initializeServices();
//	    } catch (Exception e){
////	        Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//	    }
//	}   
	
}
