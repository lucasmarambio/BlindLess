package com.BlindLess;


import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.BlindLess.R;
//import com.googlecode.tesseract.android.TessBaseAPI; //Lucas: No tengo las referencias para usar esto.

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity{

	private Button button;
	private Button buttonCamera;
	private Button buttonBillete;
	
	//text-to-speech fields
    public Speaker speaker; 
    private static final int TTS_CHECK = 10;
    private static final int CAMERA_ACTIVITY = 99;
    
    //Speech recognition fields
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent; 
    private boolean mIslistening; 
    private Map<String, Command> commandDictionary = new HashMap<String, Command>();

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
	
			button = (Button)findViewById(R.id.button1);
			buttonCamera = (Button)findViewById(R.id.buttonCamera);
			buttonBillete = (Button)findViewById(R.id.buttonBillete);
			
			//Init command dictionary
			initDictionary();
			
			//TTS Check
			Intent check = new Intent();
		    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		    startActivityForResult(check, TTS_CHECK);
			
			buttonCamera.setOnClickListener( new ButtonClickHandler() );
			//[INICIO].
			buttonBillete.setOnClickListener( new ButtonClickHandler() );
//			[FIN].
			
			initializeSpeech();
		    
		
		} catch (Exception e) {
			// TODO: hacer algo
		}	
		button.setOnClickListener(new View.OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				if (button.getText() == getString(R.string.reconocerVoz))
				{
					button.setText(getString(R.string.escuchando));
					speaker.speak(getString(R.string.escuchando));
				}
				else
				{
					button.setText(getString(R.string.reconocerVoz));
					speaker.speak(getString(R.string.reconocerVoz));
				}
			}
		});	
	}
	
