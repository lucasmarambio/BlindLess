package com.BlindLess;


import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;

import com.BlindLess.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
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
	private VoiceRecognition voiceRecognition;
    public Speaker speaker; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		button = (Button)findViewById(R.id.button1);
		buttonCamera = (Button)findViewById(R.id.buttonCamera);
		
		//TTS Check
		Intent check = new Intent();
	    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(check, TTS_CHECK);
		
		buttonCamera.setOnClickListener( new ButtonClickHandler() );
		
//		startRecognizeSpeech();
		
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
	
    public class ButtonClickHandler implements View.OnClickListener 
    {
    	public void onClick( View view ){
    		switch (view.getId()) {
			case R.id.buttonCamera:
//				SingletonTextToSpeech.getInstance(getApplicationContext()).sayHello("Iniciando Cámara");
				speaker.speak("Hola Mundo");
		    	Intent intent = new Intent(getApplicationContext(), CameraActivity.class );
		    	startActivity(intent);
				break;
			default:
				break;
			}
    	}
    }
    
    
    private void startRecognizeSpeech() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");

        try {
            startActivityForResult(intent, RESULT_SPEECH);

        } catch (ActivityNotFoundException a) {
            Toast.makeText(
                    getApplicationContext(),
                    "Oops! First you must download \"Voice Search\" App from Store",
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    
    @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);

     switch (requestCode) {
     case RESULT_SPEECH: {
         if (resultCode == RESULT_OK && null != data) {
             // text is received form google
             ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
             for (String string : text) {
				string = string + "";
			}
  //you can find your result in text Arraylist  
         }
         break;
     }
     case TTS_CHECK:{
    	 if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
             speaker = new Speaker(this);
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
    
