package com.BlindLess;

import java.util.ArrayList;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class SpeechRecognitionListener implements RecognitionListener
{
	private SpeechRecognizer mSpeechRecognizer;
	private Map<String, Command> commandDictionary;
	private Command onError;
	
    public SpeechRecognitionListener(SpeechRecognizer recognizer, Map<String, Command> dictionary, Command onError) {
		this.mSpeechRecognizer = recognizer;
		this.onError = onError;
		this.commandDictionary = dictionary;
	}

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
		        onError.runCommand();
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
        
        for (String word : matches) {
        	if (word.length() > 0){
        		Log.e("Speech", "String read = " + word);
        		if (commandDictionary.containsKey(word.toLowerCase()))
        		{
        			this.commandDictionary.get(word).runCommand();
            		return;
        		}
        	}
		}
        this.commandDictionary.get("nada").runCommand();
    }

    @Override
    public void onRmsChanged(float rmsdB)
    {
    }
}
