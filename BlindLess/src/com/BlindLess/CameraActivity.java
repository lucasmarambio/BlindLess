package com.BlindLess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import com.googlecode.tesseract.android.TessBaseAPI;


public class CameraActivity extends Activity {

	private static final String COMANDO_SALIR = "salir";
	private static final String COMANDO_VOLVER = "volver";
	private static final String COMANDO_AYUDA = "ayuda";
	private static final String COMANDO_REPETIR = "repetir";
	private Camera mCamera;
    private CameraPreview mPreview;
	private Activity act;
	private Context ctx;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    
	//text-to-speech fields
    public Speaker speaker; 
    private static final int TTS_CHECK = 10;
	protected static final double MINVAL_SUPPORTED = 4500000.0;
	protected static final double MINVAL_SUPPORTED_2 = 3500000.0;
    
    //Speech recognition fields
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private boolean mIsSpeaking;  
    private Map<String, Command> commandDictionary = new HashMap<String, Command>();
    private String actualModo;
    
    //Timer
    private Timer timer;
    private TimerTask task;
	private android.os.Handler handler;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
		ctx = this;
		act = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        handler = new android.os.Handler();
        
		//Init command dictionary
		initDictionary();
		
		//Text to speech
		Intent check = new Intent();
	    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(check, TTS_CHECK);
	    
	    Bundle bundle = getIntent().getExtras();
	    actualModo = bundle.getString("modo");
	    initializeServices(actualModo);
        
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
		Log.w("RODRILOG", ">> InitializeServices Camera");
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
  		
  		Log.w("RODRILOG", "<< InitializeServices Camera");
        
