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
import android.graphics.Matrix;
import android.util.Log;

public class ImageComparator extends Activity{

	
	public double comparate(Mat img_preprocesed, Mat templ_preprocesed, int match_method,String outFile, String description) {
		
		try {

	        // / Create the result matrix
	
	        MinMaxLocResult found = resizeToFindBestMatch(img_preprocesed,
					templ_preprocesed, match_method, description);
	
	        // / Show me what you got
	        Point matchLoc = new Point(found.maxLoc.x * found.minVal, found.maxLoc.y * found.minVal); //found.minval sería el r!
	        Point matchLoc2 = new Point((found.maxLoc.x + templ_preprocesed.size().width) * found.minVal, (found.maxLoc.y + templ_preprocesed.size().height) * found.minVal); //found.minval sería el r!
	        Imgproc.rectangle(img_preprocesed, matchLoc, matchLoc2, new Scalar(0, 255, 0));
	
//	        // Save the visualized detection.
	        Imgcodecs.imwrite(outFile + ".jpg", img_preprocesed);
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
		found.maxVal = 0.0;
		for (double i = 0; i < 10; i++) {
			Mat img_resized = img_preprocesed.clone();
			Mat img_cloned = img_preprocesed.clone();
			double newWidth = img_preprocesed.size().width * (1 - (i * 0.1));
			double newHeight = img_preprocesed.size().height * (1 - (i * 0.1));
			Imgproc.resize(img_resized, img_resized, new Size(newWidth, newHeight));
			
			double r = img_preprocesed.width() / newWidth;
			
			if (img_resized.size().height < templ_preprocesed.size().height || img_resized.size().width < templ_preprocesed.size().width) {
				break;
			}
			
			Imgproc.Canny(img_resized, img_resized, 50, 200);
			
		    int result_cols = img_resized.cols() - templ_preprocesed.cols() + 1;
		    int result_rows = img_resized.rows() - templ_preprocesed.rows() + 1;
		    Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
			Imgproc.matchTemplate(img_resized, templ_preprocesed, result, match_method);
			// / Localizing the best match with minMaxLoc
		    MinMaxLocResult mmr = Core.minMaxLoc(result);
		    
		    Point matchLoc;
		    if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
		        matchLoc = mmr.minLoc;
		        Log.w("BLINDLESSTEST","***" + description + "_esla: " + i + "-->" + "MinValue: " + String.valueOf(mmr.minVal).substring(0, String.valueOf(mmr.minVal).indexOf('.')));
		    } else {
		        matchLoc = mmr.maxLoc;
		        Log.w("BLINDLESSTEST","***" + description + "_esla: " + i + "-->" + "MaxValue: " + String.valueOf(mmr.maxVal).substring(0, String.valueOf(mmr.maxVal).indexOf('.')));
		    }

		    // / Show me what you got
		    Imgproc.rectangle(img_cloned, matchLoc, new Point(matchLoc.x + templ_preprocesed.size().width,
		            matchLoc.y + templ_preprocesed.size().height), new Scalar(0, 255,0));

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
		return found;
	}


	public double comparateSupIzq(String inFile, String templateFile, String outFile, String templateToWrite, int match_method, String description) {
		try {
			/*LEO LA IMAGEN Y EL TEMPLATE*/
			Mat img_preprocesed = Imgcodecs.imread(inFile);
			Mat templ_preprocesed = Imgcodecs.imread(templateFile);
		
			//Preprocess image to grayScale
//			Imgproc.resize(img_preprocesed, img_preprocesed, new Size(620, 344));
		    Imgproc.cvtColor(img_preprocesed, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
		    
		    
		    //Preprocess template to grayScale
			Imgproc.cvtColor(templ_preprocesed, templ_preprocesed, Imgproc.COLOR_BGR2GRAY);
//			Imgproc.Canny(templ_preprocesed, templ_preprocesed, 50, 200);
//			Imgcodecs.imwrite(templateToWrite, templ_preprocesed);
			
			return comparate(img_preprocesed, templ_preprocesed, match_method, outFile, description);
		} catch (Exception e) {
			Log.e("***" + description + "***", "No se pudo leer este");
			return 0.0;
		}
	}
	
	public BestMatches readSupIzq(String billeteToCheck, String templateToCheck, String outFile){
			
		/*LEO IMAGEN ORIGINAL (BILLETE) Y TEMPLATE ORIGINAL*/
		Mat img_billete = Imgcodecs.imread(billeteToCheck);
		Mat img_template = Imgcodecs.imread(templateToCheck);
		
		/*CONVIERTO LA IMAGEN Y EL TEMPLATE A ESCALA DE GRISES*/
		Imgproc.cvtColor(img_billete, img_billete, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(img_template, img_template, Imgproc.COLOR_BGR2GRAY);
		
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
		return bestMatch;
//        return makeMatchTemplate(outFile + ".jpg", img_preprocesed, temp_preprocesed, 0.6);
	}
	
	public BestMatches readCenter(String billeteToCheck, String templateToCheck,
			String outFile) {
		/*LEO IMAGEN ORIGINAL (BILLETE) Y TEMPLATE ORIGINAL*/
		Mat img_billete = Imgcodecs.imread(billeteToCheck);
		Mat img_template = Imgcodecs.imread(templateToCheck);
		
		/*CONVIERTO LA IMAGEN Y EL TEMPLATE A ESCALA DE GRISES*/
		Mat img_preprocesed = new Mat(img_billete.size(),CvType.CV_8UC1);
		Mat temp_preprocesed = new Mat(img_template.size(),CvType.CV_8UC1);
		Imgproc.cvtColor(img_billete, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(img_template, temp_preprocesed, Imgproc.COLOR_BGR2GRAY);
		
		/*APLICO MATCH TEMPLATE*/
		BestMatches bestMatch = new BestMatches(RotateBitmap(makeMatchTemplate(outFile, img_preprocesed, temp_preprocesed, 0.2),90), 0.0);
        return bestMatch;
	}
	
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
		if (source == null) return null;
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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

	
	public String textPreprocess(String path_texto){
		
		Mat txt_original = Imgcodecs.imread(path_texto);
		
		/*CONVIERTO EL TEXTO A ESCALA DE GRISES*/
		Mat img_preprocesed = new Mat(txt_original.size(),CvType.CV_8UC1);
		Imgproc.cvtColor(txt_original, img_preprocesed, Imgproc.COLOR_BGR2GRAY);
		
		/*CONVIERTO EL TEXTO A BLANCO Y NEGRO*/
		Imgproc.adaptiveThreshold(img_preprocesed, img_preprocesed, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 4);
		Imgcodecs.imwrite(path_texto, img_preprocesed); 
		
		return path_texto;
	}

	

//templ_preprocesed = traitImage(templ_preprocesed);
	

}
