package com.BlindLess;

import android.graphics.Bitmap;
import android.hardware.Camera;


interface Command {
    void runCommand();
}

interface CommandCamera {
	int runCommand(byte[] data, Camera camera);
}

interface CommandComparisson {
	double runCommand(String billeteToCheck, String templateToCheck, String outFile, String templateToWrite, int match_method, String string);
}

interface CommandRead {
	BestMatches runCommand(ImageComparator comparator, String billeteToCheck, String templateToCheck, String outFile);
}