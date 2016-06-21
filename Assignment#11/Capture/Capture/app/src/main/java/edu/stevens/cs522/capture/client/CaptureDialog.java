package edu.stevens.cs522.capture.client;

import java.io.IOException;

import edu.stevens.cs522.capture.util.CameraUtils;
import edu.stevens.cs522.capture.util.IContinue;
import edu.stevens.cs522.capture.R;
import edu.stevens.cs522.capture.Result;
import edu.stevens.cs522.capture.client.CaptureHandler.ICaptureClient;
import edu.stevens.cs522.capture.client.camera.CameraManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CaptureDialog extends DialogFragment implements OnClickListener, SurfaceHolder.Callback, ICaptureClient {

	public interface ICaptureListener {
		
		public void onCapture(String code, int dialogId);

		public void onCameraUnavailable(int dialogId);
		
		public void onDismiss(int dialogId);
		
	}

	private static final String TAG = CaptureDialog.class.getCanonicalName();

	public static final String DIALOG_KEY = "dialog_id";

	public static final String MESSAGE_KEY = "message_resid";

	public static final String CAMERA_KEY = "camera_id";
	
//	private static final String CAMERA_NO_FOCUS_TAG = TAG + ".camera_no_focus";

	public static CaptureDialog launch(final Activity context, final String tag, final int messageResid, final int dialogId) {
		/*
		 * Favor back-facing over front-facing camera.
		 */
		int camid = CameraUtils.getBackFacingCamera();
		if (camid < 0) {
			camid = CameraUtils.getFrontFacingCamera();
		}
		if (camid < 0) {
			Log.e(TAG, "No front-facing or back-facing camera detected.");
			((ICaptureListener) context).onCameraUnavailable(dialogId);
			return null;
		}

		CaptureDialog dialog = new CaptureDialog();
		Bundle args = new Bundle();
		args.putInt(DIALOG_KEY, dialogId);
		args.putInt(MESSAGE_KEY, messageResid);
		args.putInt(CAMERA_KEY, camid);
		dialog.setArguments(args);
		dialog.show(context.getFragmentManager(), tag);
		return dialog;
	}

	private Activity context;

	private ICaptureListener listener;
	
	private int dialogId;

	private int messageResid;

	private int cameraId;
	
	private View rootView;

	private CameraManager cameraManager;
	private CaptureHandler captureHandler;
	
	// @SuppressWarnings("unused")
	// private BeepManager beepManager;
	
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof ICaptureListener)) {
			throw new IllegalStateException("Activity must implement ICaptureListener.");
		}
		this.context = activity;
		this.listener = (ICaptureListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialogId = getArguments().getInt(DIALOG_KEY);
		messageResid = getArguments().getInt(MESSAGE_KEY);
		cameraId = getArguments().getInt(CAMERA_KEY);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.dialog_capture, container, false);

		TextView prompt = (TextView) rootView.findViewById(R.id.prompt);
		prompt.setText(messageResid);

		Button capture = (Button) rootView.findViewById(R.id.capture);
		capture.setOnClickListener(CaptureDialog.this);

		Button pause = (Button) rootView.findViewById(R.id.pause);
		pause.setOnClickListener(CaptureDialog.this);

		Button cancel = (Button) rootView.findViewById(R.id.cancel);
		cancel.setOnClickListener(CaptureDialog.this);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		inactivityTimer = new InactivityTimer(context);
		ambientLightManager = new AmbientLightManager(context);
		// beepManager = new BeepManager(context);

		cameraManager = new CameraManager(context);

		viewfinder = (ViewfinderView) rootView.findViewById(R.id.viewfinder_view);
		viewfinder.setCameraManager(cameraManager);
		
		hasSurface = false;
		preview = (SurfaceView) rootView.findViewById(R.id.preview_view);
		preview.getHolder().addCallback(this);
		
		previewFrame = (FrameLayout) rootView.findViewById(R.id.camera_preview);

		captureHandler = null;
		// context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		context.setRequestedOrientation(getCurrentOrientation());

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
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		return dialog;
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//
//		// CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
//		// want to open the camera driver and measure the screen size if we're going to show the help on
//		// first launch. That led to bugs where the scanning rectangle was the wrong size and partially
//		// off screen.
//	}

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
						CaptureDialog.this.dismiss();
						CaptureDialog.this.listener.onCameraUnavailable(CaptureDialog.this.dialogId);
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
						captureHandler = new CaptureHandler(CaptureDialog.this, cameraManager);
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
		listener.onCapture(rawResult.getText(), dialogId);
	}
	
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.capture) {
			startCapture();
		} else if (id == R.id.pause) {
			pauseCapture();
		} else if (id == R.id.cancel) {
			pauseCapture();
			getDialog().cancel();
			listener.onDismiss(dialogId);
		}
	}

}
