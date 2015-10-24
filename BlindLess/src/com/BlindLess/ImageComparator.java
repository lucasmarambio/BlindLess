package com.BlindLess;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

public class ImageComparator extends Activity{

	
	public double comparate(Mat img_preprocesed, Mat templ_preprocesed, int match_method,String outFile, String description) {
		
		try {

	        // / Create the result matrix 
	        int result_cols = img_preprocesed.cols() - templ_preprocesed.cols() + 1;
	        int result_rows = img_preprocesed.rows() - templ_preprocesed.rows() + 1;
	        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
	
	
	        // / Do the Matching and Normalize
	        Imgproc.matchTemplate(img_preprocesed, templ_preprocesed, result, match_method);
	
	        // / Localizing the best match with minMaxLoc
	        MinMaxLocResult mmr = Core.minMaxLoc(result);
	
	        Point matchLoc;
	        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
	            matchLoc = mmr.minLoc;
	            Log.w("BLINDLESSTEST","***" + description + "-->" + "MinValue: " + mmr.minVal);
	        } else {
	            matchLoc = mmr.maxLoc;
	            Log.w("BLINDLESSTEST","***" + description + "-->" + "MaxValue: " + mmr.maxVal);
	        }

	        // / Show me what you got
	        Imgproc.rectangle(img_preprocesed, matchLoc, new Point(matchLoc.x + templ_preprocesed.cols(),
	                matchLoc.y + templ_preprocesed.rows()), new Scalar(0, 255, 0));
	
//	        // Save the visualized detection.
	        Imgcodecs.imwrite(outFile, img_preprocesed);
//	        Log.i("Image Comparator", "termino la comparacion");
	        
	        return mmr.maxVal;
		}catch (Exception e)
		{
			Log.e("***" + description + "***", "No se pudo leer este");
			return 0.0;
		}
      
	}


	public double comparateSupIzq(String inFile, String templateFile, String outFile, int match_method, String description) {
		try {
			/*LEO LA IMAGEN Y EL TEMPLATE*/
			Mat img_preprocesed = Imgcodecs.imread(inFile);
			Mat templ_preprocesed = Imgcodecs.imread(templateFile);
		
			//Preprocess image to grayScale
			Imgproc.resize(img_preprocesed, img_preprocesed, new Size(620, 344));
		    Imgproc.cvtColor(img_preprocesed, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
		    
		    //Preprocess template to grayScale
			Imgproc.cvtColor(templ_preprocesed, templ_preprocesed, Imgproc.COLOR_BGR2GRAY);
			
			return comparate(img_preprocesed, templ_preprocesed, match_method, outFile, description);
		} catch (Exception e) {
			Log.e("***" + description + "***", "No se pudo leer este");
			return 0.0;
		}
	}
	
	public Bitmap readSupIzq(String billeteToCheck, String templateToCheck, String outFile){
			
		/*LEO IMAGEN ORIGINAL (BILLETE) Y TEMPLATE ORIGINAL*/
		Mat img_billete = Imgcodecs.imread(billeteToCheck);
		Mat img_template = Imgcodecs.imread(templateToCheck);
		
		/*CONVIERTO LA IMAGEN Y EL TEMPLATE A ESCALA DE GRISES*/
		Mat img_preprocesed = new Mat(img_billete.size(),CvType.CV_8UC1);
		Mat temp_preprocesed = new Mat(img_template.size(),CvType.CV_8UC1);
		Imgproc.cvtColor(img_billete, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(img_template, temp_preprocesed, Imgproc.COLOR_BGR2GRAY);
		
		/*APLICO MATCH TEMPLATE*/
        int result_cols = img_preprocesed.cols() - temp_preprocesed.cols() + 1;
        int result_rows = img_preprocesed.rows() - temp_preprocesed.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
		Imgproc.matchTemplate(img_preprocesed, temp_preprocesed, result, Imgproc.TM_CCOEFF_NORMED);

        /*LOCALIZO EL BEST MATCH CON minMaxLoc*/
        MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;
        
//        Descomentar para Ver que fue lo que encontró
//        Imgproc.rectangle(img_preprocesed, matchLoc, new Point(matchLoc.x + temp_preprocesed.cols(),
//                matchLoc.y + temp_preprocesed.rows()), new Scalar(0, 255, 0));
//        Imgcodecs.imwrite(outFile, img_preprocesed);
        
        /* GUARDO EL ROI EN UNA NUEVA IMAGEN (EL SUPIZQ ENCONTRADO) => IMAGEN PREPROCESADA RECORTADA EN EL AREA DE TEMPLATE DETECTADO */
        Mat ROI=img_preprocesed.submat((int)matchLoc.y,(int)(matchLoc.y + temp_preprocesed.rows()),(int)matchLoc.x,(int)(matchLoc.x + temp_preprocesed.cols()));
        Imgcodecs.imwrite(outFile, ROI);
       
		/*CONVIERTO El ROI A BITMAP PARA USAR TESSERACT Y CORROBORAR COINCIDENCIA CON EL TEMPLATE ORIGINAL*/ 
        Bitmap bmp_temp_2 = Bitmap.createBitmap(ROI.cols(), ROI.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(ROI, bmp_temp_2);
		bmp_temp_2 = bmp_temp_2.copy(Bitmap.Config.ARGB_8888, true);
		return bmp_temp_2;
	}
// TRAIT IMAGE
//    Imgproc.GaussianBlur(img_preprocesed, img_preprocesed, new Size(15, 15), 1, 0);
//    Imgproc.equalizeHist(img_preprocesed, img_preprocesed);
//    Imgproc.Canny(img_preprocesed, img_preprocesed, 100, 200);

//templ_preprocesed = traitImage(templ_preprocesed);
	

}
