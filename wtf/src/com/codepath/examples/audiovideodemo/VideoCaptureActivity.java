package com.codepath.examples.audiovideodemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

//import com.example.test2.R;
//import com.marakana.android.videocapturedemo.R;
//import youten.redo.y1camerapreview.JavaFilter;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VideoCaptureActivity extends Activity {
	private Camera camera;
	private boolean autofocus;
	private SurfaceHolder surfaceHolder;
	private ImageView imageView;
	private TextView textView;
	private long elapsedTime;
	String storagePath = "";
	int cameraCount = 0;
	int cameraID = 1;
	Button recordButton;
	Button stopButton;
	Button picButton;
	private boolean isRecording;
	private boolean isRecordingStopped = true;
	private boolean isPictureTaken = false;
	private long native_handle = 0;
	File file;
	String dir = "PlayCamera";
	FileOutputStream outStreamVideo = null;
	Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	private int[] mRGBData = new int[200 * 200];
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 native_handle = nativeOnCreate(1, "192.168.9.64", 5555, 6666);		
		 SurfaceView surfaceView2 = (SurfaceView)findViewById(R.id.surfaceView2);	
	        surfaceView2.getHolder().addCallback(surfaceHolderCallback2);
	        surfaceView2.setOnClickListener(new OnClickListener() {
	                public void onClick(View view) {
	                    Toast toast = Toast.makeText(VideoCaptureActivity.this,
	                                                 "This demo combines Java UI and native EGL + OpenGL renderer",
	                                                 Toast.LENGTH_LONG);
	                    toast.show();
	                }});		
		
		final File parentPath = Environment.getExternalStorageDirectory();

		// private final String DST_FOLDER_NAME = "PlayCamera";
		storagePath = parentPath.getAbsolutePath() + "/" + "PlayCamera";
		// 翻译附件在全屏模式。
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_yuv);
		lockScreenOrientation();
		this.recordButton = (Button) super.findViewById(R.id.recordButton);
		this.stopButton = (Button) super.findViewById(R.id.stopButton);
		this.picButton = (Button) super.findViewById(R.id.picButton);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceHolderCallback);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		imageView = (ImageView) findViewById(R.id.imageView);
		textView = (TextView) findViewById(R.id.textView);
		textView.setText("");
	}

	@Override
	protected void onResume() {
		super.onResume();
        Log.d("wtf", "onResume()");
        nativeOnResume(native_handle);
		startCamera();
	}

	// 处理
	@Override
	protected void onPause() {
		super.onPause();
        Log.i("wtf", "onPause()");
        nativeOnPause(native_handle);
		stopCamera();
	}
	
    @Override
    protected void onStop() {
        super.onDestroy();
        Log.i("wtf", "onStop()");
        nativeOnStop(native_handle);
    }
	public void startRecording(View v) {
		Log.d("wtf", "TWS start Recording.........");
		isRecording = true;
		isRecordingStopped = false;
		// FileOutputStream outStreamVideo = null;
		try {
			file = FileUtils.createFileInSDCard(dir,
					"Video_" + System.currentTimeMillis() + ".YUV");
			outStreamVideo = new FileOutputStream(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, R.string.cannot_record, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		Toast.makeText(this, R.string.recording, Toast.LENGTH_SHORT).show();
	}

	public void stopRecording(View v) {
		Log.d("wtf", "TWS stop Recording.........");
		isRecording = false;
		Toast.makeText(this, R.string.saving, Toast.LENGTH_SHORT).show();
		//isRecordingStopped = true;
	}

	public void takePicture(View v) {
		Log.d("wtf", "TWS take a picture.........");
		isPictureTaken = true;
		Toast.makeText(this, R.string.picture, Toast.LENGTH_SHORT).show();
		try {
			file = FileUtils.createFileInSDCard(dir,
					"Image_" + System.currentTimeMillis() + ".jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, R.string.cannot_picture, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		// Uri uriSavedImage = Uri.fromFile(file);
		Log.i("wtf", "TWS debug @ 1");

	}

	private boolean startCamera() {
		if (hasCamera(this)) {
			if (camera != null)
				camera.release();
			cameraCount = Camera.getNumberOfCameras(); // get cameras number

//			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
//				Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
//				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
//
//					try {
//						camera = Camera.open(camIdx); // 打开
//														// "Fail to connect to camera service".
//						cameraID = camIdx;
//					} catch (RuntimeException e) {
//						e.printStackTrace();
//						Toast.makeText(this, "无法访问摄像头." + "\n请尝试重新启动装置",
//								Toast.LENGTH_LONG).show();
//						finish();
//						return false;
//					}
//				}
//			}
            if (cameraCount > 1) {
            	try {
                camera = Camera.open(1);
                cameraID = 1 ;
            	} catch (RuntimeException e){
				e.printStackTrace();
				Toast.makeText(this, "无法访问前摄像头." + "\n请尝试重新启动装置",
						Toast.LENGTH_LONG).show();            		         		
            	}
            } else {

            	try {
                camera = Camera.open(0);
                cameraID = 0 ;
            	} catch (RuntimeException e){
				e.printStackTrace();
				Toast.makeText(this, "无法访问后摄像头." + "\n请尝试重新启动装置",
						Toast.LENGTH_LONG).show();            		          		
            	}            
            }
			
		} else {
			Toast.makeText(this, "抱歉，没有检测到设备上的摄像机.", Toast.LENGTH_LONG).show();
			finish();
			return false;
		}
		return true;
	}

	private void stopCamera() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			// 停止的预处理.
			camera.stopPreview();
			// 释放相机使用的其他应用程序.
			camera.release();
			camera = null;
		}
	}

	private void refreshCamera(int width, int height) {
		if (camera != null) {
			try {
				camera.stopPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}

			setOptimalPreview(width, height);
		}
	}

	// 设备是否有摄像机.
	private boolean hasCamera(Context context) {
		return context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA);
	}

	private void lockScreenOrientation() {
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	// 确定图像的最佳配比.
	private void setOptimalPreview(int width, int height) {
		if (surfaceHolder.getSurface() != null && camera != null) {
			Camera.Parameters param = camera.getParameters();
			param.setPreviewSize(width, height);
			// 列出可用的权限定义屏幕.
			List<Camera.Size> sizes = param.getSupportedPreviewSizes();
			if (sizes != null) {
				int displayOrientation = getScreenOrientation(this, cameraID);
				Camera.Size optimalSize = getOptimalPreviewSize(sizes, width,
						height, displayOrientation);
				if (optimalSize != null) {
					param.setPreviewSize(optimalSize.width, optimalSize.height);
					//setCameraDisplayOrientation(this, cameraID, camera);
					// setCameraDisplayOrientation(this, 1, camera);
					try {
						camera.setParameters(param); //
														// "RuntimeException: setParameters failed".
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
				camera.addCallbackBuffer(new byte[width
				           						* height * 3 / 2]);
				camera.setPreviewCallback(previewCallback);
				// camera.setPreviewCallbackWithBuffer(previewCallback);
				try {
					camera.setPreviewDisplay(surfaceHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				camera.startPreview();
			}
		}
	}

	// 预览渲染的处理.
	SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (startCamera())
				setOptimalPreview(200, 200);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			refreshCamera(width, height);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			stopCamera();
		}
	};


	// 预览渲染的处理.
	SurfaceHolder.Callback surfaceHolderCallback2 = new SurfaceHolder.Callback() {

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// holder.setFormat(PixelFormat.RGBX_8888);
			nativeSetSurface(native_handle, holder.getSurface());
		}

		public void surfaceCreated(SurfaceHolder holder) {
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			nativeSetSurface(native_handle, null);
		}
	};
	public boolean onTouchEvent(MotionEvent event) {// 屏幕触摸事件
		if (event.getAction() == MotionEvent.ACTION_DOWN) {// 按下时自动对焦
			camera.autoFocus(null);
			autofocus = true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP && autofocus == true) {// 放开后拍照
			// camera.takePicture(null, null, this);
			autofocus = false;
		}
		return true;
	}

	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		@Override
		public synchronized void onPreviewFrame(byte[] data, Camera camera) {
			// 添加一个计算一帧的处理时间
			long currentTime = System.currentTimeMillis();
			if (elapsedTime == 0)
				elapsedTime = currentTime;
			final long time = currentTime - elapsedTime;
			elapsedTime = currentTime;
			if(camera != null)
			camera.addCallbackBuffer(data);
			//final byte[] mYUVData = new byte[data.length];	
			//System.arraycopy(data, 0, mYUVData, 0, data.length);//转存用于保存视频，与图片处理不干扰
			Camera.Parameters parameters = camera.getParameters();
			int format = parameters.getPreviewFormat();
			// YUV formats require more conversion
			if (format == ImageFormat.NV21 || format == ImageFormat.YUY2
					|| format == ImageFormat.NV16) {
				int width = parameters.getPreviewSize().width;
				int height = parameters.getPreviewSize().height;
			//	JavaFilter.decodeYUV420SP(mRGBData, data, 200,
			//			200);
				Matrix matrix = new Matrix();
				matrix.postRotate(getScreenOrientation(VideoCaptureActivity.this,
						cameraID));
				
				if (isRecording) {
					Log.i("wtf", "TWS debug @ is recording a video");
					try {
						outStreamVideo.write(data);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					if (!isRecordingStopped) {
						Log.i("wtf", "TWS debug @ finish a video ");
						try {
							outStreamVideo.close();
							isRecordingStopped = true;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				YuvImage yuvImage = new YuvImage(data,
						parameters.getPreviewFormat(), width, height, null);

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100,
						outputStream);

				final byte[] bytes = outputStream.toByteArray();
				Log.i("wtf","TWS debug @ write a file using JNI ");
				
				new Thread() {
					public void run() {
						hellotest.nativePicDataPush(SilentCameraActivity.native_handle, bytes, 1);
					}
				}.start();
				FileOutputStream outStream = null;
				if (isPictureTaken) {
					try {
						// Write to SD Card
						// File file = FileUtils.createFileInSDCard(null,
						// "Image_"
						// + System.currentTimeMillis() + ".jpg");
						// Uri uriSavedImage = Uri.fromFile(file);
						Log.i("wtf", "TWS debug @ take a picture");
						outStream = new FileOutputStream(file);
						outStream.write(bytes);
						outStream.close();
					//	Toast.makeText(this, R.string.picture, Toast.LENGTH_SHORT).show();	
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					//	Toast.makeText(this, R.string.cannot_picture, Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						e.printStackTrace();
					//	Toast.makeText(this, R.string.cannot_picture, Toast.LENGTH_SHORT).show();
					} finally {
					}
					isPictureTaken = false;
				}
				
				// final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
				// bytes.length);

				Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);
				final Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, width,
						height, matrix, true);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						imageView.setImageBitmap(bitmap);
						textView.setText(elapsedTime == 0 ? "" : String
								.valueOf(time) + " мс");
					}
				});
			}
		}
	};

	// 确定图像的最佳配比方，使屏幕上的对象
	// 发生的比例，
	// 接近真实的情况。
	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes,
			int width, int height, int displayOrientation) {
		final double ASPECT_TOLERANCE = 0.1;

		if (sizes == null || height == 0)
			return null;
		double targetRatio = (double) width / height;
		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = height;
		if (displayOrientation == 90 || displayOrientation == 270)
			targetRatio = (double) height / width;

		// 试图找到最优的纵横比.
		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE)
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
		}
		// 如果无法找到最近的纵横比，找到最接近的
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	// 翻转时，相机的旋转图像的处理。
	private void setCameraDisplayOrientation(Activity activity, int cameraId,
			android.hardware.Camera camera) {
	//	camera.setDisplayOrientation(getScreenOrientation(activity, cameraId));
	}

	// .
	private int getScreenOrientation(Activity activity, int cameraId) {
		if (Build.VERSION.SDK_INT < 9)
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		else {
			// .
			int rotation = activity.getWindowManager().getDefaultDisplay()
					.getRotation();
			int degrees = 0;
			switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
			}

			android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
			android.hardware.Camera.getCameraInfo(cameraId, info);
		//	Log.i("wtf", "TWS debug cameraId =   " + cameraId);
			int result = 0;
			// 前后的相机以不同的翻转角度.
				if (cameraId == 1)
					result = (360 - degrees) - info.orientation +180;
				else
					result = (360 - degrees) - info.orientation +360;

			result %= 360;

			return result;
			/*
			 * private void data2file(byte[] w, String fileName) throws
			 * Exception {//将二进制数据转换为文件的函数 FileOutputStream out =null; try { out
			 * =new FileOutputStream(fileName); out.write(w); out.close(); }
			 * catch (Exception e) { if (out !=null) out.close(); throw e; } }
			 */
		}
	}
	
    public static native long nativeOnCreate(long id, String sip, int stp, int sup);
    public static native long nativeOnResume(long handle);
    public static native long nativeOnPause(long handle);
    public static native long nativeOnStop(long handle);
    public static native long nativeSetSurface(long handle, Surface surface);	
	
//    public static native long nativeDataPush(byte data[], int type); //1 stands for video   2 stands for audio
//    public static native long nativePicDataPush(byte data[], int type); //1 stands for video   2 stands for audio
//    public static native long nativeVideoParameters(int width, int height, int framerate);
//    public static native long nativeAudioParameters(int channel, int sample_rate, int encod_bit);
//  //  public static native long nativeOnStop(long handle);
//  //  public static native long nativeSetSurface(long handle, Surface surface);
//    static {
//   	         System.loadLibrary("hello");
//    	   }
}