package edu.stevens.cs522.capture.util;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraUtils {

	private static final String TAG = CameraUtils.class.getCanonicalName();

	public static int getCameraByDirection(int direction) {
		int numCameras = Camera.getNumberOfCameras();
		CameraInfo info = new CameraInfo();
		for (int camid = 0; camid < numCameras; camid++) {
			Camera.getCameraInfo(camid, info);
			if (info.facing == direction) {
				return camid;
			}
		}
		return -1;
	}

	public static int getFrontFacingCamera() {
		return getCameraByDirection(CameraInfo.CAMERA_FACING_FRONT);
	}

	public static int getBackFacingCamera() {
		return getCameraByDirection(CameraInfo.CAMERA_FACING_BACK);
	}

	public static Camera bindCamera(int camid) throws IOException {
		try {
			return Camera.open(camid);
		} catch (RuntimeException e) {
			Log.w(TAG, "Camera may already be in use.", e);
			return null;
		}
	}

	public static void releaseCamera(Camera camera) {
		camera.release();
	}

	public static double PREFERRED_ASPECT_RATIO = 0.75;

	public static final double ASPECT_TOLERANCE = 0.1;
	
	public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		double targetRatio = (double) w / h;
		if (sizes == null) {
			return null;
		}

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find a size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
				continue;
			}
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find a match for the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	/**
	 * This method configures the camera with a set of defaults for brightness, flash, camera mode, and picture sizes.
	 */
	public static void setCameraDefaults(Camera camera) {
		Parameters params = camera.getParameters();

		// Supported picture formats (all devices should support JPEG).
		List<Integer> formats = params.getSupportedPictureFormats();

		if (formats.contains(ImageFormat.JPEG)) {
			params.setPictureFormat(ImageFormat.JPEG);
			params.setJpegQuality(100);
		} else {
			params.setPictureFormat(PixelFormat.RGB_565);
		}

		// Now the supported picture sizes.
		List<Size> sizes = params.getSupportedPictureSizes();
		Size size = sizes.get(sizes.size() - 1);
		params.setPictureSize(size.width, size.height);

		/*
		 * For preview size, we will pick the second largest in landscape mode, with 4:3 aspect ratio.
		 */
		sizes = params.getSupportedPreviewSizes();
		int maxHeight = 0;
		for (Size sz : sizes) {
			if (sz.height < sz.width) {
				maxHeight = Math.max(maxHeight, sz.height);
			}
		}
		int preferredHeight = 0;
		for (Size sz : sizes) {
			if (sz.height < sz.width && preferredHeight < sz.height && sz.height < maxHeight) {
				preferredHeight = sz.height;
			}
		}
		int preferredWidth = (int) Math.round(PREFERRED_ASPECT_RATIO * preferredHeight);

		// size = sizes.get(sizes.size() - 1);
		size = getOptimalPreviewSize(sizes, preferredWidth, preferredHeight);
		params.setPreviewSize(size.width, size.height);

		// Set the brightness to auto.
		if (params.getSupportedWhiteBalance() != null
				&& StringUtils.lookup(Parameters.WHITE_BALANCE_AUTO, params.getSupportedWhiteBalance()) >= 0) {
			params.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
		}

		// Set the flash mode to auto.
		if (params.getSupportedFlashModes() != null
				&& StringUtils.lookup(Parameters.FLASH_MODE_AUTO, params.getSupportedFlashModes()) >= 0) {
			params.setFlashMode(Parameters.FLASH_MODE_AUTO);
		}

		// If available, set scene mode to steady photo (compensate for vibration)
		if (params.getSupportedSceneModes() != null
				&& StringUtils.lookup(Parameters.SCENE_MODE_STEADYPHOTO, params.getSupportedSceneModes()) >= 0) {
			params.setSceneMode(Parameters.SCENE_MODE_STEADYPHOTO);
		}

		// Lastly set the focus to auto. Try macro mode if auto focus not available
		if (StringUtils.lookup(Parameters.FOCUS_MODE_AUTO, params.getSupportedFocusModes()) >= 0) {
			params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		} else if (StringUtils.lookup(Parameters.FOCUS_MODE_MACRO, params.getSupportedFocusModes()) >= 0) {
			params.setFocusMode(Parameters.FOCUS_MODE_MACRO);
		}

		camera.setParameters(params);
	}

	// public static void setPictureSize(Parameters parameters) {
	// List<Size> list = parameters.getSupportedPictureSizes();
	// if (list != null) {
	// Size theSize = null;
	// for (Size size : list) {
	// if (Math.abs(3F * ((float) size.width / 4F) - (float) size.height) < 0.1F * (float) size.width
	// && (theSize == null || size.height > theSize.height && size.width < 3000))
	// theSize = size;
	// }
	// ;
	// if (theSize != null)
	// parameters.setPictureSize(theSize.width, theSize.height);
	// else
	// Log.e(TAG, "No supported picture size found.");
	// }
	// }
	//
	// public static void setPreviewSize(Parameters parameters) {
	// List<Size> list = parameters.getSupportedPreviewSizes();
	// if (list != null) {
	// Size theSize = null;
	// for (Size size : list) {
	// if (Math.abs(3F * ((float) size.width / 4F) - (float) size.height) < 0.1F * (float) size.width
	// && (theSize == null || size.height > theSize.height && size.width < 3000))
	// theSize = size;
	// }
	// ;
	// if (theSize != null) {
	// parameters.setPreviewSize(theSize.width, theSize.height);
	// } else {
	// Log.e(TAG, "No supported picture size found.");
	// }
	// }
	// }

	public static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		/**
		 * A basic Camera preview class
		 */
		private static final String TAG = CameraPreview.class.getCanonicalName();

		private SurfaceHolder theHolder;
		private Camera theCamera;

		private Size previewSize;

		public CameraPreview(Context context) {
			super(context);
		}

		public CameraPreview(Context context, Camera camera) {
			super(context);
			theCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			theHolder = getHolder();
			theHolder.addCallback(this);

			requestLayout();

			setCameraDefaults(theCamera);

			previewSize = theCamera.getParameters().getPreviewSize();
			
			theHolder.setFixedSize(previewSize.width, previewSize.height);

		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw the preview.
			try {
				// Parameters parameters = theCamera.getParameters();
				// setPictureSize(parameters);
				// setPreviewSize(parameters);
				theCamera.setPreviewDisplay(holder);
				theCamera.startPreview();
			} catch (IOException e) {
				Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// empty. Take care of releasing the Camera preview in your activity.
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// If your preview can change or rotate, take care of those events here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (theHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				theCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here
			Parameters parameters = theCamera.getParameters();
			parameters.setPreviewSize(previewSize.width, previewSize.height);
			requestLayout();

			theCamera.setParameters(parameters);
			theCamera.startPreview();

			// Parameters parameters = theCamera.getParameters();
			// setPictureSize(parameters);
			// setPreviewSize(parameters);

			// start preview with new settings
			try {
				theCamera.setPreviewDisplay(theHolder);
				theCamera.startPreview();

			} catch (Exception e) {
				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			}
		}
	}

}
