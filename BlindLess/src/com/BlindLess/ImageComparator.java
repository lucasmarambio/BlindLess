package com.BlindLess;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
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

//	        // / Show me what you got
//	        Imgproc.rectangle(img_preprocesed, matchLoc, new Point(matchLoc.x + templ_preprocesed.cols(),
//	                matchLoc.y + templ_preprocesed.rows()), new Scalar(0, 255, 0));
//	
//	        // Save the visualized detection.
//	        Imgcodecs.imwrite(outFile, templ_preprocesed);
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
	
	public double comparateOtra(){
		/*CONVIERTO LA IMAGEN Y EL TEMPLATE A ESCALA DE GRISES*/
//		Mat img_preprocesed = new Mat(img_original.size(),CvType.CV_8UC1);
//		Mat templ_preprocesed = new Mat(templ_original.size(), CvType.CV_8UC1);
//		Imgproc.cvtColor(img_original, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
//		Imgproc.cvtColor(templ_original, templ_preprocesed, Imgproc.COLOR_BGR2GRAY);
////		Imgcodecs.imwrite(path_gris, img_preprocesed);
//		
////		CONVIERTO LA IMAGEN Y EL TEMPLATE A BLANCO Y NEGRO
//		Imgproc.adaptiveThreshold(img_preprocesed, img_preprocesed, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,15, 40);
//		Imgproc.adaptiveThreshold(templ_preprocesed, templ_preprocesed, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,15, 40);
//		Imgcodecs.imwrite(path_bw, img_preprocesed);
		
//		APLICO GAUSSIAN BLUR EN LA IMAGEN Y EL TEMPLATE
//		Imgproc.GaussianBlur(img_preprocesed, img_preprocesed, new Size(11, 11), 0);
//		Imgproc.GaussianBlur(templ_preprocesed, templ_preprocesed, new Size(11, 11), 0);
//		Imgcodecs.imwrite(path_blur, img_preprocesed);
			
/*		APLICO CANNY EN LA IMAGEN Y EL TEMPLATE
		int thresh = 100;
		Imgproc.Canny(img_preprocesed, img_preprocesed, thresh, thresh*2);
		Imgproc.Canny(templ_preprocesed, templ_preprocesed, thresh, thresh*2);
		Imgcodecs.imwrite(path_canny, img_preprocesed);
		
		APLICO LA DETECCION DE CONTORNOS EN LA IMAGEN
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(img_preprocesed, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        Imgcodecs.imwrite(path_contours, img_preprocesed);
        
        BUSCO EL CONTORNO DE MAYOR AREA => ES EL RECTANGULO QUE FORMA EL BILLETE
        double maxArea = -1;
        int maxAreaIdx = -1;
        Log.d("size",Integer.toString(contours.size()));
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint largest_contour = contours.get(0);
        //largest_contour.ge
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        //Imgproc.drawContours(imgSource,contours, -1, new Scalar(0, 255, 0), 1);

        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize*0.05, true);
                if (approxCurve_temp.total() == 4) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    approxCurve=approxCurve_temp;
                    largest_contour = temp_contour;
                }
            }
        }
        
        UNO LOS PUNTOS OBTENIDOS DE LA IMAGEN CON DRAWCONTOURS
        Imgproc.drawContours(img_original, contours, maxAreaIdx, new Scalar(40, 233, 45,0 ));
        Imgcodecs.imwrite(path_drawing, img_original);
        
        int i = 0;

        for (i = 0; i < contours.size(); i++) {
        	Imgproc.drawContours(img_original, contours, i, new Scalar(40, 233, 45,0 ));
        }
        
       Imgcodecs.imwrite(path_drawing, img_original);*/
		return 0;
	}
}
