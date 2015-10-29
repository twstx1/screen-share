package com.codepath.examples.audiovideodemo;

import android.view.Surface;

public class hellotest {
	
    static {
	         System.loadLibrary("wtf");
	   }
    
    public static native long nativeDataPush(long handle,byte data[], int type); //1 stands for video   2 stands for audio
    public static native long nativePicDataPush(long handle, byte data[], int type); //1 stands for video   2 stands for audio
    public static native long nativeVideoParameters(long handle,int width, int height, int format, int framerate);
    public static native long nativeAudioParameters(long handle,int channel, int sample_rate, int encod_bit, int bufferSize);
    public static native long nativeOnCreate(long id, String sip, int stp, int sup);
    public static native long nativeOnResume(long handle);
    public static native long nativeOnPause(long handle);
    public static native long nativeOnStop(long handle);
    public static native long nativeSetSurface(long handle, Surface surface);
    
  //  public static native long nativeOnStop(long handle);
  //  public static native long nativeSetSurface(long handle, Surface surface);


}
