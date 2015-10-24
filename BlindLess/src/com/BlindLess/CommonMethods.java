package com.BlindLess;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class CommonMethods {
	
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int DECIR_MSJ_PRINCIPAL = 30000;
    public static final int REPETIR_MSJ_PRINCIPAL = 30000;
	public static final String MODO_RECONOCIMIENTO_TEXTO = "Texto";
	public static final String MODO_RECONOCIMIENTO_BILLETE = "Billete";
	public static final String NUMERO_BILLETE = "0125";
	public static final String LETRAS_BILLETE = "acdeinostuvzACDEINOSTUVZ";
	private static String[] numeroBilletesReconocidos = {
		"2", "5", "10","20","50","100"
		};
	private static String[] textoBilletesReconocidos = {
		"dos","cinco","diez","veinte","cincuenta","cien"
		};
	
	
	public static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "BlindLess Pics");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.e("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new java.util.Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	
	public static String esBilleteValido(String texto, boolean contains){
		if (contains){
			return stringContainsItemFromList(texto, textoBilletesReconocidos);
		}else {
			return stringEqualsItemFromList(texto, numeroBilletesReconocidos);
		}
	}
	
	public static String stringEqualsItemFromList(String inputString, String[] items)
	{
	    for(int i =0; i < items.length; i++)
	    {
	        if(inputString.toUpperCase().equals(items[i].toUpperCase()))
	        {
	            return items[i];
	        }
	    }
	    return "";
	}
	
	public static String stringContainsItemFromList(String inputString, String[] items)
	{
	    for(int i =0; i < items.length; i++)
	    {
	        if(inputString.toUpperCase().contains(items[i].toUpperCase()))
	        {
	            return items[i];
	        }
	    }
	    return "";
	}
}
