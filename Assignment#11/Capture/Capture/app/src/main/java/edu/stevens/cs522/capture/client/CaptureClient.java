package edu.stevens.cs522.capture.client;

import android.app.Activity;
import android.util.Log;

import edu.stevens.cs522.capture.util.CameraUtils;

public class CaptureClient {
	
	private static final String TAG = CaptureClient.class.getCanonicalName();
	
	public static final int CAPTURE_OK = Activity.RESULT_OK;
	
	public static final int CAPTURE_CANCELED = Activity.RESULT_CANCELED;
	
	public static final int CAMERA_UNAVAILABLE = Activity.RESULT_FIRST_USER+1;
	
	public static final String RESULT_KEY = "result_code";
	
	public static final String MESSAGE_KEY = "message";

	public static final String CAMERA_KEY = "camera_id";
	
	public static boolean launch(final Activity context, int requestCode, String message) {
		/*
		 * Favor back-facing over front-facing camera.
		 */
		int camid = CameraUtils.getBackFacingCamera();
		if (camid < 0) {
			camid = CameraUtils.getFrontFacingCamera();
		}
		if (camid < 0) {
			Log.e(TAG, "No front-facing or back-facing camera detected.");
			return false;
		}
		// TODO launch CameraActivity
		return false;
	}

}
