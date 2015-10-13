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
	
	public void comparate(String inFile, String templateFile, String outFile, int match_method) {

		/*PATHS DE GRABACIONES*/
		String path_contours = "storage/sdcard0/PatronesBilletes/contours.jpg";
		String path_erate = "storage/sdcard0/PatronesBilletes/erate.jpg";
		String path_canny = "storage/sdcard0/PatronesBilletes/canny.jpg";
		String path_blur = "storage/sdcard0/PatronesBilletes/blur.jpg";
		String path_drawing = "storage/sdcard0/PatronesBilletes/drawing.jpg";
		String path_grey_draw = "storage/sdcard0/PatronesBilletes/grey_drawing.jpg";
		String path_bw = "storage/sdcard0/PatronesBilletes/bw.jpg";
		String path_ht = "storage/sdcard0/PatronesBilletes/ht.jpg";
		String path_gris = "storage/sdcard0/PatronesBilletes/gris.jpg";
		String path_dilate = 	"storage/sdcard0/PatronesBilletes/dilate.jpg";
		String path_lines = 	"storage/sdcard0/PatronesBilletes/lines.jpg";
		
		/*LEO LA IMAGEN Y EL TEMPLATE*/
		Mat img_original = Imgcodecs.imread(inFile);
		Mat templ_original = Imgcodecs.imread(templateFile);
		
		/*CONVIERTO LA IMAGEN Y EL TEMPLATE A ESCALA DE GRISES*/
		Mat img_preprocesed = new Mat(img_original.size(),CvType.CV_8UC1);
		Mat templ_preprocesed = new Mat(templ_original.size(), CvType.CV_8UC1);
//		Imgproc.cvtColor(img_original, img_preprocesed, Imgproc.COLOR_RGB2HSV);
//		List<Mat> channels = new ArrayList<Mat>();
//		Core.split(img_preprocesed, channels);
//		img_preprocesed = channels.get(0);
		Imgproc.cvtColor(img_original, img_preprocesed, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(templ_original, templ_preprocesed, Imgproc.COLOR_RGB2GRAY);
		Imgcodecs.imwrite(path_gris, img_preprocesed);
		
		/*CONVIERTO LA IMAGEN Y EL TEMPLATE A BLANCO Y NEGRO*/
/*		Imgproc.adaptiveThreshold(img_preprocesed, img_preprocesed, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,15, 40);
		Imgproc.adaptiveThreshold(templ_preprocesed, templ_preprocesed, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,15, 40);
		Imgcodecs.imwrite(path_bw, img_preprocesed);*/
		
		Imgproc.medianBlur(img_preprocesed, img_preprocesed, 5);
		Imgcodecs.imwrite(path_blur, img_preprocesed);
				
		/*APLICO CANNY EN LA IMAGEN Y EL TEMPLATE*/
		int thresh = 100;
		Imgproc.Canny(img_preprocesed, img_preprocesed, thresh, thresh*2);
		Imgproc.Canny(templ_preprocesed, templ_preprocesed, thresh, thresh*2);
		Imgcodecs.imwrite(path_canny, img_preprocesed);
		
		/*APLICO DILATE */
		Mat dst = img_preprocesed.clone();
		int dilatacion_size = 5;
		Size s = new Size(2*dilatacion_size + 1, 2*dilatacion_size+1);
		Point p = new Point(dilatacion_size, dilatacion_size);
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, s, p);
		Imgproc.dilate(img_preprocesed, img_preprocesed, element);	
		
		/*APLICO GAUSSIAN BLUR EN LA IMAGEN Y EL TEMPLATE*/
/*		Imgproc.GaussianBlur(img_preprocesed, img_preprocesed, new Size(11, 11), 0);
		Imgproc.GaussianBlur(templ_preprocesed, templ_preprocesed, new Size(11, 11), 0);
		Imgcodecs.imwrite(path_blur, img_preprocesed);*/
			
		/*APLICO LA DETECCION DE CONTORNOS EN LA IMAGEN*/
		Mat hierarchy = new Mat();
		Scalar color =  new Scalar(250, 250, 255);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(img_preprocesed, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgcodecs.imwrite(path_contours, img_preprocesed);
        
        /*BUSCO EL CONTORNO DE MAYOR AREA => ES EL RECTANGULO QUE FORMA EL BILLETE*/
        double maxArea = -1;
        int maxAreaIdx = -1;

        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f maxCurve = new MatOfPoint2f();
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                // Imgproc.drawContours(untouched, contours, maxAreaIdx, new Scalar(0, 255, 0), 1); 
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);
                if (approxCurve.total() == 4) {
                    maxCurve = approxCurve;
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    largest_contours.add(temp_contour);
                }
            }
        }
        Imgproc.drawContours(img_original, contours, maxAreaIdx, new Scalar(0, 255, 0), 5);
        Imgcodecs.imwrite(path_drawing, img_original);
        
        /* DIBUJO EL CONTORNO DE MAYOR AREA EN LA IMAGEN ORIGINAL */
        Imgproc.drawContours(img_original, contours, maxAreaIdx, new Scalar(40, 233, 45,0 ));
        Imgcodecs.imwrite(path_drawing, img_original);
        
        /* DIBUJO TODOS LOS CONTORNOS OBTENIDOS EN LA IMAGEN ORIGINAL */
        for (int i = 0; i < contours.size(); i++) {
        	Imgproc.drawContours(img_original, contours, i, new Scalar(40, 233, 45,0 ));
        }
        Imgcodecs.imwrite(path_drawing, img_original);
        
        /* GUARDO EL ROI EN UNA NUEVA IMAGEN => IMAGEN ORIGINAL RECORTADA POR EL CONTORNO OBTENIDO */
        Rect rect = Imgproc.boundingRect(contours.get(maxAreaIdx));
        Mat ROI = img_original.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
        Imgcodecs.imwrite(path_drawing, ROI);
        
        
        Imgcodecs.imwrite(path_drawing, img_original);
        
        /* DIBUJO EL CONTORNO DE MAYOR AREA EN LA IMAGEN ORIGINAL*/
        Imgproc.drawContours(img_original, contours, maxAreaIdx, new Scalar(40, 233, 45,0 ));
        Imgcodecs.imwrite(path_drawing, img_original);
        
      
              
        // / Create the result matrix 
        int result_cols = img_original.cols() - templ_original.cols() + 1;
        int result_rows = img_original.rows() - templ_original.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);


        // / Do the Matching and Normalize
        Imgproc.matchTemplate(img_original, templ_original, result, match_method);

        // / Localizing the best match with minMaxLoc
        MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }
        
        // / Show me what you got
        Imgproc.rectangle(img_original, matchLoc, new Point(matchLoc.x + templ_original.cols(),
                matchLoc.y + templ_original.rows()), new Scalar(0, 255, 0));

        // Save the visualized detection.
        Imgcodecs.imwrite(outFile, img_original);
        Log.i("Image Comparator", "termino la comparacion");
        //System.out.println("Writing "+ outFile);
      
	}
}
