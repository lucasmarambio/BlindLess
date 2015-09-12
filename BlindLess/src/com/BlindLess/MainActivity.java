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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
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
    public Speaker speaker; 
    
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
			
			//Init command dictionary
			initDictionary();
			
			//TTS Check
			Intent check = new Intent();
		    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		    startActivityForResult(check, TTS_CHECK);
			
			buttonCamera.setOnClickListener( new ButtonClickHandler() );
		
			initializeSpeech();
		    
		
		} catch (Exception e) {
			// TODO: hacer algo
		}	
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
		
		commandDictionary.put("camara", new Command() {
            public void runCommand() { speaker.speak("Dijiste cámara"); startRecognition(); };
        });
		commandDictionary.put("iniciar", new Command() {
            public void runCommand() { speaker.speak("Dijiste Iniciar"); startRecognition(); };
        });
		commandDictionary.put("billete", new Command() {
            public void runCommand() { speaker.speak("Dijiste billete"); startRecognition(); };
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
	
    public class ButtonClickHandler implements View.OnClickListener 
    {
    	public void onClick( View view ){
    		switch (view.getId()) {
			case R.id.buttonCamera:
//				SingletonTextToSpeech.getInstance(getApplicationContext()).sayHello("Iniciando Cámara");
				speaker.speak("Iniciando cámara");
		    	Intent intent = new Intent(getApplicationContext(), CameraActivity.class );
		    	startActivity(intent);
				break;
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
       
    
 }

    
