package com.BlindLess;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class Speaker implements OnInitListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
	 
    private TextToSpeech tts;
    private String initMsg;
    public Command runOnInit;
     
    private boolean ready;
    public boolean initFinish;
     
    private boolean allowed = false;
     
    public Speaker(Context context, String msg){
        tts = new TextToSpeech(context, this);    
        initMsg = msg;
        this.ready = false;
        initFinish = false;
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
	        this.ready = true;

	        this.speak(this.initMsg);
	        
	        if (runOnInit != null) {
	        	runOnInit.runCommand();
			}

        	initFinish = true;
	        
	    }else{
	    	this.ready = false;
	    }
	}
	
	public void speak(String text){
	     
	    // Speak only if the TTS is ready
	    // and the user has allowed speech
	     
	    if(this.ready) {
	        HashMap<String, String> hash = new HashMap<String,String>();
	        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, 
	                String.valueOf(AudioManager.STREAM_NOTIFICATION));
	        tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
	        try {
	        	while (tts.isSpeaking() ) {
//	        		Log.i("Speaker", "Waiting");
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

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
}