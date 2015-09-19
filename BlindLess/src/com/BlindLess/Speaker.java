package com.BlindLess;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class Speaker implements OnInitListener {
	 
    private TextToSpeech tts;
    public Command runOnInit;
     
    public boolean ready = false;
     
    private boolean allowed = false;
     
    public Speaker(Context context){
        tts = new TextToSpeech(context, this);      
    }   
     
    public boolean isAllowed(){
        return allowed;
    }
     
    public void allow(boolean allowed){
        this.allowed = allowed;
    }

	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS){
	        // Change this to match your
	        // locale
			Locale locSpanish = new Locale("spa", "ARG");
			tts.setLanguage(locSpanish);
	        ready = true;

	        this.speak("Bienvenidos a BlindLess");
	        runOnInit.runCommand();
	    }else{
	        ready = false;
	    }
	}
	
	public void speak(String text){
	     
	    // Speak only if the TTS is ready
	    // and the user has allowed speech
	     
	    if(ready) {
	        HashMap<String, String> hash = new HashMap<String,String>();
	        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, 
	                String.valueOf(AudioManager.STREAM_NOTIFICATION));
	        tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
	        try {
	        	while (tts.isSpeaking() ) {
	            };
	            Log.i("Speaker", "Wait 1 sec");
	            Thread.sleep(1000);
	            Log.i("Speaker", "Stop Waiting");
	        } catch(Exception ex) {
	            Thread.currentThread().interrupt();
	        }
	    }
	}
	
	// Free up resources
	public void destroy(){
	    tts.shutdown();
	}
	
	public void pause(int duration){
	    tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
	}
}