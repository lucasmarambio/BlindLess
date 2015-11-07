package com.BlindLess;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.OpenCVLoader;

import com.BlindLess.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

	private static final String COMANDO_SALIR = "salir";
	private static final String COMANDO_REPETIR = "repetir";
	private static final String COMANDO_DETECTAR_BILLETE = "detectar billete";
	private static final String COMANDO_DETECTAR_TEXTO = "detectar texto";
	
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
	    Log.w("RODRILOG", "onStop");

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
	    Log.w("RODRILOG", "onRestart");
	    
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
    	Log.w("RODRILOG", ">> iniciarActividadCamara");
    	speakWithoutRepetir("Iniciando cámara");
		Intent intent = new Intent(getApplicationContext(), CameraActivity.class );
		intent.putExtra("modo", modo);
		startActivityForResult(intent, CAMERA_ACTIVITY);
		Log.w("RODRILOG", "<< iniciarActividadCamara");
	}

	//Speech Recognition necessary methods
	private void initializeSpeech() {
		if (CommonMethods.verificaConexion(this))
		{
			Log.w("RODRILOG", ">> InitializeSpeech");
			mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
			mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					"es-ES");
	
			SpeechRecognitionListener listener = 
				new SpeechRecognitionListener(mSpeechRecognizer, commandDictionary, new Command() {
												public void runCommand() { 
													Log.w("RODRILOG", ">> InitiliazeSpeechOnError");
													if(mSpeechRecognizer != null) mSpeechRecognizer.destroy();
													initializeSpeech();
													startRecognition();
													Log.w("RODRILOG", "<< InitiliazeSpeechOnError");
												};
											 });
			mSpeechRecognizer.setRecognitionListener(listener);
			Log.w("RODRILOG", "<< InitializeSpeech");
		}
	}
	
	private void initDictionary() {
		
		commandDictionary.put(COMANDO_DETECTAR_TEXTO, new Command() {
            public void runCommand() { 
            	Log.w("RODRILOG", ">> detectarTextoCommand");
            	speak("Dijiste detectar texto");
            	iniciarActividadCamara(CommonMethods.MODO_RECONOCIMIENTO_TEXTO); 
            	Log.w("RODRILOG", "<< detectarTextoCommand");
        	};
        });
		commandDictionary.put(COMANDO_DETECTAR_BILLETE, new Command() {
            public void runCommand() { 
            	Log.w("RODRILOG", ">> detectarbilletecommand");
            	speak("Dijiste detectar billete"); 
            	iniciarActividadCamara(CommonMethods.MODO_RECONOCIMIENTO_BILLETE);
            	Log.w("RODRILOG", "<< detectarbilletecommand");
        	};
        });
		commandDictionary.put(COMANDO_REPETIR, new Command() {
			public void runCommand() { 
				speak("Dijiste repetir");
				mensajePrincipal();
				startRecognition();
	    	};
        });
		commandDictionary.put(COMANDO_SALIR, new Command() {
            public void runCommand() { 
            	speakWithoutRepetir("Dijiste salir"); 
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

		

    
    public void mensajePrincipal(){
    	Log.w("RODRILOG", "mensajePrincipal");
    	speak("mensaje principal");
//    	List<String> textos = new ArrayList<String>();
//    	textos.add("Pronuncie el comando detectar billete"
//  			 + "si desea ingresar al módulo de detección de billetes");
//    	textos.add("Pronuncie el comando detectar texto"
//	  	  	  	 + "si desea ingresar al módulo de reconocimiento de textos");
//    	textos.add("Pronuncie el comando salir si desea salir de la aplicación");
//    	multipleSpeak(textos);
    }
    
  //repite el mensaje principal cada x cantidad de segundos, si no hubo interacción del usuario.
    public void repetirMensajePrincipal(int seg1, int seg2) {
    	Log.w("RODRILOG", "RepetirMensajePrincipal");
    	cleanTimer();
    	task = new TimerTask() {
  		   	@Override
  		   	public void run() {
  		   		handler.post(new Runnable() {
  		   			public void run() {
  		   				Log.w("RODRILOG", ">> Repitiendopapi");
  		   				cleanSpeecher();
		   				mensajePrincipal();
		   				initializeSpeech();
		   				startRecognition();
		   				Log.w("RODRILOG", "<< InitiliazeSpeechOnError");
  		   			};
  		   		});
  		   	}
  		};
		timer = new Timer();
		timer.schedule(task,seg1,seg2);
    }
    
	public void startRecognition(){
		Log.w("RODRILOG", "startRecognition");
		if (!mIsSpeaking)
		{
			Log.i("Speech", "Starting listening");
		    if (mSpeechRecognizer != null)
		    {
		    	mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		    }
		}
	}
	
	public void cleanSpeecher() {
		Log.w("RODRILOG", "matoSpeecher");
	    if(mSpeechRecognizer != null)
	    {
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              
	    }
	    mSpeechRecognizer = null;
	}
	
	public void cleanTimer() 
	{
		Log.w("RODRILOG", "matoTimer");
		if (timer != null) 
		{
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
	
	public void speakWithoutRepetir(String text){
		mIsSpeaking = true;
		cleanTimer();
		if(speaker != null) speaker.speak(text);
		mIsSpeaking = false;
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
    	Log.w("RODRILOG", "onDoubleTap");
        if(mIsSpeaking) {
        	Log.d(DEBUG_TAG, "onDoubleTap: Silenciar Speaker" + event.toString());       	
        } else {
        	Log.d(DEBUG_TAG, "onDoubleTap: Módulo Texto" + event.toString());
        	cleanSpeecher();
        	commandDictionary.get(COMANDO_DETECTAR_TEXTO).runCommand();
        }
        
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: Módulo Texto" + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
    	Log.w("RODRILOG", "onSingleTapConfirmed");
        if(mIsSpeaking) {
        	Log.d(DEBUG_TAG, "onSingleTapConfirmed: Silenciar Speaker" + event.toString());
        }
        else {
        	Log.d(DEBUG_TAG, "onSingleTapConfirmed: Módulo Billete" + event.toString());
        	cleanSpeecher();
        	commandDictionary.get(COMANDO_DETECTAR_BILLETE).runCommand();
        }
        return false;
    }
 }

    
