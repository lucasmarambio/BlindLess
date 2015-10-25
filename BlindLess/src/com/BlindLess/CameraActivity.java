package com.BlindLess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.FrameLayout;


public class CameraActivity extends Activity {

    private static final String COMANDO_SALIR = "salir";
	private static final String COMANDO_VOLVER = "volver";
	private static final String COMANDO_AYUDA = "ayuda";
	private static final String COMANDO_REPETIR = "repetir";
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
    private boolean mIsSpeaking;  
    private Map<String, Command> commandDictionary = new HashMap<String, Command>();
    
    //Timer
    private Timer timer;
    private TimerTask task;
	private android.os.Handler handler;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        handler = new android.os.Handler();
        
		//Init command dictionary
		initDictionary();
		
		//Text to speech
		Intent check = new Intent();
	    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(check, TTS_CHECK);
	    
	    Bundle bundle = getIntent().getExtras();
	    initializeServices(bundle.getString("modo"));
        
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

	private FrameLayout initializeServices(String modo) {
		CommandCamera onTakePicture;
		if (modo.equals(CommonMethods.MODO_RECONOCIMIENTO_TEXTO)) {
			onTakePicture = textOnTakePicture;
		}else {
			onTakePicture = billeteOnTakePicture;
		}
		
        // Create an instance of Camera
		if (mCamera == null) mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
		if (mPreview == null) mPreview = new CameraPreview(this, (SurfaceView)findViewById(R.id.camera_preview), mCamera, onTakePicture);
		mPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((FrameLayout) findViewById(R.id.layout)).addView(mPreview);
		mPreview.setKeepScreenOn(true);
        
        //Speech Recognition
  		initializeSpeech();
        
		return ((FrameLayout) findViewById(R.id.layout));
	}
	
	private CommandCamera textOnTakePicture = new CommandCamera(){

		@Override
		public int runCommand(byte[] data, Camera camera) {
			
			File pictureFile = CommonMethods.getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.e("TAG",
						"Error creating media file, check storage permissions: pictureFile== null");
				return -1;
			}
			
			speak("Imagen capturada. Aguarde mientras se procesa.");
			
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
			
			baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmnñopqrstuvwxyzABCDEFGHIJKLMNÑOPQRSTUVWXYZ0123456789%$@#");
			baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!^&*()_+=-[]}{;:'\"\\|~`,./<>?");
			
			baseApi.init("/storage/sdcard0/BlindLess/", "spa");
			baseApi.setImage(bitmap);
			String recognizedText = baseApi.getUTF8Text();
			speak(recognizedText);
			
			speak("Texto leído.");
			return 0;
		}

		
	};
	
	private CommandCamera billeteOnTakePicture = new CommandCamera(){

		@Override
		public int runCommand(byte[] data, Camera camera) {
			
			File pictureFile = CommonMethods.getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.e("TAG",
						"Error creating media file, check storage permissions: pictureFile== null");
				return -1;
			}
			
//			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//			bitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
//			
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				
//				FileOutputStream fos = new FileOutputStream(pictureFile);
//				bitmap.compress(Bitmap.CompressFormat.PNG, 85, fos);
//				fos.flush();
//				fos.close();
//				Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
			} catch (FileNotFoundException e) {
				Log.e("TAG", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.e("TAG", "Error accessing file: " + e.getMessage());
			}
			Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
						
			List<String> billetes = new ArrayList<String>();
			billetes.add(pictureFile.getPath());	
			
			//Old Method to detect supizq value from picture
			return MatchPatternsFor("supizq", billetes);
		}

		private int MatchPatternsFor(String pattern, List<String> billetes) {
			List<String> templates = new ArrayList<String>();
			addTemplatesValue("2", pattern, templates);
			addTemplatesValue("5", pattern, templates);
			addTemplatesValue("10", pattern, templates);
			addTemplatesValue("20", pattern, templates);
			addTemplatesValue("50", pattern, templates);
			addTemplatesValue("100", pattern, templates);
			
			if (pattern == "supizq"){
//				Old Method
//					return matchSupIzq(billetes, templates);
//				New Method
				return matchSupIzq(billetes, templates);
			}
			return 0;
		}

