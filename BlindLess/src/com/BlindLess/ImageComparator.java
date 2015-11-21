package com.BlindLess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class ImageComparator extends Activity{

	private Map<Integer, Mat> pictureCache = new HashMap<Integer, Mat>();
	
	public double comparate(Mat img_preprocesed, Mat templ_preprocesed, int match_method,String outFile, String description) {
		
		try {

	        // / Create the result matrix
	        MinMaxLocResult found = resizeToFindBestMatch(img_preprocesed,
					templ_preprocesed, match_method, description);
	
	        // / Show me what you got
//	        Point matchLoc = new Point(found.maxLoc.x * found.minVal, found.maxLoc.y * found.minVal); //found.minval sería el r!
//	        Point matchLoc2 = new Point((found.maxLoc.x + templ_preprocesed.size().width) * found.minVal, (found.maxLoc.y + templ_preprocesed.size().height) * found.minVal); //found.minval sería el r!
//	        Imgproc.rectangle(img_preprocesed, matchLoc, matchLoc2, new Scalar(0, 255, 0));
	
//	        // Save the visualized detection.
//	        Imgcodecs.imwrite(outFile + ".jpg", img_preprocesed);
//	        Log.i("Image Comparator", "termino la comparacion");
	        img_preprocesed.release();
	        templ_preprocesed.release();
	        
	        return found.maxVal;
		}catch (Exception e)
		{
			Log.e("***" + description + "***", "No se pudo leer este");
			return 0.0;
		}
      
	}


	private MinMaxLocResult resizeToFindBestMatch(Mat img_preprocesed,
			Mat templ_preprocesed, int match_method, String description) {
		MinMaxLocResult found = new MinMaxLocResult();
		int[] angleToSearch = new int[]{ 0,180 };
		found.maxVal = 0.0;
		Mat res = null;
		for (int angle : angleToSearch) {
			switch (angle % 360) {
	            case 0:
	            	res = templ_preprocesed;
	                break;
	            case 90:
	            	res = templ_preprocesed.t();
	                Core.flip(res, res, 1);
	                break;
	            case 180:
	            	Core.flip(templ_preprocesed, res, -1);
	                break;
	            case 270:
	                res = templ_preprocesed.t();
	                Core.flip(res, res, 0);
	                break;
//	            default:
//	                cv::Mat r = cv::getRotationMatrix2D({image.cols/2.0F, image.rows/2.0F}, degrees, 1.0);
//	                int len = std::max(image.cols, image.rows);
//	                cv::warpAffine(image, res, r, cv::Size(len, len));
//	                break; //image size will change
	        }
			
			templ_preprocesed = res;
			for (double i = 0; i < 15; i++) {
				Mat img_resized = img_preprocesed.clone();
				Mat img_cloned = img_preprocesed.clone();
				img_resized = resizeAndProcessPicture(img_preprocesed, i, img_resized);
				
				double r = img_preprocesed.width() / img_resized.width();
	
				if (img_resized.size().height < templ_preprocesed.size().height || img_resized.size().width < templ_preprocesed.size().width) {
					break;
				}
				
			    int result_cols = img_resized.cols() - templ_preprocesed.cols() + 1;
			    int result_rows = img_resized.rows() - templ_preprocesed.rows() + 1;
			    Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
				Imgproc.matchTemplate(img_resized, templ_preprocesed, result, match_method);
				// / Localizing the best match with minMaxLoc
			    MinMaxLocResult mmr = Core.minMaxLoc(result);
			    
			    Point matchLoc;
			    if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
			        matchLoc = mmr.minLoc;
			        Log.w("BLINDLESSTEST","***" + description + "_esla: " + i + "-->" + "MinValue: " + String.valueOf(mmr.minVal));//.substring(0, String.valueOf(mmr.minVal).indexOf('.')));
			    } else {
			        matchLoc = mmr.maxLoc;
			        Log.w("BLINDLESSTEST","***" + description + "_esla: " + i + "-->" + "MaxValue: " + String.valueOf(mmr.maxVal));//.substring(0, String.valueOf(mmr.maxVal).indexOf('.')));
			    }
	
			    // / Show me what you got
	//		    Imgproc.rectangle(img_cloned, matchLoc, new Point(matchLoc.x + templ_preprocesed.size().width,
	//		            matchLoc.y + templ_preprocesed.size().height), new Scalar(0, 255,0));
	
	//		        // Save the visualized detection.
	//		        Imgcodecs.imwrite(outFile + "-" + i + ".jpg", img_resized);
			    
			    if (mmr.maxVal > found.maxVal) {
					found.maxVal = mmr.maxVal;
					found.maxLoc = mmr.maxLoc;
					found.minVal = r; //No quiero guardar el minVal.. solo el width
				}
			    
			    img_cloned.release();
			    img_resized.release();
			}
			
		}
		return found;
	}


	private Mat resizeAndProcessPicture(Mat img_preprocesed, double i, Mat img_resized) {
		//Search image on cache
		if (pictureCache.containsKey((int) i)) {
			img_resized.release();
			return pictureCache.get((int) i).clone();
		}
		
		//Resize image
		double newWidth = img_preprocesed.size().width * (1 - (i * 0.05));
		double newHeight = img_preprocesed.size().height * (1 - (i * 0.05));
		Imgproc.resize(img_resized, img_resized, new Size(newWidth, newHeight));
		Imgproc.Canny(img_resized, img_resized, 50, 200);
		
		//Update Cache
		pictureCache.put((int) i, img_resized.clone());
		return img_resized;
	}


	public double comparateSupIzq(Mat img_preprocesed, String templateFile, String outFile, String templateToWrite, int match_method, String description) {
		try {
		    Mat templ_preprocesed = getImageToProcess(templateFile, false);
		    
//	    	Imgproc.Canny(templ_preprocesed, templ_preprocesed, 50, 200);
//		    Imgcodecs.imwrite(templateToWrite, templ_preprocesed);
			
			return comparate(img_preprocesed, templ_preprocesed, match_method, outFile, description);
		} catch (Exception e) {
			Log.e("***" + description + "***", "No se pudo leer este");
			return 0.0;
		}
	}


	public Mat getImageToProcess(String inFile, boolean reduceResolution) {
		/*LEO LA IMAGEN*/
		Mat img_preprocesed = null;
		
		if (reduceResolution) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			Bitmap bitmap = BitmapFactory.decodeFile(inFile, options);
			img_preprocesed = new Mat (bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(4));
			Utils.bitmapToMat(bitmap, img_preprocesed);
			bitmap.recycle();
		}else {
			img_preprocesed = Imgcodecs.imread(inFile);
		}

		//Preprocess image to grayScale
