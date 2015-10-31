package com.BlindLess;

import android.graphics.Bitmap;

public class BestMatches {

	private Bitmap image;
	private double maxVal;

	public BestMatches(Bitmap image, double maxVal) {
		this.image = image;
		this.maxVal = maxVal;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public double getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
	}

	public void release() {
		this.image.recycle();
		this.maxVal = 0.0;
	}
}
