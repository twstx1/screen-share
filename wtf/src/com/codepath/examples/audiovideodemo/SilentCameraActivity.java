package com.codepath.examples.audiovideodemo;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


//import youten.redo.y2ndkyuv420sp.R;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.jni.NativeFilter;

/**
 * Camera#setPreviewCallbackWithBuffer YUV420SP UI説明 左上：Camera Preview
 * 右下：YUV420SP→ARGB 右上：YUV420SP→ARGB
 *
 * @author tws
 */

public class SilentCameraActivity extends Activity {
	Button RecordButton;
	Button StopButton;
	Button SwitchButton;
	private static final String TAG = "YUV420SP";
	private static final String FORMAT_FPS = "YUV420SP->ARGB %d fps\nAve. %.3fms\nmin %.3fms max %.3fms";
	private static int PREVIEW_WIDTH = 640;
	private static int PREVIEW_HEIGHT = 480;
	// private int mSurfaceWidth = 640;
	// private int mSurfaceHeight = 480;
	private boolean autofocus;
	private SurfaceView mPreviewSurfaceView;
	// private SurfaceView mFilterSurfaceView;
	private Camera mCamera;
	int cameraCount = 0;
	public static boolean isSharing = false;
	private boolean isSharingStopped = true;
	// for filter
	// YUV420SP→ARGB変換後
	private byte[] mYUVData = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 3/2];
	private Paint mPaint = new Paint();
	public static long native_handle = 0;
	// private long handle = 0;
	// for fps
	// 计算转换时间
	private TextView mFpsTextView;
	private long mSumEffectTime;
	private long mMinEffectTime = 0;
	private long mMaxEffectTime = 0;
	private long mFrames;
	private long mPrivFrames;
	private String mFpsString;
	private Timer mFpsTimer;
	public static int cameraID = 1;
	/**
	 * Camera Preview Callback
	 */

	private SurfaceHolder.Callback mPreviewSurfaceListener = new SurfaceHolder.Callback() {

		public void surfaceCreated(SurfaceHolder holder) {
			Log.d("wtf", "TWS debug @@ 001 cameraID =" + cameraID);
			if (Camera.getNumberOfCameras() > 1) {
				mCamera = Camera.open(1);
				cameraID = 1;
			} else {
				mCamera = Camera.open(0);
				cameraID = 0;
			}
			if (mCamera != null) {
				mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
				try {
					mCamera.setPreviewDisplay(holder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// mSurfaceWidth = width;
			// mSurfaceHeight = height;
			if (mCamera != null) {

				mCamera.stopPreview();
				Parameters params = mCamera.getParameters();

				params.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
				mCamera.setParameters(params);
				mCamera.startPreview();

				mCamera.addCallbackBuffer(new byte[PREVIEW_WIDTH
						* PREVIEW_HEIGHT * 3 / 2]);
			}

			// start timer
			mFrames = 0;
			mPrivFrames = 0;
			mSumEffectTime = 0;
			mFpsTimer = new Timer();
			mFpsTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {

					if ((mPrivFrames > 0) && (mSumEffectTime > 0)) {
						long frames = mFrames - mPrivFrames;
						mFpsString = String.format(FORMAT_FPS, frames,
								((double) mSumEffectTime)
										/ (frames * 1000000.0),
								((double) mMinEffectTime) / (1000000.0),
								((double) mMaxEffectTime) / (1000000.0));
						mSumEffectTime = 0;
						mMinEffectTime = 0;
						mMaxEffectTime = 0;
						runOnUiThread(new Runnable() {
							public void run() {
								mFpsTextView.setText(mFpsString);
							}
						});
					}
					mPrivFrames = mFrames;
				}
			}, 0, 1000); // 1000ms periodic
		}

		public boolean onTouchEvent(MotionEvent event) {// 屏幕触摸事件 为了自动对焦
			if (event.getAction() == MotionEvent.ACTION_DOWN) {// 按下时自动对焦
				mCamera.autoFocus(null);
				autofocus = true;
			}
			if (event.getAction() == MotionEvent.ACTION_UP && autofocus == true) {// 放开后拍照
				// camera.takePicture(null, null, this);
				autofocus = false;
			}
			return true;
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// stop timer
			if (mFpsTimer != null) {
				mFpsTimer.cancel();
				mFpsTimer = null;
			}

			// deinit preview
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.setPreviewCallbackWithBuffer(null);
				mCamera.release();
				mCamera = null;
			}
		}

	};

	public void StartSharing(View v) {
		Log.d("wtf", "TWS start sharing and set video parameters.........");
		isSharing = true;
		isSharingStopped = false;
		int mResult = -1;
		// FileOutputStream outStreamVideo = null;
		Toast.makeText(this, R.string.startSharing, Toast.LENGTH_SHORT).show();
		Camera.Parameters parameters = mCamera.getParameters();
		int format = parameters.getPreviewFormat();
		int width = parameters.getPreviewSize().width;
		int height = parameters.getPreviewSize().height;
		int framerate = 15;
		hellotest.nativeVideoParameters(native_handle, height, width, format,
				framerate);
		AudioRecordPCM mRecord_1 = AudioRecordPCM.getInstance();
		mResult = mRecord_1.startRecordAndFile(); // using AudioRecorder PCM to
													// send data to C
		int bufferSizeInBytes = AudioRecord.getMinBufferSize(
				AudioFileFunc.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);
		hellotest.nativeAudioParameters(native_handle,
				AudioFormat.CHANNEL_IN_STEREO, AudioFileFunc.AUDIO_SAMPLE_RATE,
				AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);

	}

	public void StopSharing(View v) {
		Log.d("wtf", "TWS stop Recording.........");
		AudioRecordPCM mRecord_1 = AudioRecordPCM.getInstance();
		mRecord_1.stopRecordAndFile();
		isSharing = false;

		Toast.makeText(this, R.string.stopSharing, Toast.LENGTH_SHORT).show();
		// isRecordingStopped = true;

	}

	SurfaceHolder.Callback surfaceHolderCallback2 = new SurfaceHolder.Callback() {
		

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// holder.setFormat(PixelFormat.RGBX_8888);
			Log.d("wtf", "TWS surface changed in surfaceview2.........");
			hellotest.nativeSetSurface(native_handle, holder.getSurface());
		}

		public void surfaceCreated(SurfaceHolder holder) {
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			hellotest.nativeSetSurface(native_handle, null);
		}
	};

	/**
	 * Camera Preview取得Callback 主要数据获取入口。
	 */
	private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
		
		public void onPreviewFrame(final byte[] data, Camera camera) {
			Log.d("wtf", "TWS debug @  1");

			if (camera != null) {
				camera.addCallbackBuffer(data);
			} else {
				return;
			}
			long currentTime = System.currentTimeMillis();
			Camera.Parameters parameters = camera.getParameters();
			int format = parameters.getPreviewFormat();
			// YUV formats require more conversion
			if (format == ImageFormat.NV21 || format == ImageFormat.YUY2
					|| format == ImageFormat.NV16) {
				// Log.i("TIMS", "TWS debug format " + format + "data len :" +
				// data.length);
				int width = parameters.getPreviewSize().width;
				int height = parameters.getPreviewSize().height;
				// YuvImage yuvImage = new YuvImage(data,
				// parameters.getPreviewFormat(), width, height, null);
				//
				// ByteArrayOutputStream outputStream = new
				// ByteArrayOutputStream();
				// yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100,
				// outputStream);
				//
				// final byte[] bytes = outputStream.toByteArray();

				// Log.i("TIMS", "TWS debug format " + format + "bytes len :" +
				// bytes.length);
				YUVspRotate.YUV420spRotate270(data, mYUVData, PREVIEW_WIDTH, PREVIEW_HEIGHT);
				if (isSharing) {
					new Thread() {
						public void run() {
							hellotest.nativeDataPush(native_handle, mYUVData, 1);
						}
					}.start();
				} else {
					// if (false)// if (!isSharingStopped)
					// {
					// hellotest.nativePicDataPush(null, 1);// finish sharing
					// // video ,write
					// // data to null
					// isSharingStopped = true;
					// }
				}
			}

			long before = System.nanoTime();
			// JavaFilter.decodeYUV420SP(mYUVData, data, PREVIEW_WIDTH,
			// PREVIEW_HEIGHT);
			// call JNI method.
			// NativeFilter.decodeYUV420SP(mYUVData, data, PREVIEW_WIDTH,
			// PREVIEW_HEIGHT);
			long after = System.nanoTime();
			updateEffectTimes(after - before);

			// if (mFilterSurfaceView != null) {
			// SurfaceHolder holder = mFilterSurfaceView.getHolder();
			// Canvas canvas = holder.lockCanvas();
			// // canvas.save();
			// // canvas.scale(mSurfaceWidth / PREVIEW_WIDTH, mSurfaceHeight /
			// PREVIEW_HEIGHT);
			// canvas.drawBitmap(mYUVData, 0, PREVIEW_WIDTH, 0, 0,
			// PREVIEW_WIDTH, PREVIEW_HEIGHT, false, mPaint);
			// // canvas.restore();
			// holder.unlockCanvasAndPost(canvas);
			// mFrames++;
			// }
		}
	};

	private void SwitchCamera() {
		SurfaceHolder holder = mPreviewSurfaceView.getHolder();
		if (hasCamera(this)) {
			if (Camera.getNumberOfCameras() > 1) {
				CameraInfo cameraInfo = new CameraInfo();
				cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

				for (int i = 0; i < cameraCount; i++) {

					Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
					Log.i("wtf", "TWS debug cameraID = " + cameraID);
					if (cameraID == 1) {
						// 现在是后置，变更为前置
						if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
																							// CAMERA_FACING_BACK后置
								mCamera.setPreviewCallback(null);//后来加上，防止崩溃
								mCamera.stopPreview();// 停掉原来摄像头的预览
								mCamera.release();// 释放资源
								mCamera = null;// 取消原来摄像头
								mCamera = Camera.open(0);// 打开当前选中的摄像头
								mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
								Log.i("wtf", "TWS debug ID now = 0 ");
								// deal(mCamera);
							try {
								mCamera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mCamera.startPreview();// 开始预览
							cameraID = 0;
							break;
						}
					} else {
						// 现在是前置， 变更为后置
						if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
							 // CAMERA_FACING_BACK后置
								mCamera.setPreviewCallback(null);  //后来加上，防止崩溃
								mCamera.stopPreview();// 停掉原来摄像头的预览
								mCamera.release();// 释放资源
								mCamera = null;// 取消原来摄像头
								mCamera = Camera.open(1);// 打开当前选中的摄像头
								mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
								Log.i("wtf", "TWS debug ID now = 1 ");
								mCamera.cancelAutoFocus();
								// deal(mCamera);
							try {
								mCamera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mCamera.startPreview();// 开始预览
							cameraID = 1;
							break;
						}
					}

				}

			}

		} else {

			// do noting
			Log.i("wtf", "No Camera was found ");
		}

	}

	private boolean hasCamera(Context context) {
		return context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA);
	}

	/**
	 * 
	 *
	 * @param elapsed
	 *            経過時間
	 */
	private void updateEffectTimes(long elapsed) {
		if (elapsed <= 0) {
			return;
		}
		if ((mMinEffectTime == 0) || (elapsed < mMinEffectTime)) {
			mMinEffectTime = elapsed;
		}
		if ((mMaxEffectTime == 0) || (mMaxEffectTime < elapsed)) {
			mMaxEffectTime = elapsed;
		}
		mSumEffectTime += elapsed;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("wtf", "TWS debug @@ 002");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏显示出来的title，使其能够全屏
		setContentView(R.layout.activity_wtf);
		Log.i("wtf", " TWS before nativeOnCreate() .......handle is "
				+ native_handle);
		native_handle = hellotest.nativeOnCreate(1, "192.168.6.76", 5555, 6666); // TWS
																					// debug
																					// may
																					// cause
																					// delay
																					// in
																					// this
																					// program
																					// because
																					// of
																					// the
																					// service
																					// was
		// not started
		Log.i("wtf", " TWS after  nativeOnCreate() .......handle is"
				+ native_handle);
		SurfaceView surfaceView2 = (SurfaceView) findViewById(R.id.surfaceView2);
		surfaceView2.getHolder().addCallback(surfaceHolderCallback2);
		surfaceView2.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Toast toast = Toast
						.makeText(
								SilentCameraActivity.this,
								"This demo combines Java UI and native EGL + OpenGL renderer",
								Toast.LENGTH_LONG);
				toast.show();
			}
		});
		this.RecordButton = (Button) super.findViewById(R.id.RecordButton);
		this.StopButton = (Button) super.findViewById(R.id.StopButton);
		this.SwitchButton = (Button) super.findViewById(R.id.SwitchButton);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("wtf", "TWS debug @@ 003");
		Log.d("wtf", "onResume()");
		Thread t=new Thread(){
			public void run(){
				hellotest.nativeOnPause(native_handle);		
			}
		};
		t.start();
	}

	// 处理
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("wtf", "TWS debug @@ 004");
		Log.i("wtf", "onPause()");
		Thread t=new Thread(){
			public void run(){
				hellotest.nativeOnPause(native_handle);		
			}
		};
		t.start();
	}

	@Override
	protected void onStop() {
		super.onDestroy();
		Log.d("wtf", "TWS debug @@ 005");
		Log.i("wtf", "onStop()");
		Thread t=new Thread(){
			public void run(){
				hellotest.nativeOnStop(native_handle);		
			}
		};
		t.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("wtf", "TWS debug @@ 006");
		deinit();
	}

	public void SwitchCamera(View v) {
		Log.i("wtf", "Switch the camera ");
		SwitchCamera();
	}

	@SuppressWarnings("deprecation")
	private void init() {
		Log.d("wtf", "TWS debug @@ 007");
		mPreviewSurfaceView = (SurfaceView) findViewById(R.id.preview_surface);
		SurfaceHolder holder = mPreviewSurfaceView.getHolder();
		holder.addCallback(mPreviewSurfaceListener);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		// mFilterSurfaceView = (SurfaceView) findViewById(R.id.filter_surface);
		// mFilterSurfaceView.setZOrderOnTop(true);
		// mFpsTextView = (TextView) findViewById(R.id.fps_text);
	}

	private void deinit() {
		Log.d("wtf", "TWS debug @@ 008");
		SurfaceHolder holder = mPreviewSurfaceView.getHolder();
		holder.removeCallback(mPreviewSurfaceListener);

		mPreviewSurfaceView = null;
		// mFilterSurfaceView = null;
		mFpsTextView = null;
	}

}