//			Imgproc.resize(img_preprocesed, img_preprocesed, new Size(620, 344));
		Imgproc.cvtColor(img_preprocesed, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
		return img_preprocesed;
	}
	
	public BestMatches readSupIzq(Mat img_billete, String templateToCheck, String outFile){
			
		/*LEO IMAGEN ORIGINAL (BILLETE) Y TEMPLATE ORIGINAL*/
		Mat img_template = getImageToProcess(templateToCheck, false);
		
		/*Busco el punto donde mejor matchee el template y el billete*/
		MinMaxLocResult found = resizeToFindBestMatch(img_billete,
				img_template, Imgproc.TM_CCOEFF, "Nothing");
		
		// Calculo los puntos donde mejor matcheo para ubicarlos en la imagen original
        Point matchLoc = new Point(found.maxLoc.x * found.minVal, found.maxLoc.y * found.minVal); //found.minval sería el r!
        Point matchLoc2 = new Point((found.maxLoc.x + img_template.size().width) * found.minVal, (found.maxLoc.y + img_template.size().height) * found.minVal); //found.minval sería el r!
		
		/* GUARDO EL ROI EN UNA NUEVA IMAGEN (EL SUPIZQ ENCONTRADO) => IMAGEN PREPROCESADA RECORTADA EN EL AREA DE TEMPLATE DETECTADO */
        Mat ROI=img_billete.submat((int)matchLoc.y,(int)matchLoc2.y,(int)matchLoc.x,(int)matchLoc2.x);
        Imgcodecs.imwrite(outFile + ".jpg", ROI);
       
		/*CONVIERTO El ROI A BITMAP PARA USAR TESSERACT Y CORROBORAR COINCIDENCIA CON EL TEMPLATE ORIGINAL*/ 
        Bitmap bmp_temp_2 = Bitmap.createBitmap(ROI.cols(), ROI.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(ROI, bmp_temp_2);
		bmp_temp_2 = bmp_temp_2.copy(Bitmap.Config.ARGB_8888, true);
		BestMatches bestMatch = new BestMatches(bmp_temp_2, found.maxVal);
		
		img_template.release();
		img_billete.release();
		return bestMatch;
//        return makeMatchTemplate(outFile + ".jpg", img_preprocesed, temp_preprocesed, 0.6);
	}
	
	public BestMatches readCenter(Mat billeteToCheck, String templateToCheck,
			String outFile) {

		BestMatches bestMatch = readSupIzq(billeteToCheck, templateToCheck, outFile);
		
		/*APLICO MATCH TEMPLATE*/
		bestMatch.setImage(RotateBitmap(bestMatch.getImage(),90));
        return bestMatch;
	}
	
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
		if (source == null) return null;
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      Bitmap sourceAngle = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	      source.recycle();
	      return sourceAngle;
	}
	
