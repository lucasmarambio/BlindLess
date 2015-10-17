package com.BlindLess;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.OpenCVLoader;

import com.BlindLess.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity{

	private Button buttonComparador;
	
	//text-to-speech fields
    public Speaker speaker; 
    private static final int TTS_CHECK = 10;
    private static final int CAMERA_ACTIVITY = 99;
    private static final int CANT_IMAGES = 4;
    
    //Speech recognition fields
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent; 
    private boolean mIsSpeaking; 
    private Map<String, Command> commandDictionary = new HashMap<String, Command>();
    
    //Timer
    private Timer timer;
    private TimerTask task;
	private android.os.Handler handler;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
	
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
		
		commandDictionary.put("detectar texto", new Command() {
            public void runCommand() { 
	            	speak("Dijiste detectar texto"); 
	            	iniciarActividadCamara(); 
            	};
        });
		commandDictionary.put("detectar billete", new Command() {
            public void runCommand() { 
            	speak("Dijiste detectar billete"); 
            	startRecognition(); 
        	};
        });
		commandDictionary.put("repetir", new Command() {
			public void runCommand() { 
				speak("Dijiste repetir");
				startRecognition();
	    	};
        });
		commandDictionary.put("salir", new Command() {
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
    	    switch (view.getId()) {
			case R.id.ButtonComparador:								
				List<String> billetes = new ArrayList<String>();
				for (int i = 0; i < CANT_IMAGES; i++) {
					billetes.add("2_" + i);
				}
				for (int i = 0; i < CANT_IMAGES; i++) {
					billetes.add("5_" + i);
				}
				for (int i = 0; i < CANT_IMAGES; i++) {
					billetes.add("10_" + i);
				}
				for (int i = 0; i < CANT_IMAGES; i++) {
					billetes.add("100_" + i);
				}
				
				MatchPatternsFor("supizq", billetes);
				
				break;
			   
			default:
				break;
			}
    	}

		private void MatchPatternsFor(String pattern, List<String> billetes) {
			List<String> templates = new ArrayList<String>();
			addTemplatesValue("2", pattern, templates);
			addTemplatesValue("5", pattern, templates);
			addTemplatesValue("10", pattern, templates);
			addTemplatesValue("20", pattern, templates);
			addTemplatesValue("50", pattern, templates);
			addTemplatesValue("100", pattern, templates);
			
			if (pattern == "supizq"){
				matchSupIzq(billetes, templates);
			}
			
		}

		private void matchSupIzq(List<String> billetes, List<String> templates) {
			int match_method = Imgproc.TM_CCOEFF_NORMED;
			startComparisson(billetes, templates, match_method, new CommandComparisson() {
				
				@Override
				public double runCommand(String billeteToCheck, String templateToCheck,
						String outFile, int match_method, String description) {
					ImageComparator comparator = new ImageComparator();
					return comparator.comparateSupIzq(billeteToCheck, templateToCheck, outFile, match_method, description);
				}
			});
		}

		private void startComparisson(List<String> billetes,
				List<String> templates, int match_method, CommandComparisson comparisson) {
			double maxVal;
			String templateGanador;
			for (String billete : billetes) {
				maxVal = 0.0;
				templateGanador = "";
				String billeteToCheck = "storage/sdcard0/Pictures/PatronesBilletes/2 Images a Probar/billete_" + billete + ".jpg";
				for (String template : templates) {	
					
					String templateToCheck = "storage/sdcard0/Pictures/PatronesBilletes/2 Pesos/" + template + ".jpg";
					String outFile = "storage/sdcard0/Pictures/PatronesBilletes/Resultado" + billete + "_" + template + ".jpg";
					double val = comparisson.runCommand(billeteToCheck, templateToCheck, outFile, 
							match_method, "Billete: " + billete + ", Template: " + template);
							
					if (val > maxVal)
					{
						maxVal = val;
						templateGanador = template;
					}
				}
				Log.w("BLINDLESSTEST","Es un billete de: " + templateGanador + " MaxVal: " + maxVal);
				speaker.speak("Es un billete de: " + templateGanador.substring(0, templateGanador.indexOf('_')) + " pesos");
			}
		}

		private void addTemplatesValue(String value, String pattern, List<String> templates) {
			templates.add(value + "_" + pattern + "_" + 20);
			templates.add(value + "_" + pattern + "_" + 40);
			templates.add(value + "_" + pattern + "_" + 60);
			templates.add(value + "_" + pattern + "_" + 80);
			templates.add(value + "_" + pattern + "_" + 100);
		}

    }
    
    public void mensajePrincipal(){
    	speak("mensaje principal");
//    	List<String> textos = new ArrayList<String>();
//    	textos.add("Pronuncie el comando detectar texto"
//	  	  	  	+ "si desea ingresar al módulo de detección de textos");
//    	textos.add("Pronuncie el comando detectar billete"
//		  		+ "si desea ingresar al módulo de reconocimiento de billetes");
//    	textos.add("Pronuncie el comando salir si desea salir de la aplicación");
//    	multipleSpeak(textos);
    }
    
  //repite el mensaje principal cada x cantidad de segundos, si no hubo interacción del usuario.
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
 }

    
