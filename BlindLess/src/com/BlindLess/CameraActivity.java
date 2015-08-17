package com.BlindLess;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class CameraActivity extends Activity {
	
	private boolean pictureTaken;
	private ImageView imagePicture;
	private String path;
	public static final int CAMERA_TEXT_REQUEST_CODE = 1;
	public Thread thread;
	private ThreadGroup rootThreadGroup = null;
	private static final String PHOTO_TAKEN	= "photo_taken";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        Thread[] allThreads = null;
//        
//        ThreadGroup tg = getRootThreadGroup();
//        tg.enumerate(allThreads);
//        if (allThreads != null) {
//        	for (Thread th : allThreads) {
//
//				Log.i("MakeMachine", th.getName() );
//    			if (th.getName() == "tito") {
//    				thread = th;
//    			}
//    		}
//		}
//        MainActivity.tts.sayHello("hola");
//        ((TextToSpeechActivity) thread).sayHello("Hola");
        startCameraActivity();
    }
	
	ThreadGroup getRootThreadGroup( ) {
	    if ( rootThreadGroup != null )
	        return rootThreadGroup;
	    ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
	    ThreadGroup ptg;
	    while ( (ptg = tg.getParent( )) != null )
	        tg = ptg;
	    return tg;
	}
	
	protected void startCameraActivity()
    {
    	Log.i("MakeMachine", "startCameraActivity()" );
    	path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/texto.jpg";
    	File file = new File(path);
    	Uri outputFileUri = Uri.fromFile( file );
    	
    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
    	cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
    	
    	startActivityForResult( cameraIntent, CAMERA_TEXT_REQUEST_CODE);
    }    
  
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {	
    	Log.i( "MakeMachine", "resultCode: " + resultCode ); 	
    	
    	if (resultCode == RESULT_OK){
    		Log.i( "MakeMachine", "User OK" );
			onPhotoTaken();		
    	} else if (resultCode == RESULT_CANCELED)
    	if (resultCode == RESULT_CANCELED){
    		Log.i( "MakeMachine", "User cancelled");
    	}
    	else {
    		Log.i( "MakeMachine", "nada" );
    		
    	}
    }
 
    protected void onPhotoTaken()
    {
    	Log.i( "MakeMachine", "onPhotoTaken" );
    	
    	pictureTaken = true;
    	
    	BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
    	
    	Bitmap bitmap = BitmapFactory.decodeFile(path, options );
    	
    	imagePicture.setImageBitmap(bitmap);
  
//    	field.setVisibility( View.GONE );
    }
    
    @Override 
    protected void onRestoreInstanceState( Bundle savedInstanceState){
    	Log.i( "MakeMachine", "onRestoreInstanceState()");
    	if( savedInstanceState.getBoolean( CameraActivity.PHOTO_TAKEN ) ) {
    		onPhotoTaken();
    	}
    }
    
    @Override
    protected void onSaveInstanceState( Bundle outState ) {
    	outState.putBoolean( CameraActivity.PHOTO_TAKEN, pictureTaken );
    }
	

}