//		private int matchSupIzq(List<String> billetes, List<String> templates) {
//			int match_method = Imgproc.TM_CCOEFF_NORMED;
//			return startComparisson(billetes, templates, match_method, new CommandComparisson() {
//				
//				@Override
//				public double runCommand(String billeteToCheck, String templateToCheck,
//						String outFile, int match_method, String description) {
//					ImageComparator comparator = new ImageComparator();
//					return comparator.comparateSupIzq(billeteToCheck, templateToCheck, outFile, match_method, description);
//				}
//			});
//		}

		private int matchSupIzq(List<String> billetes, List<String> templates) {
			/*INICIALIZO TESSERACT*/
			ImageComparator comparator = new ImageComparator();
			TessBaseAPI baseApi = new TessBaseAPI();
			baseApi.init("/storage/sdcard0/BlindLess/", "spa");
			baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789");
			
			for (String billeteToCheck : billetes) {
				for (String template : templates) {
					String templateToCheck = "storage/sdcard0/BlindLess/Templates/" + template + ".jpg";
					String outFile = "storage/sdcard0/BlindLess/Resultados/Resultado" + 
						billeteToCheck.substring(billeteToCheck.length() - 9, billeteToCheck.length() - 1) 
						+ "_" + template + ".jpg";
					Bitmap supIzq = comparator.readSupIzq(billeteToCheck, templateToCheck, outFile);
					baseApi.setImage(supIzq);
					String textoLeido = baseApi.getUTF8Text();
					Log.w("BLINDLESSTEST","Leyó: " + textoLeido);
					String billeteReconocido = CommonMethods.esBilleteValido(textoLeido);
					if (!billeteReconocido.equals("")) {
						speak("Es un billete de: " + billeteReconocido + " pesos");
						return 1;
					}
				}
			}
			Log.w("BLINDLESSTEST","No se encontró patrón amigo");
			return 1;//TODO: Tiene que devolver 0, para sacar una foto automatica, pero por ahora que devuelva 1.
		}


		//No usado por ahora, pero sirve para hacer la vieja comparación
		/*
		private int startComparisson(List<String> billetes,
				List<String> templates, int match_method, CommandComparisson comparisson) {
			double maxVal, val;
			String templateGanador, actualTemplate, templateNumber = "";
			for (String billeteToCheck : billetes) {
				maxVal = 0.0;
				val = 0.0;
				templateGanador = "";
				actualTemplate = templates.get(0).substring(0, templates.get(0).indexOf('_'));
				String descripcionBillete = billeteToCheck.substring(billeteToCheck.length() - 9, billeteToCheck.length() - 1);
				for (String template : templates) {	
					templateNumber = template.substring(0, template.indexOf('_'));
					String templateToCheck = "storage/sdcard0/BlindLess/Templates/" + template + ".jpg";
					String outFile = "storage/sdcard0/BlindLess/Resultados/Resultado" + descripcionBillete + "_" + template + ".jpg";
					double valAux = comparisson.runCommand(billeteToCheck, templateToCheck, outFile, 
							match_method, "Billete: " + descripcionBillete + ", Template: " + template);
						
					if (actualTemplate.equals(templateNumber)){
						val = val + valAux;
					}else {
						if (val > maxVal && val > 1.5)
						{
							maxVal = val;
							templateGanador = actualTemplate;
						}
						val = valAux;
						actualTemplate = templateNumber;
					}
				}
				
				if (val > maxVal && val > 1.5)
				{
					maxVal = val;
					templateGanador = templateNumber;
				}
				
				if (maxVal > 0.0) {
					Log.w("BLINDLESSTEST","Es un billete de: " + templateGanador + " MaxVal: " + maxVal);
					speak("Es un billete de: " + templateGanador + " pesos");
					return 1;
				}
			}
			
			return 0;
		}
*/
		
		
		private void addTemplatesValue(String value, String pattern, List<String> templates) {
			templates.add(value + "_" + pattern + "_" + 40);
			templates.add(value + "_" + pattern + "_" + 60);
			templates.add(value + "_" + pattern + "_" + 80);
		}
		
	};
	

    
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first

	    //Release camera resources
	    if(mCamera != null) mCamera.release();
	    cleanSpeecher();
	    if(speaker != null) speaker.destroy();
	    cleanTimer();
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();  // Always call the superclass method first	    
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
//	    camera.setParameters(params);
	    
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
		
		commandDictionary.put(COMANDO_AYUDA, new Command() {
            public void runCommand() { 
            	List<String> textos = new ArrayList<String>();
            	textos.add("Dijiste ayuda");
            	textos.add("Sujetar firmemente el celular");
            	textos.add("Alinear y centrar con el objeto a escanear");
            	textos.add("Distanciar el celular del objeto entre 27 y 32 centímetros");
            	textos.add("Aguardar la señal de reconocimiento efectivo"); 
            	multipleSpeak(textos);
            	startRecognition();
            	};
        });
		
		commandDictionary.put(COMANDO_VOLVER, new Command() {
            public void runCommand() { 
            	speak("Dijiste volver"); 
            	setResult(Activity.RESULT_OK);
            	finish();
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
            	setResult(Activity.RESULT_CANCELED);
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

    public void mensajePrincipal(){
		speak("Pronuncie el comando ayuda para iniciar la guía de"
			+ "detección o el comando volver para retornar al Menú principal");
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
    	 Log.i("Camera Activity", "TTS_Check");
    	 if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
    		speaker = new Speaker(this, "");
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
     }
   }
	
	@Override
	protected void onPause() {
//		if(mCamera != null) {
//			mCamera.stopPreview();
//			mPreview.setCamera(null);
//			mCamera.release();
//			mCamera = null;
//		}
		super.onPause();
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
//		int numCams = Camera.getNumberOfCameras();
//		if(numCams > 0){
//			try{
//				mCamera = Camera.open(0);
//				mCamera.startPreview();
//				mPreview.setCamera(mCamera);
//			} catch (RuntimeException ex){
//			}
//		}
	} 
	
	
}
