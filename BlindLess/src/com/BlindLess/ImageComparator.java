package com.BlindLess;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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

		Mat img = Imgcodecs.imread(inFile);
		Mat templ = Imgcodecs.imread(templateFile);
//		Mat otro = new Mat(img.rows(), img.cols(), CvType.CV_8UC1, new Scalar(0));
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Mat e = new Mat();


        // apply gaussian blur to smoothen lines of dots
        Imgproc.GaussianBlur(img, img, new Size(11, 11), 0);
        Imgcodecs.imwrite("storage/sdcard0/PatronesBilletes/Billete 2 pesos/2_billete_posta4.jpg", img);

//        // Applying Canny
//        Imgproc.Canny(otro, otro, 80, 100);
//        
//        MatOfPoint2f mat2fsrc = new MatOfPoint2f(), mat2fdst = new MatOfPoint2f();
//        Scalar color =  new Scalar(250, 250, 255);
//
////        Mat lines = new Mat();
////        int threshold = 100;
////        int minLineSize = 150;
////        int lineGap = 40;
////
////        Imgproc.HoughLinesP(otro, lines, 1, Math.PI / 180, threshold,
////                minLineSize, lineGap);
//        Imgproc.findContours(otro, contours,e , Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        
//        for(int i=0; i< contours.size();i++){
//            if (Imgproc.contourArea(contours.get(i)) > 50 ){
//                Rect rect = Imgproc.boundingRect(contours.get(i));
//                if (rect.height > 28){
//                    Imgproc.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
//                    Mat ROI = img.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
//
//                    Imgcodecs.imwrite("storage/sdcard0/PatronesBilletes/Resultado2.jpg",ROI);
//
//                }
//            }
//        }
        
//        for (int i = 0; i < contours.size(); i++) {
//            contours.get(i).convertTo(mat2fsrc, CvType.CV_32FC2);
//            Imgproc.approxPolyDP(mat2fsrc, mat2fdst, 0.01 * Imgproc.arcLength(mat2fsrc, true), true);
//            mat2fdst.convertTo(contours.get(i), CvType.CV_32S);
//            Imgproc.drawContours(otro, contours, i, color, 2, 8, e, 0, new Point());
//        }
//        
//        Imgcodecs.imwrite("storage/sdcard0/PatronesBilletes/Resultado2.jpg",otro);
        
        // / Create the result matrix 
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);


        // / Do the Matching and Normalize
        Imgproc.matchTemplate(img, templ, result, match_method);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // / Localizing the best match with minMaxLoc
        MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }
        
        // / Show me what you got
        Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

        // Save the visualized detection.
        Imgcodecs.imwrite(outFile, img);
        Log.i("Image Comparator", "termino la comparacion");
        //System.out.println("Writing "+ outFile);
      
	}
}