		return ((FrameLayout) findViewById(R.id.layout));
	}
	
	private CommandCamera textOnTakePicture = new CommandCamera(){

		@Override
		public int runCommand(byte[] data, Camera camera) {
			
			ImageComparator textComparator = new ImageComparator();
			
			File pictureFile = CommonMethods.getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.e("TAG",
						"Error creating media file, check storage permissions: pictureFile== null");
				return -1;
			}
			
			speakWithoutRepetir("Imagen capturada. Aguarde mientras se procesa.");
			
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
				
			Bitmap bitmap = BitmapFactory.decodeFile(textComparator.textPreprocess(pictureFile.getPath(), true), options );
			
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			TessBaseAPI baseApi = new TessBaseAPI();
			// DATA_PATH = Path to the storage
			// lang = for which the language data exists, usually "eng"
			
			baseApi.init("/storage/sdcard0/BlindLess/", "eng");
//			baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmnñopqrstuvwxyzABCDEFGHIJKLMNÑOPQRSTUVWXYZ0123456789%$@#,.()");
//			baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!^&*()_+=-[]}{;:'\"\\|~`,./<>?");
			
			baseApi.setImage(bitmap);
			String recognizedText = baseApi.getUTF8Text();
			speak(recognizedText);
			
			speak("Texto leído.");
			initializeSpeech();
			startRecognition();
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
			
			cleanTimer();
			cleanSpeecher();
//			
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				
			} catch (FileNotFoundException e) {
				Log.e("TAG", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.e("TAG", "Error accessing file: " + e.getMessage());
			}
			Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
			
//			textComparator.textPreprocess(pictureFile.getPath(), false);
						
			List<String> billetes = new ArrayList<String>();
			billetes.add(pictureFile.getPath());	
			
			//Old Method to detect supizq value from picture
			if (MatchPatternsFor(CommonMethods.SUPIZQ_VAL, billetes) > 0) return endTakePic(billetes);
			if (MatchPatternsFor(CommonMethods.MEDIO_TEXT, billetes) > 0)  return endTakePic(billetes);
			if (MatchPatternsFor(CommonMethods.INFDER_VAL, billetes) > 0) return endTakePic(billetes);
//			if (MatchPatternsFor(CommonMethods.MEDIO_VAL, billetes) > 0) return 1;
//			if (MatchPatternsFor(CommonMethods.SUPIZQ_TEXT, billetes) > 0) return 1;
//			if (MatchPatternsFor("infder", billetes) > 0) return 1;
			
			speak("El billete no pudo ser reconocido. Realice otra captura por favor."); //reinicializa el speech
			return endTakePic(billetes);
		}

		private int endTakePic(List<String> billetes) {
			initializeSpeech();
			startRecognition();
			billetes.clear();
			return 1;
		}
		
		private CommandRead readSupIzqCommand = new CommandRead() {
			@Override
			public BestMatches runCommand(ImageComparator comparator, Mat billeteToCheck, String templateToCheck,
					String outFile) {
				return comparator.readSupIzq(billeteToCheck, templateToCheck, outFile);
			}
		};
		
		private CommandRead readCenterCommand = new CommandRead() {
			@Override
			public BestMatches runCommand(ImageComparator comparator, Mat billeteToCheck, String templateToCheck,
					String outFile) {
				return comparator.readCenter(billeteToCheck, templateToCheck, outFile);
			}
		};

		private int MatchPatternsFor(String pattern, List<String> billetes) {
			List<String> templates = new ArrayList<String>();
			addTemplatesValue("2", pattern, templates);
			addTemplatesValue("5", pattern, templates);
			addTemplatesValue("10", pattern, templates);
			addTemplatesValue("20", pattern, templates);
			addTemplatesValue("50", pattern, templates);
			addTemplatesValue("100", pattern, templates);
			
//			if (pattern.equals(CommonMethods.SUPIZQ_TEXT)){
//				return matchAndRead(billetes, templates, false, CommonMethods.NUMERO_BILLETE, readSupIzqCommand, false);
//			}
//			else 
			if (pattern.equals(CommonMethods.SUPIZQ_VAL)){
				int rtn = matchSupIzq(billetes, templates, true, MINVAL_SUPPORTED);
				templates.clear();
				return rtn;
			}
			if (pattern.equals(CommonMethods.INFDER_VAL)){
				int rtn =  matchSupIzq(billetes, templates, true, MINVAL_SUPPORTED_2);
				templates.clear();
				return rtn;
			}
//			if (pattern.equals(CommonMethods.MEDIO_VAL)){
//				return matchSupIzq(billetes, templates, true);
//			}
//			}else 
			if (pattern.equals(CommonMethods.MEDIO_TEXT)){
				int rtn = matchAndRead(billetes,templates, true, CommonMethods.LETRAS_BILLETE, readCenterCommand, true);
				templates.clear();
				return rtn;
			}
			return 0;
		}
		
		private int matchSupIzq(List<String> billetes, List<String> templates, boolean maxFound, double minvalSupported) {
			int match_method = Imgproc.TM_CCOEFF;
			return startComparisson(billetes, templates, match_method, maxFound, minvalSupported, new CommandComparisson() {
				
				@Override
				public double runCommand(ImageComparator comparator, Mat billeteToCheck, String templateToCheck,
						String outFile, String templateToWrite, int match_method, String description) {
					return comparator.comparateSupIzq(billeteToCheck, templateToCheck, outFile, templateToWrite, match_method, description);
				}
			});
		}

		private int matchAndRead(List<String> billetes, List<String> templates, boolean contains, String whiteList, CommandRead readCommand, boolean alPrimerMatch) {
			/*INICIALIZO TESSERACT*/
			ImageComparator comparator = new ImageComparator();
			TessBaseAPI baseApi = new TessBaseAPI();
			baseApi.init("/storage/sdcard0/BlindLess/", "spa");
			baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whiteList);
			double maxVal = 0.0;
			String templateGanador = "";
			ArrayList<String> billetesLeidos = new ArrayList<String>();
			for (String billeteToCheck : billetes) {
				Mat img_billete = comparator.getImageToProcess(billeteToCheck);
				for (String template : templates) {
					String templateToCheck = "storage/sdcard0/BlindLess/Templates/" + template + ".jpg";
					String outFile = "storage/sdcard0/BlindLess/Resultados/Resultadomedio" + 
						billeteToCheck.substring(billeteToCheck.length() - 9, billeteToCheck.length() - 1) 
						+ "_" + template + ".jpg";
					BestMatches bestMatch = readCommand.runCommand(comparator, img_billete.clone(), templateToCheck, outFile);
					if (bestMatch.getImage() == null) continue;
					baseApi.setImage(bestMatch.getImage());
					String textoLeido = baseApi.getUTF8Text();
					Log.w("BLINDLESSTEST","Leyó: " + textoLeido);
					String billeteReconocido = CommonMethods.esBilleteValido(textoLeido, contains);
					
					if (!billeteReconocido.equals("")) {
						if (alPrimerMatch) {
							return leerBilleteFinal(billeteReconocido);
						}else {
							billetesLeidos.add(billeteReconocido);
						}
						
					}

					//Save best match
					if (bestMatch.getMaxVal() > maxVal) {
						templateGanador = template.substring(0, template.indexOf('_'));
						maxVal = bestMatch.getMaxVal();
					}
					bestMatch.release();
				}
				img_billete.release();
			}
			
			return ReadBilletesLeidos(billetesLeidos);
		}


		//No usado por ahora, pero sirve para hacer la vieja comparación

		private int ReadBilletesLeidos(List<String> billetesLeidos) {
			int max = 0;
			String billeteReconocido = "";
			for (String billete : billetesLeidos) {
				int occurrences = Collections.frequency(billetesLeidos, billete);
				if (occurrences > max){
					billeteReconocido = billete;
				}
			}
			if (!billeteReconocido.equals("")) {
				billetesLeidos.clear();
				return leerBilleteFinal(billeteReconocido);
			}
			
			Log.w("BLINDLESSTEST","No se encontró patrón amigo");
			billetesLeidos.clear();
			return 0;
		}

		private int leerBilleteFinal(String billeteReconocido) {
			Log.w("BLINDLESSTEST","Finalmente es de: " + billeteReconocido);
			speak("Es un billete de: " + billeteReconocido + " pesos");
			return 1;
		}

		private int startComparisson(List<String> billetes,
				List<String> templates, int match_method, boolean maxFound, double minvalSupported, CommandComparisson comparisson) {
			double maxVal, val;
			String templateGanador, actualTemplate, templateNumber = "";
			for (String billeteToCheck : billetes) {
				maxVal = 0.0;
				val = 0.0;
				templateGanador = "";
				actualTemplate = templates.get(0).substring(0, templates.get(0).indexOf('_'));
				String descripcionBillete = billeteToCheck.substring(billeteToCheck.length() - 12, billeteToCheck.length() - 1);
				ImageComparator comparator = new ImageComparator();
				Mat img_billete = comparator.getImageToProcess(billeteToCheck);
				for (String template : templates) {	
					templateNumber = template.substring(0, template.indexOf('_'));
					String templateToCheck = "storage/sdcard0/BlindLess/Templates/" + template + ".jpg";
					String templateToWrite = "storage/sdcard0/BlindLess/Templates/" + template + "_canny.jpg";
					String outFile = "storage/sdcard0/BlindLess/Resultados/" + descripcionBillete + "_" + template;
					double valAux = comparisson.runCommand(comparator, img_billete.clone(), templateToCheck, outFile, templateToWrite, 
							match_method, "Billete: " + descripcionBillete + ", Template: " + template);
						
					if (maxFound){
						//Busca el valor más alto de match
						if (valAux > val){
							val = valAux;
							templateGanador = templateNumber;
						}
					}else {
						//Acumula el valor para ver de entre todos cual es el mejor
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
					
				}
				if (maxFound){
					maxVal = val;
				}else{
					//Acumula el valor para ver de entre todos cual es el mejor
					if (val > maxVal && val > 1.5)
					{
						maxVal = val;
						templateGanador = templateNumber;
					}
				}
				
				if (maxVal > minvalSupported) {
					Log.w("BLINDLESSTEST","Es un billete de: " + templateGanador + " MaxVal: " + maxVal);
					speak("Es un billete de: " + templateGanador + " pesos");
					return 1;
				}
				
				img_billete.release();
			}
			
			return 0;
		}
		
		private void addTemplatesValue(String value, String pattern, List<String> templates) {
			templates.add(value + "_" + pattern + "_" + 40);
//			templates.add(value + "_" + pattern + "_" + 60);
//			templates.add(value + "_" + pattern + "_" + 80);
		}
		
	};
	

    

	
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
		if (CommonMethods.verificaConexion(this))
		{
			Log.w("RODRILOG", ">> InitializeSpeech Camera");
			mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(CameraActivity.this);
			mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					"es-ES");

			SpeechRecognitionListener listener = 
					new SpeechRecognitionListener(mSpeechRecognizer, commandDictionary, new Command() {
											public void runCommand() { 
												Log.w("RODRILOG", ">> InitializeSpeechOnError Camera");
												if(mSpeechRecognizer != null) mSpeechRecognizer.destroy();
												initializeSpeech();
												startRecognition();
												Log.w("RODRILOG", "<< InitializeSpeechOnError Camera");
											};
	        });
			mSpeechRecognizer.setRecognitionListener(listener);
			Log.w("RODRILOG", "<< InitializeSpeech Camera");
		}
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
            	speakWithoutRepetir("Dijiste volver"); 
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
            	speakWithoutRepetir("Dijiste salir"); 
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
	
	public void speakWithoutRepetir(String text){
		mIsSpeaking = true;
		cleanTimer();
		if(speaker != null) speaker.speak(text);
		mIsSpeaking = false;
	}

    public void mensajePrincipal(){
//		speak("Pronuncie el comando ayuda para iniciar la guía de"
//			+ "detección o el comando volver para retornar al Menú principal");
    	speak("Mensaje principal");
    }
	
    //repite el mensaje principal cada x cantidad de segundos, si no hubo interacción del usuario.
    public void repetirMensajePrincipal(int seg1, int seg2) {
    	Log.w("RODRILOG", ">> RepetirMensajePrincipal Camera");
    	cleanTimer();
    	task = new TimerTask() {
  		   	@Override
  		   	public void run() {
  		   		handler.post(new Runnable() {
  		   			public void run() {
  		   				Log.w("RODRILOG", ">> Repitiendopapi Camera");
  		   				cleanSpeecher();
  		   				mensajePrincipal();
  		   				initializeSpeech();
  		   				startRecognition();
  		   				Log.w("RODRILOG", "<< Repitiendopapi Camera");
  		   			};
  		   		});
  		   	}
  		};
		timer = new Timer();
		timer.schedule(task,seg1,seg2);
		Log.w("RODRILOG", "<< RepetirMensajePrincipal Camera");
    }
    
	public void startRecognition(){
		Log.w("RODRILOG", ">> StartRecognition Camera");
		if (!mIsSpeaking)
		{
			Log.i("Speech", "Starting listening");
			if(mSpeechRecognizer != null)
			{
				mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
			}
		}
	}
	
	public void cleanSpeecher() {
		Log.w("RODRILOG", ">> CleanSpeecher Camera");
	    if(mSpeechRecognizer != null)
	    {
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              
	    }
	    mSpeechRecognizer = null;
	}
	
	public void cleanTimer() {
		Log.w("RODRILOG", ">> cleanTimer Camera");
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
	    initializeServices(actualModo);
	    Log.i("MainActivity","onRestartLeaving");
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
