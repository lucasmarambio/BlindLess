package com.BlindLess;

import android.hardware.Camera;


interface Command {
    void runCommand();
}

interface CommandCamera {
	void runCommand(byte[] data, Camera camera);
}