//	@Override
//	protected void onPause() {
//	    super.onPause();  // Always call the superclass method first
//	    Log.i("MainActivity","onPause()");
//
//	    // Save the note's current draft, because the activity is stopping
//	    // and we want to be sure the current note progress isn't lost.
//	    mSpeechRecognizer.stopListening();
//	    Log.i("MainActivity","onPauseLeaving()");
//	}
//	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first
	    Log.i("MainActivity","onStop()");

	    // Save the note's current draft, because the activity is stopping
	    // and we want to be sure the current note progress isn't lost.
	    if(mSpeechRecognizer != null) cleanSpeecher();
	    if(speaker != null) speaker.destroy();
	    Log.i("MainActivity","onStopLeaving()");
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();  // Always call the superclass method first
	    Log.i("MainActivity","onRestart()");
	    
	    // The activity is either being restarted or started for the first time
	    // so this is where we should make sure that GPS is enabled
	    speaker = new Speaker(this, "");
	    speaker.runOnInit = new Command() {
            public void runCommand() { startRecognition(); };
        };
    
	    initializeSpeech();
	    Log.i("MainActivity","onRestartLeaving");
	}
	
	private void cleanSpeecher() {
	    if(mSpeechRecognizer !=null){
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              

	    }
	    mSpeechRecognizer = null;
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();  // Always call the superclass method first
	    Log.i("MainActivity","onDestroy()");
	    
	    // The activity is either being restarted or started for the first time
	    // so this is where we should make sure that GPS is enabled
	    if (speaker != null) speaker.destroy();
	    if (mSpeechRecognizer != null) cleanSpeecher();
	    Log.i("MainActivity","onDestroyLeaving");
	}

	
	//Leaving Activity methods
    private void iniciarActividadCamara() {
		speaker.speak("Iniciando cámara");
		Intent intent = new Intent(getApplicationContext(), CameraActivity.class );
		startActivityForResult(intent, CAMERA_ACTIVITY);
	}
    

	//Speech Recognition necessary methods
	private void initializeSpeech() {
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				"es-ES");


		SpeechRecognitionListener listener = 
				new SpeechRecognitionListener(mSpeechRecognizer, commandDictionary, new Command() {
										public void runCommand() { 
											if(mSpeechRecognizer != null) mSpeechRecognizer.destroy();
											initializeSpeech();
											startRecognition();
										};
        });
		mSpeechRecognizer.setRecognitionListener(listener);
	}
	
	
	private void initDictionary() {
		
		commandDictionary.put("camara", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Dijiste cámara"); 
            	iniciarActividadCamara(); 
            	startRecognition(); 
            	};
        });
		commandDictionary.put("iniciar", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Dijiste Iniciar"); 
            	startRecognition(); 
        	};
        });
		commandDictionary.put("detectar billete", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Dijiste detectar billete"); 
            	startRecognition(); 
        	};
        });
		commandDictionary.put("nada", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Comando de voz no reconocido"); 
            	startRecognition(); 
        	};
        });
		commandDictionary.put("salir", new Command() {
            public void runCommand() { 
            	if(speaker != null) speaker.speak("Dijiste salir"); 
            	finish(); 
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

	//To remove.. buttons!
    public class ButtonClickHandler implements View.OnClickListener 
    {
    	public void onClick( View view ){
    		//[INICIO]    		
    	    String path_ocr;
    	    //[FIN]
    		
    	    switch (view.getId()) {
			case R.id.buttonCamera:
//				SingletonTextToSpeech.getInstance(getApplicationContext()).sayHello("Iniciando Cámara");
				iniciarActividadCamara();
				break;
//[INICIO] Comenzando con las pruebas para detectar texto.
			case R.id.buttonBillete:
				path_ocr = "/storage/sdcard0/Pictures/BlindLess Pics/BlindLess1.jpg";
				ExifInterface exif;
				try {
					exif = new ExifInterface(path_ocr);
					int exifOrientation = exif.getAttributeInt(
					        ExifInterface.TAG_ORIENTATION,
					        ExifInterface.ORIENTATION_NORMAL);

					int rotate = 0;

					switch (exifOrientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
					    rotate = 90;
					    break;
					case ExifInterface.ORIENTATION_ROTATE_180:
					    rotate = 180;
					    break;
					case ExifInterface.ORIENTATION_ROTATE_270:
					    rotate = 270;
					    break;
					default:
						break;
					}
					
					BitmapFactory.Options options = new BitmapFactory.Options();
				    options.inSampleSize = 1;
				    	
				    Bitmap bitmap = BitmapFactory.decodeFile( path_ocr, options );
				    //_image.setImageBitmap(bitmap);
					
					if (rotate != 0) {
					    int w = bitmap.getWidth();
					    int h = bitmap.getHeight();

					    // Setting pre rotate
					    Matrix mtx = new Matrix();
					    mtx.preRotate(rotate);

					    // Rotating Bitmap & convert to ARGB_8888, required by tess
					    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
					}
					bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
					TessBaseAPI baseApi = new TessBaseAPI();
					// DATA_PATH = Path to the storage
					// lang = for which the language data exists, usually "eng"
					baseApi.init("/storage/sdcard0/", "spa");
					//baseApi.init("/storage/sdcard0/tessdata/spa.traineddata", "spa");
//					baseApi.init("/storage/sdcard0/TrainData", "spa");
					// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
					baseApi.setImage(bitmap);
					String recognizedText = baseApi.getUTF8Text();
					baseApi.end();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
												
//[FIN] Comenzando con las pruebas para detectar texto.
			default:
				break;
			}
    	}

    }
    
	//Text-to-Speech necessary method to initialize for each activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);

     switch (requestCode) {
     case TTS_CHECK:{
	    	 Log.i("Main Activity", "TTS_Check");
	    	 if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
	             speaker = new Speaker(this, "Bienvenidos a BlindLess");
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
     case CAMERA_ACTIVITY:{
    	 if (resultCode == Activity.RESULT_CANCELED) {
    		 finish();
    		 break;
    	 }
    	 
    	 //Reinitialize services
    	 speaker = new Speaker(this, "Usted a vuelto al menu principal");
		    speaker.runOnInit = new Command() {
	            public void runCommand() { startRecognition(); };
	        };
	    
		    initializeSpeech();
    	 break;
     }
     }
   }
    
    
 }

    
