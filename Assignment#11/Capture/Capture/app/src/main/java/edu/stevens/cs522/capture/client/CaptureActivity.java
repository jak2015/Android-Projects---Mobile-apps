package edu.stevens.cs522.capture.client;

import java.io.IOException;

import edu.stevens.cs522.capture.util.IContinue;
import edu.stevens.cs522.capture.R;
import edu.stevens.cs522.capture.Result;
import edu.stevens.cs522.capture.client.CaptureHandler.ICaptureClient;
import edu.stevens.cs522.capture.client.camera.CameraManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CaptureActivity extends Activity implements OnClickListener, SurfaceHolder.Callback, ICaptureClient {

	private static final String TAG = CaptureActivity.class.getCanonicalName();
	
//	private static final String CAMERA_NO_FOCUS_TAG = TAG + ".camera_no_focus";

	private static final int REQUEST_PERMISSION_CAPTURE = 1;

	private static final int REQUEST_PERMISSION_PAUSE = 2;

	private static final int REQUEST_PERMISSION_CANCEL = 3;

	private Activity context = this;

	private boolean hasPermission = false;

	public void onCapture(String code) {
		Intent result = new Intent();
		result.putExtra(CaptureClient.RESULT_KEY, code);
		setResult(CaptureClient.CAPTURE_OK, result);
		finish();
	}

	public void onCameraUnavailable() {
		setResult(CaptureClient.CAMERA_UNAVAILABLE);
		finish();
	}

	public void onDismiss() {
		setResult(CaptureClient.CAPTURE_CANCELED);
		finish();
	}
	
	private String message;

	private int cameraId;
	
	private CameraManager cameraManager;
	private CaptureHandler captureHandler;
	
	private ViewfinderView viewfinder;
	private SurfaceView preview;
	private FrameLayout previewFrame;
	
	private boolean hasSurface;
	
	private InactivityTimer inactivityTimer;
	private AmbientLightManager ambientLightManager;

	@Override
	public ViewfinderView getViewfinderView() {
		return viewfinder;
	}

	@Override
	public Handler getCaptureHandler() {
		return captureHandler;
	}

	@Override
	public CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setFinishOnTouchOutside(false);
		setTheme(android.R.style.Theme_Dialog); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		/*
		 * Retrieve arguments (intent extras).
		 */
		Intent intent = getIntent();
		message = intent.getStringExtra(CaptureClient.MESSAGE_KEY);
		cameraId = intent.getIntExtra(CaptureClient.CAMERA_KEY, -1);

		/*
		 * Initialize the UI.
		 */
		setContentView(R.layout.dialog_capture);

		TextView prompt = (TextView) findViewById(R.id.prompt);
		prompt.setText(message);

		Button capture = (Button) findViewById(R.id.capture);
		capture.setOnClickListener(CaptureActivity.this);

		Button pause = (Button) findViewById(R.id.pause);
		pause.setOnClickListener(CaptureActivity.this);

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(CaptureActivity.this);

		/*
		 * Customize the window.
		 */
		inactivityTimer = new InactivityTimer(context);
		ambientLightManager = new AmbientLightManager(context);
		// beepManager = new BeepManager(context);

		cameraManager = new CameraManager(context);

		viewfinder = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinder.setCameraManager(cameraManager);

		hasSurface = false;
		preview = (SurfaceView) findViewById(R.id.preview_view);
		preview.getHolder().addCallback(this);

		previewFrame = (FrameLayout) findViewById(R.id.camera_preview);

		captureHandler = null;
		// context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		int rotation = context.getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
			case Surface.ROTATION_0:
			case Surface.ROTATION_90:
				context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			default:
				context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder, cameraId);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void drawViewfinder() {
		viewfinder.drawViewfinder();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder, int cameraId) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder, context, cameraId, new IContinue<Boolean>() {
				public void kontinue(Boolean ok) {
					if (!ok) {
						Log.e(TAG, "Failed to initialize the camera, dismissing the dialog for key capture.");
						CaptureActivity.this.onCameraUnavailable();
						// CaptureActivity.this.finish();
						return;
					}
					// Set the size of the frame containing the preview and viewfinder.
					Point p = cameraManager.getPreviewSize();
					LayoutParams frameParams = previewFrame.getLayoutParams();
					frameParams.width = p.x;
					frameParams.height = p.y;
					previewFrame.setLayoutParams(frameParams);
					previewFrame.setVisibility(View.VISIBLE);
					previewFrame.invalidate();
					
					// Creating the handler starts the preview, which can also throw a RuntimeException.
					if (captureHandler == null) {
						// This is where the state machine gets installed
						captureHandler = new CaptureHandler(CaptureActivity.this, cameraManager);
					}

				}
			});
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
//			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
//			displayFrameworkBugMessageAndExit();
		}
	}

	private int getCurrentOrientation() {
		int rotation = context.getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_90:
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		default:
			return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		}
	}
	
	private void startCapture() {
		SurfaceHolder holder = preview.getHolder();
	    if (hasSurface) {
	        // The activity was paused but not stopped, so the surface still exists. Therefore
	        // surfaceCreated() won't be called, so init the camera here.
	        initCamera(holder, cameraId);
	      } else {
	        // Install the callback and wait for surfaceCreated() to init the camera.
	        holder.addCallback(this);
	      }
		ambientLightManager.start(cameraManager);
		inactivityTimer.onResume();
	}
	
	private void pauseCapture() {
		if (captureHandler != null) {
			captureHandler.quitSynchronously();
			captureHandler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			preview.getHolder().removeCallback(this);
		}
	}

	@Override
	public void onPause() {
		pauseCapture();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	// Be sure to capture the KeyDown event in the parent activity and call in here.
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// if (source == IntentSource.NATIVE_APP_INTENT) {
			// setResult(RESULT_CANCELED);
			// finish();
			// return true;
			// }
			// if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
			// restartPreviewAfterDelay(0L);
			// return true;
			// }
			break;
		// Handle these events so they don't launch the Camera app
		case KeyEvent.KEYCODE_FOCUS:
		case KeyEvent.KEYCODE_CAMERA:
			return true;
		// Use volume up/down to turn on light
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			cameraManager.setTorch(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			cameraManager.setTorch(true);
			return true;
		}
		return false;
	}

	@Override
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		Log.d(TAG, "Returning result of decoding QR code image.");
		onCapture(rawResult.getText());
	}
	
	public void onClick(View view) {
		/*
		 * Request permission if required
		 */
		int id = view.getId();
		if (id == R.id.capture) {
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAPTURE);
			} else {
				startCapture();
			}
		} else if (id == R.id.pause) {
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_PAUSE);
			} else {
				pauseCapture();
			}
		} else if (id == R.id.cancel) {
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CANCEL);
			} else {
				pauseCapture();
				onDismiss();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int request, String[] permissions, int[] grantResults) {
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch (request) {
				case REQUEST_PERMISSION_CAPTURE:
					startCapture();
					break;
				case REQUEST_PERMISSION_PAUSE:
					pauseCapture();
					break;
				case REQUEST_PERMISSION_CANCEL:
					pauseCapture();
					onDismiss();
					break;
				default:
					throw new IllegalArgumentException("Unrecognized request " + request);
			}
		} else {
			Log.w(TAG, "Permissions not granted for "+request);
		}
	}

}
