package com.BlindLess;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

public class MainActivity extends Activity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

	private static final String COMANDO_SALIR = "salir";
	private static final String COMANDO_REPETIR = "repetir";
	private static final String COMANDO_DETECTAR_BILLETE = "detectar billete";
	private static final String COMANDO_DETECTAR_TEXTO = "detectar texto";
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
    private boolean mIsSpeaking; 
    private Map<String, Command> commandDictionary = new HashMap<String, Command>();
    
    //Timer
    private Timer timer;
    private TimerTask task;
	private android.os.Handler handler;
	
	//Detector de Taps
	private static final String DEBUG_TAG = "Gestures";
	private GestureDetectorCompat mDetector;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			
			mIsSpeaking = true;
	
			buttonCamera = (Button)findViewById(R.id.buttonCamera);
			buttonBillete = (Button)findViewById(R.id.buttonBillete);
			buttonComparador = (Button)findViewById(R.id.ButtonComparador);
			handler = new android.os.Handler();
			
			if (!OpenCVLoader.initDebug()) {
		        // Handle initialization error
		    }
			
			//Init command dictionary
			initDictionary();
			
			//TTS Check
			Intent check = new Intent();
		    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		    startActivityForResult(check, TTS_CHECK);
		    
		    //Inicializo y defino los taps
		    mDetector = new GestureDetectorCompat(this,this);
		    mDetector.setOnDoubleTapListener(this);
			
			buttonCamera.setOnClickListener( new ButtonClickHandler() );
			buttonBillete.setOnClickListener( new ButtonClickHandler() );
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
	    cleanSpeecher();
	    if (speaker != null) speaker.destroy();
	    cleanTimer();
	    Log.i("MainActivity","onStopLeaving()");
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();  // Always call the superclass method first
	    Log.i("MainActivity","onRestart()");
	    
	    // The activity is either being restarted or started for the first time
	    // so this is where we should make sure that GPS is enabled
	    if (speaker == null || speaker.initFinish){ 
	    	speaker = new Speaker(this, "");
		    speaker.runOnInit = new Command() {
		    	public void runCommand() { 
		    		mensajePrincipal();
		    		startRecognition();
 		    	};
	        };
	    }
	    initializeSpeech();
	    Log.i("MainActivity","onRestartLeaving");
	}
	


	@Override
	protected void onDestroy() {
	    super.onDestroy();  // Always call the superclass method first
	    Log.i("MainActivity","onDestroy()");
	    
	    // The activity is either being restarted or started for the first time
	    // so this is where we should make sure that GPS is enabled
	    if (speaker != null) speaker.destroy();
	    cleanSpeecher();
	    cleanTimer();
	    Log.i("MainActivity","onDestroyLeaving");
	}

	
	//Leaving Activity methods
    private void iniciarActividadCamara(String modo) {
		speaker.speak("Iniciando c�mara");
		Intent intent = new Intent(getApplicationContext(), CameraActivity.class );
		intent.putExtra("modo", modo);
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
		
		commandDictionary.put(COMANDO_DETECTAR_TEXTO, new Command() {
            public void runCommand() { 
	            	speak("Dijiste detectar texto"); 
	            	iniciarActividadCamara(CommonMethods.MODO_RECONOCIMIENTO_TEXTO); 
            	};
        });
		commandDictionary.put(COMANDO_DETECTAR_BILLETE, new Command() {
            public void runCommand() { 
            	speak("Dijiste detectar billete"); 
            	iniciarActividadCamara(CommonMethods.MODO_RECONOCIMIENTO_BILLETE);
        	};
        });
		commandDictionary.put(COMANDO_REPETIR, new Command() {
			public void runCommand() { 
				speak("Dijiste repetir");
				startRecognition();
	    	};
        });
		commandDictionary.put(COMANDO_SALIR, new Command() {
            public void runCommand() { 
            	speak("Dijiste salir"); 
            	finish(); 
        	};
        });
		commandDictionary.put("nada", new Command() {
            public void runCommand() { 
            	speak("Comando de voz no reconocido"); 
            	startRecognition();
        	};
        });
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
//				SingletonTextToSpeech.getInstance(getApplicationContext()).sayHello("Iniciando C�mara");
				iniciarActividadCamara(CommonMethods.MODO_RECONOCIMIENTO_TEXTO);
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
//				ImagePreprocessor preprocessor = new ImagePreprocessor();
//				preprocessor.imagePreprocess();
								
				break;
			   
			default:
				break;
			}
    	}

		

    }
    
    public void mensajePrincipal(){
    	speak("mensaje principal");
//    	List<String> textos = new ArrayList<String>();
//    	textos.add("Pronuncie el comando detectar texto"
//	  	  	  	+ "si desea ingresar al m�dulo de detecci�n de textos");
//    	textos.add("Pronuncie el comando detectar billete"
//		  		+ "si desea ingresar al m�dulo de reconocimiento de billetes");
//    	textos.add("Pronuncie el comando salir si desea salir de la aplicaci�n");
//    	multipleSpeak(textos);
    }
    
  //repite el mensaje principal cada x cantidad de segundos, si no hubo interacci�n del usuario.
    public void repetirMensajePrincipal(int seg1, int seg2) {
    	cleanTimer();
    	task = new TimerTask() {
  		   	@Override
  		   	public void run() {
  		   		handler.post(new Runnable() {
  		   			public void run() {
  		   				cleanSpeecher();
		   				mensajePrincipal();
		   				initializeSpeech();
		   				startRecognition();
  		   			};
  		   		});
  		   	}
  		};
		timer = new Timer();
		timer.schedule(task,seg1,seg2);
    }
    
	public void startRecognition(){
		Log.i("Speech", "StartRecognition call");
		if (!mIsSpeaking)
		{
			Log.i("Speech", "Starting listening");
		    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}
	}
	
	public void cleanSpeecher() {
	    if(mSpeechRecognizer !=null){
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              
	    }
	    mSpeechRecognizer = null;
	}
	
	public void cleanTimer() {
		if (timer != null) {
    		timer.cancel();
    		timer.purge();
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
		 		    	public void runCommand() { 
		 		    		mensajePrincipal();
		 		    		startRecognition();
		 		    	};
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
    	 speaker = new Speaker(this, "Usted ha vuelto al menu principal");
    	 	speaker.runOnInit = new Command() {
    	 		public void runCommand() { 
    	 			mensajePrincipal();
    	 			startRecognition();
 		    	};
	        };
		    initializeSpeech();
    	 break;
     }
     }
   }
    
	public void multipleSpeak(List<String> textos){
		mIsSpeaking = true;
		cleanTimer();
		for (String texto : textos) {
			if(speaker != null) speaker.speak(texto);
		}
		mIsSpeaking = false;
		repetirMensajePrincipal(CommonMethods.DECIR_MSJ_PRINCIPAL, CommonMethods.REPETIR_MSJ_PRINCIPAL);
	}
	
	public void speak(String text){
		mIsSpeaking = true;
		cleanTimer();
		if(speaker != null) speaker.speak(text);
		mIsSpeaking = false;
		repetirMensajePrincipal(CommonMethods.DECIR_MSJ_PRINCIPAL, CommonMethods.REPETIR_MSJ_PRINCIPAL);
	}

    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) { 
        Log.d(DEBUG_TAG,"onDown: " + event.toString()); 
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, 
            float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString()); 
        	finish(); 
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        
        if(mIsSpeaking) {
        	Log.d(DEBUG_TAG, "onDoubleTap: Silenciar Speaker" + event.toString());       	
        } else {
        	Log.d(DEBUG_TAG, "onDoubleTap: M�dulo Texto" + event.toString());
        	commandDictionary.get(COMANDO_DETECTAR_BILLETE);
        }
        
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: M�dulo Texto" + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
    	 
        if(mIsSpeaking) {
        	Log.d(DEBUG_TAG, "onSingleTapConfirmed: Silenciar Speaker" + event.toString());
        }
        else {
        	Log.d(DEBUG_TAG, "onSingleTapConfirmed: M�dulo Billete" + event.toString());
        	commandDictionary.get(COMANDO_DETECTAR_TEXTO);
        }
        return true;
    }
 }

    