// TRAIT IMAGE
//    Imgproc.GaussianBlur(img_preprocesed, img_preprocesed, new Size(15, 15), 1, 0);
//    Imgproc.equalizeHist(img_preprocesed, img_preprocesed);
//    Imgproc.Canny(img_preprocesed, img_preprocesed, 100, 200);


	private Bitmap makeMatchTemplate(String outFile, Mat img_preprocesed,
			Mat temp_preprocesed, double minValSupported) {
		int result_cols = img_preprocesed.cols() - temp_preprocesed.cols() + 1;
        int result_rows = img_preprocesed.rows() - temp_preprocesed.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
		Imgproc.matchTemplate(img_preprocesed, temp_preprocesed, result, Imgproc.TM_CCOEFF_NORMED);

        /*LOCALIZO EL BEST MATCH CON minMaxLoc*/
        MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;
        
        Log.w("BLINDLESSTEST","El maxLoc: " + mmr.maxVal);
        if (mmr.maxVal < minValSupported) return null;
        
//        Descomentar para Ver que fue lo que encontró
//        Imgproc.rectangle(img_preprocesed, matchLoc, new Point(matchLoc.x + temp_preprocesed.cols(),
//                matchLoc.y + temp_preprocesed.rows()), new Scalar(0, 255, 0));
//        Imgcodecs.imwrite(outFile, img_preprocesed);
        
		/* GUARDO EL ROI EN UNA NUEVA IMAGEN (EL SUPIZQ ENCONTRADO) => IMAGEN PREPROCESADA RECORTADA EN EL AREA DE TEMPLATE DETECTADO */
        Mat ROI=img_preprocesed.submat((int)matchLoc.y,(int)(matchLoc.y + temp_preprocesed.rows()),(int)matchLoc.x,(int)(matchLoc.x + temp_preprocesed.cols()));
        Imgcodecs.imwrite(outFile + ".jpg", ROI);
       
		/*CONVIERTO El ROI A BITMAP PARA USAR TESSERACT Y CORROBORAR COINCIDENCIA CON EL TEMPLATE ORIGINAL*/ 
        Bitmap bmp_temp_2 = Bitmap.createBitmap(ROI.cols(), ROI.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(ROI, bmp_temp_2);
		bmp_temp_2 = bmp_temp_2.copy(Bitmap.Config.ARGB_8888, true);
		return bmp_temp_2;
	}

	
	public String textPreprocess(String path_texto, boolean toWhiteAndBlack){
		
		Mat img_original = Imgcodecs.imread(path_texto);
		
		/*CONVIERTO EL TEXTO A ESCALA DE GRISES*/
		Mat img_preprocesed = new Mat(img_original.size(),CvType.CV_8UC1);
		Imgproc.cvtColor(img_original, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(img_original, img_original, Imgproc.COLOR_BGR2GRAY);
		
				
		RotatedRect minRect = this.rotatedAngle(img_preprocesed);
	    	    
	    /*ROTO LA IMAGEN ORIGINAL*/
	    Mat rotImage = Imgproc.getRotationMatrix2D(minRect.center, minRect.angle, 1);
	    Mat rotated = new Mat();
	    Imgproc.warpAffine(img_original, rotated, rotImage, img_original.size(), Imgproc.INTER_CUBIC);
	    
		if (toWhiteAndBlack) {
			/*CONVIERTO EL TEXTO A BLANCO Y NEGRO DE LA IMAGEN ORIGINAL => FONDO BLANCO Y LETRAS NEGRAS*/
			Imgproc.adaptiveThreshold(img_original, img_original, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 4);
		}
		
		Imgcodecs.imwrite(path_texto, img_original);
		
		img_original.release();
		img_preprocesed.release();
		rotImage.release();
		rotated.release();
//		
		return path_texto;
	}
	
	public RotatedRect rotatedAngle(Mat img){
		
		/*CONVIERTO EL TEXTO A BLANCO Y NEGRO DE LA IMAGEN => FONDO NEGRO Y LETRAS BLANCAS*/		
		Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
		
		/*APLICO ERODE SOBRE LA IMAGEN A PREPROCESAR*/
		Mat element = Imgproc.getStructuringElement(
			    Imgproc.MORPH_CROSS, new Size(5, 3));	
		Imgproc.erode(img, img, element);
		
		
		/*BUSCO LOS PIXELES BLANCOS DE LA IMAGEN A PREPROCESAR Y LOS GUARDO EN UNA LISTA DE PUNTOS*/
		ArrayList<Point> pointsInterestList = new ArrayList<Point>();
	
	    for (int j = 0; j < img.rows(); j++) {
	        for (int k = 0; k < img.cols(); k++) {
	            double[] pixel = img.get(j, k);

	            if (pixel[0] == 255) {
	                //add Point of Mat to list of points
	                Point point = new Point(k, j);
	                pointsInterestList.add(point);
	            }
	        }
	    }
	
	    MatOfPoint2f m2fFromList = new MatOfPoint2f();
	    m2fFromList.fromList(pointsInterestList); //create MatOfPoint2f from list of points
	    MatOfPoint2f m2f = new MatOfPoint2f();
	    m2fFromList.convertTo(m2f, CvType.CV_32FC2); //convert to type of MatOfPoint2f created from list of points
	    
	    /*BUSCO EL ANGULO DE ROTACION*/
	    RotatedRect minRect = Imgproc.minAreaRect(m2f);
	    double angle = minRect.angle;
	    if (angle < -45.)
	        minRect.angle += 90.;	  
		return minRect;
	}


	public void cleanCache() {
		pictureCache.clear();
	}
	
	
	

//templ_preprocesed = traitImage(templ_preprocesed);
	

}
