package com.codepath.examples.audiovideodemo;

public class YUVspRotate {

	// public static void rotateYUV240SP(byte[] src,byte[] des,int width,int
	// height)
	// {
	//
	// int wh = width * height;
	// //旋转Y
	// int k = 0;
	// for(int i=0;i<width;i++) {
	// for(int j=0;j<height;j++)
	// {
	// des[k] = src[width*j + i];
	// k++;
	// }
	// }
	//
	// for(int i=0;i<width;i+=2) {
	// for(int j=0;j<height/2;j++)
	// {
	// des[k] = src[wh+ width*j + i];
	// des[k+1]=src[wh + width*j + i+1];
	// k+=2;
	// }
	// }
	//
	//
	// }
	public static void yuv420spRotate90(byte[] des, byte[] src, int width,
			int height) {
		int wh = width * height;
		int k = 0;
		for (int i = 0; i < width; i++) {
			for (int j = height - 1; j >= 0; j--) {
				des[k] = src[width * j + i];
				k++;
			}
		}
		for (int i = 0; i < width; i += 2) {
			for (int j = height / 2 - 1; j >= 0; j--) {
				des[k] = src[wh + width * j + i];
				des[k + 1] = src[wh + width * j + i + 1];
				k += 2;
			}
		}
	}

	public static void YUV420spRotate180(byte[] des, byte[] src, int width,
			int height) {
		int n = 0;
		int uh = height >> 1;
		int wh = width * height;
		// copy y
		for (int j = height - 1; j >= 0; j--) {
			for (int i = width - 1; i >= 0; i--) {
				des[n++] = src[width * j + i];
			}
		}

		for (int j = uh - 1; j >= 0; j--) {
			for (int i = width - 1; i > 0; i -= 2) {
				des[n] = src[wh + width * j + i - 1];
				des[n + 1] = src[wh + width * j + i];
				n += 2;
			}
		}
	}

	public static void YUV420spRotate270(byte[] src, byte[] des, int width,
			int height) {
		int n = 0;
		int uvHeight = height >> 1;
		int wh = width * height;
		// copy y
		for (int j = width - 1; j >= 0; j--) {
			for (int i = 0; i < height; i++) {
				des[n++] = src[width * i + j];
			}
		}

		for (int j = width - 1; j > 0; j -= 2) {
			for (int i = 0; i < uvHeight; i++) {
				des[n++] = src[wh + width * i + j - 1];
				des[n++] = src[wh + width * i + j];
			}
		}
	}
}
