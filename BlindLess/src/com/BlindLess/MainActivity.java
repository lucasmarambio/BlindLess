package com.BlindLess;


import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.BlindLess.R;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{

	private static final int RESULT_SPEECH = 0;
	private static final int TTS_CHECK = 10;
	private TextView titleBlindless;
	private TextView field;
	private Button button;
	private Button buttonCamera;
	private Button buttonBillete;
	private VoiceRecognition voiceRecognition;
    public Speaker speaker; 
     
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent; 
    private boolean mIslistening; 
    private Map<Character, Command> methodMap = new HashMap<Character, Command>();

    
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
			//[FIN].
			
			initializeSpeech();
		    
		
		} catch (Exception e) {
			// TODO: hacer algo
		}	
		button.setOnClickListener(new View.OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
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


	private void initializeSpeech() {
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		                                 RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
		                                 this.getPackageName());


		SpeechRecognitionListener listener = new SpeechRecognitionListener();
		mSpeechRecognizer.setRecognitionListener(listener);
	}
	
	
	private void initDictionary() {
		
		methodMap.put('c', new Command() {
            public void runCommand() { speaker.speak("Dijiste cámara"); startRecognition(); };
        });
		methodMap.put('i', new Command() {
            public void runCommand() { speaker.speak("Dijiste Iniciar"); startRecognition(); };
        });
		methodMap.put('b', new Command() {
            public void runCommand() { speaker.speak("Dijiste billete"); startRecognition(); };
        });
		methodMap.put('z', new Command() {
            public void runCommand() { speaker.speak("Comando de voz no reconocido"); startRecognition(); };
        });
		
	}


	protected void onDestroy(Bundle savedInstanceState) {
	
		if (mSpeechRecognizer != null)
		{
		        mSpeechRecognizer.destroy();
		}
	}
	
	public void startRecognition(){
		Log.i("Speech", "StartRecognition call");
		if (!mIslistening)
		{
			Log.i("Speech", "Starting listening");
		    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}
	}
	
    public class ButtonClickHandler implements View.OnClickListener 
    {
    	public void onClick( View view ){
    		//[INICIO]    		
    	    String path_ocr;
    	    //[FIN]
    		
    	    switch (view.getId()) {
			case R.id.buttonCamera:
//				SingletonTextToSpeech.getInstance(getApplicationContext()).sayHello("Iniciando Cámara");
				speaker.speak("Iniciando cámara");
		    	Intent intent = new Intent(getApplicationContext(), CameraActivity.class );
		    	startActivity(intent);
				break;
//[INICIO] Comenzando con las pruebas para detectar texto.
			case R.id.buttonBillete:
				path_ocr = "/storage/sdcard0/Pictures/BlindLess Pics/BlindLess6.jpg";
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
				    options.inSampleSize = 4;
				    	
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
					baseApi.init("/storage/sdcard0/", "eng");
					//baseApi.init("/storage/sdcard0/tessdata/spa.traineddata", "spa");
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
        
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);

     switch (requestCode) {
     case TTS_CHECK:{
    	 Log.i("Main Activity", "TTS_Check");
    	 if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
             speaker = new Speaker(this);
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
       
    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech()
        {               
            //Log.d(TAG, "onBeginingOfSpeech"); 
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "onEndOfSpeech");
         }

        @Override
        public void onError(int error)
        {
            Log.e("Speech", "error = " + error);
    		if (mSpeechRecognizer != null)
    		{
    		        mSpeechRecognizer.destroy();
    		        initializeSpeech();
    		        startRecognition();
    		}
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {

        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            //Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results)
        {
            //Log.d(TAG, "onResults"); //$NON-NLS-1$
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            // matches are the return values of speech recognition engine
            // Use these values for whatever you wish to do
            
            for (String string : matches) {
            	if (string.length() > 0){
            		Log.e("Speech", "String read = " + string);
            		char word = string.charAt(0);
            		if (word == 'c' || word == 'b' || word == 'i')
            		{
            			methodMap.get(word).runCommand();
                		return;
            		}
            	}
			}
            methodMap.get('z').runCommand();
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
    }
 }

interface Command {
    void runCommand();
}
    
