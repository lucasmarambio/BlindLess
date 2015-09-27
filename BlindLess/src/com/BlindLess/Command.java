package com.BlindLess;

import android.hardware.Camera;


interface Command {
    void runCommand();
}

interface CommandCamera {
	int runCommand(byte[] data, Camera camera);
}
