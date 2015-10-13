package com.BlindLess;


import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.opencv.android.OpenCVLoader;
import com.BlindLess.R;
import com.googlecode.tesseract.android.TessBaseAPI; //Lucas: No tengo las referencias para usar esto.

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.imgproc.Imgproc;
//RR 2015-09-27 [FIN].

public class MainActivity extends Activity{

	private Button buttonCamera;
	private Button buttonBillete;
	private Button buttonComparador;
	
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
	
			buttonCamera = (Button)findViewById(R.id.buttonCamera);
			buttonBillete = (Button)findViewById(R.id.buttonBillete);
			buttonComparador = (Button)findViewById(R.id.ButtonComparador);
			
			if (!OpenCVLoader.initDebug()) {
		        // Handle initialization error
		    }
			
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
			buttonComparador.setOnClickListener( new ButtonClickHandler() );
			
			initializeSpeech();
		    
		
		} catch (Exception e) {
			// TODO: hacer algo
		}	
		
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
				path_ocr = "/storage/sdcard0/Pictures/BlindLess Pics/rodri_prueba.jpg";
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
					baseApi.setImage(bitmap);
					String recognizedText = baseApi.getUTF8Text();
					baseApi.end();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;	
//[FIN] Comenzando con las pruebas para detectar texto.			

			case R.id.ButtonComparador:
				
				String naipe = "storage/sdcard0/PatronesBilletes/Billete 2 pesos/naipe.jpg";
				String cartel = "storage/sdcard0/PatronesBilletes/Billete 2 pesos/cartel.jpg";
				String billete_2_pesos = "storage/sdcard0/PatronesBilletes/Billete 2 pesos/2_billete_posta_2.jpg";
				String billete_100_pesos = "storage/sdcard0/PatronesBilletes/Billete 100 pesos/billete_100_0.jpg";
				String billete_10 = "storage/sdcard0/PatronesBilletes/Billete 10 pesos/10_pesos.jpg";
				String billete_10_pesos = "storage/sdcard0/PatronesBilletes/Billete 10 pesos/10_pesos_billete.jpg";
				String billete_10_pesos_2 = "storage/sdcard0/PatronesBilletes/Billete 10 pesos/10_pesos_billete2.jpg";
				String billete_50_pesos = "storage/sdcard0/PatronesBilletes/Billete 50 pesos/50_pesos_billete.jpg";
				String patron_billete_2 =  "storage/sdcard0/PatronesBilletes/Billete 2 pesos/2_patron_rombos.JPG";
				String patron_billete_2_abajo = "storage/sdcard0/PatronesBilletes/Billete 2 pesos/2_inferior_der.JPG";
				String patron_billete_2_escrito = "storage/sdcard0/PatronesBilletes/Billete 2 pesos/texto_2_pesos.JPG";
				String outFile = "storage/sdcard0/PatronesBilletes/Resultado.jpg";
				int match_method = Imgproc.TM_CCOEFF_NORMED;
				ImageComparator comparator = new ImageComparator();
				comparator.comparate(billete_10_pesos_2, patron_billete_2, outFile, match_method);
				break;
			   
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

    
