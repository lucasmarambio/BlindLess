package com.BlindLess;

import org.opencv.core.Mat;

import android.hardware.Camera;


interface Command {
    void runCommand();
}

interface CommandCamera {
	int runCommand(byte[] data, Camera camera);
}

interface CommandComparisson {
	double runCommand(ImageComparator comparator, Mat billeteToCheck, String templateToCheck, String outFile, String templateToWrite, int match_method, String string);
}

interface CommandRead {
	BestMatches runCommand(ImageComparator comparator, Mat billeteToCheck, String templateToCheck, String outFile);
}