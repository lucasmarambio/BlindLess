package com.BlindLess;

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
	
// TRAIT IMAGE
//    Imgproc.GaussianBlur(img_preprocesed, img_preprocesed, new Size(15, 15), 1, 0);
//    Imgproc.equalizeHist(img_preprocesed, img_preprocesed);
//    Imgproc.Canny(img_preprocesed, img_preprocesed, 100, 200);

//templ_preprocesed = traitImage(templ_preprocesed);
	

}
